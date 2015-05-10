/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.main;

import com.fastsitesoft.agent.processxml.action.ProcessResult;
import com.fastsitesoft.agent.processxml.action.shellaction.FSSShellAction;
import com.fastsitesoft.agent.processxml.action.shellaction.FSSShellActionArg;
import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.fastsitesoft.agent.utils.FSSAgentException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * Test UNIX specific functionality in the agent.
 * 
 * @author kervin
 */
public class MainServiceProcRunWindowsTestCaseIT
{
    private static final org.apache.logging.log4j.Logger log 
                                = LogManager.getLogger(MainServiceProcRunWindowsTestCaseIT.class);
    private Properties testProperties;
    
    @Rule
    public TestWatcher testWatcher = new LogCheckTestWatcher();
    
    @Before
    public void beforeMethod() 
    {
        /**
         * All test cases in this file pertain to WINDOWS only
         */
        Assume.assumeTrue( SystemUtils.IS_OS_WINDOWS );
        
        /**
         * Get the current test properties from a file so we don't hard-code
         * in our source code.
         */
        testProperties = LogCheckProperties.GetProperties();
    }
    
     /**
     * Test the Windows service code
     * 
     *      * An example of the ProcRun install command which must be run prior...
     * 
     * C:\commons-daemon\amd64\prunsrv.exe  //IS/LOGCHECK 
     *   --Classpath=c:\src\fastsitesoft.com\svn\trunk\v1\agent\target\logcheck-0.9.jar 
     *   --StartMode=jvm --StartClass=com.sludev.logs.logcheck.main.LogCheckMain
     *   --StartMethod=windowsStart --StopMethod=windowsStop
     *   --JavaHome="C:\Program Files\Java\jdk1.8.0_45" 
     *   --Jvm="C:\Program Files\Java\jdk1.8.0_45\jre\bin\server\jvm.dll" 
     *   --StdOutput=auto --StdError=auto --StartParams=--service
     * 
     * Then the service can be run using...
     * C:\commons-daemon\amd64\prunsrv.exe  //RS/LOGCHECK 
     * or...
     * C:\commons-daemon\amd64\prunsrv.exe  //TS/LOGCHECK 
     * 
     * or deleted...
     * C:\commons-daemon\amd64\prunsrv.exe  //DS/LOGCHECK 
     * 
     */
    @Test
    public void testMainProcrunStart()
    {
        log.info("Starting Windows ProcSrv.exe agent service test.");
        
        String procSrvCmd = testProperties.getProperty("procsrv.cmd_full_path");
        String javaHome = testProperties.getProperty("procsrv.java_home");
        String targetFldr = testProperties.getProperty("procsrv.cmd_cwd");
        String logcheckJar = testProperties.getProperty("procsrv.logcheck_jar");
        String jvmRelPath = testProperties.getProperty("procsrv.jvm_dll_relative_path");
        
        FSSShellAction cmd = new FSSShellAction();
  
        cmd.setCommand(procSrvCmd);
        
        // Delete the service if it exists
        cmd.addArgument( new FSSShellActionArg("//", "DS/LOGCHECK") );
        
        ProcessResult pr = new ProcessResult();
        try
        {
            pr = cmd.Run();
        }
        catch (FSSAgentException ex)
        {
            String errMsg = String.format("Error running %s", cmd);
            
            log.error(errMsg, ex);
            fail(errMsg);
        }
        
        log.info( pr );
        
        // Install service
        
        cmd = new FSSShellAction();
        cmd.setCommand(procSrvCmd);
        
        cmd.addArgument( new FSSShellActionArg("//", "IS/LOGCHECK") );
        cmd.addArgument( new FSSShellActionArg("--", "LogLevel", "=", "DEBUG") );
        cmd.addArgument( new FSSShellActionArg("--", "StartMode", "=", "Jvm") );
        cmd.addArgument( new FSSShellActionArg("--", "StartMethod", "=", "windowsStart") );
        cmd.addArgument( new FSSShellActionArg("--", "StopMethod", "=", "windowsStop") );
        cmd.addArgument( new FSSShellActionArg("--", "StartParams", "=", "--service") );
        cmd.addArgument( new FSSShellActionArg("--", "StartClass", "=", "com.sludev.logs.logcheck.main.LogCheckMain") );
        
        Path cwd = Paths.get("").toAbsolutePath();
        
        cmd.addArgument( new FSSShellActionArg("--", "Classpath", "=", "\"", " ",
                                String.format("%s", Paths.get(cwd.toString(), targetFldr,
                                        logcheckJar)), true ) );
        cmd.addArgument( new FSSShellActionArg("--", "JavaHome", "=", "\"", " ",
                                String.format("%s", javaHome), true ) );
        cmd.addArgument( new FSSShellActionArg("--", "Jvm", "=", "\"", " ",
                                String.format("%s", Paths.get(javaHome, jvmRelPath)), true ) );
        
        pr = new ProcessResult();
        try
        {
            pr = cmd.Run();
        }
        catch (FSSAgentException ex)
        {
            String errMsg = String.format("Error running %s", cmd);
            
            log.error(errMsg, ex);
            fail(errMsg);
        }
        
        log.info( pr );
        
        // Make sure the installation succeeded
        assertEquals(0L, (long)pr.getExitCode());
        
        cmd = new FSSShellAction();
        cmd.setCommand(procSrvCmd);
        
        // Run the service
        cmd.addArgument( new FSSShellActionArg("//", "TS/LOGCHECK") );
        
        pr = new ProcessResult();
        try
        {
            pr = cmd.Run();
        }
        catch (FSSAgentException ex)
        {
            String errMsg = String.format("Error running %s", cmd);
            
            log.error(errMsg, ex);
            fail(errMsg);
        }
        
        log.info( pr );
        
        // Make sure the test run succeeded
        assertEquals(0L, (long)pr.getExitCode());
    }
    
}
