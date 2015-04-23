/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.main;

import com.fastsitesoft.agent.processxml.action.ProcessResult;
import com.fastsitesoft.agent.processxml.action.shellaction.FSSShellAction;
import com.fastsitesoft.agent.processxml.action.shellaction.FSSShellActionArg;
import com.fastsitesoft.agent.utils.FSSAgentException;
import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import java.util.Properties;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * Test UNIX specific service functionality in the agent using JSvc
 * 
 * @author kervin
 */
public class MainServiceJsvcUnixTestCaseIT
{
    private static final org.apache.logging.log4j.Logger log 
                                = LogManager.getLogger(MainServiceJsvcUnixTestCaseIT.class);
    private Properties testProperties;
    
    @Rule
    public TestWatcher testWatcher = new LogCheckTestWatcher();
    
    @Before
    public void beforeMethod() 
    {
        /**
         * All test cases in this file pertain to UNIX only
         */
        Assume.assumeTrue( SystemUtils.IS_OS_UNIX );
        
        /**
         * Get the current test properties from a file so we don't hard-code
         * in our source code.
         */
        testProperties = LogCheckProperties.GetProperties();
    }
    
     /**
     * Test the Unix service interface.
     * 
     * E.g. Equivalent to the command line...
     *  /usr/bin/jsvc -home /usr/lib/jvm/java-8-openjdk-amd64/ -debug  -nodetach  -cwd ./target/ -cp logcheck-0.9.jar com.sludev.logs.logcheck.main.LogCheckMain arg1 arg2
     *  1. JSvc requires its full executable path to start.
     *  2. -home points to JAVA_HOME e.g.  $(readlink -f /usr/bin/javac | sed "s:bin/javac::")
     *  3. -debug Prints debugging messages to the console
     *  4. -nodetach prevents spawning as a service
     *  5. -cwd allows us to change the working directory before start
     *  6. -cp sets the class path
     *  7.  Followed by the full main class then any arguments to the program.
     * 
     */
    @Test
    @Ignore
    public void testMainJsvcStart()
    {
        log.info("Starting UNIX Jsvc service test.");
        
        // TODO : Start the jsvc exe and see if it does what we need it to do
        String jsvcCmd = testProperties.getProperty("jsvc.cmd_full_path");
        String javaHome = testProperties.getProperty("jsvc.java_home");
        String jsvcCmdCwd = testProperties.getProperty("jsvc.cmd_cwd");
        String fssagentJar = testProperties.getProperty("jsvc.logcheck_jar");
        
        FSSShellAction cmd = new FSSShellAction();
  
        cmd.setCommand(jsvcCmd);
        
        // Set 'jsvc' arguments...
        cmd.addArgument( new FSSShellActionArg("-", "home", " ", javaHome) );
        cmd.addArgument( new FSSShellActionArg("-", "debug") );
        cmd.addArgument( new FSSShellActionArg("-", "nodetach") );
        cmd.addArgument( new FSSShellActionArg("-", "cwd", " ", jsvcCmdCwd) );
        cmd.addArgument( new FSSShellActionArg("-", "cp", " ", fssagentJar) );
        cmd.addArgument( new FSSShellActionArg("com.sludev.logs.logcheck.main.LogCheckMain") );
        cmd.addArgument( new FSSShellActionArg("--version") );
        
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
        
        assertEquals(0L, (long)pr.getExitCode());
    }
    
}
