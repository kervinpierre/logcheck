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

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * E.g. java -cp 'logcheck-0.9.jar:*' com.sludev.logs.logcheck.main.LogCheckMain --argfile /opt/logcheck/logcheck-service-args-01.txt --verbosity=debu
 *
 *  @author kervin
 */
public class LogCheckMain 
{
    private static final Logger LOGGER
                                    = LogManager.getLogger(LogCheckMain.class);

    private static String[] s_staticArgs = null;
    private static ExecutorService s_mainThreadExe = null;
    private static volatile boolean s_run = true;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {  
        LOGGER.debug("Starting Log Check via its Command Line Interface.");
        
        commonStart(args);
    }
    
    
    /**
     * A common static method for all interfaces to us.  This interface is called
     * currently from the...
     * 1. Command Line Interface - main()
     * 2. Unix Service Interface - JServ
     * 3. Windows Service Interface - ProcRun
     * 
     * @param args 
     */
    public static void commonStart(String[] args)
    {
        LOGGER.debug( String.format("LogCheckMain::commonStart() called [%s]",
                Arrays.toString(args)) );
        
        // Initialize only.  Don't actually run or do anything else
        LinkedHashMap<Integer, LogCheckConfig> res = LogCheckInitialize.initialize(args);
        Map<Integer, LogCheckResult> procRes;


        try
        {
            procRes = processStart(res); 
        }
        catch (LogCheckException ex)
        {
            LOGGER.error("Error running application.", ex);
        }

        LOGGER.info("Exiting running application.");
    }
    
    public static void commonStop(String[] args)
    {
        LOGGER.debug( String.format("LogCheckMain::commonStop() called [%s]",
                Arrays.toString(args)) );
        
        // Initialize only.  Don't actually run or do anything else
        LinkedHashMap<Integer, LogCheckConfig> res = LogCheckInitialize.initialize(args);
        LogCheckResult procRes;

        procRes = processStop(res); 
  
    }
    
    public void destroy()
    {
        LOGGER.info("Service destroy called.");
        
        commonStop(LogCheckMain.s_staticArgs);
    }

    public void init(String[] args) 
    {
        LOGGER.info( String.format("Service init called.\n[[%s]]\n",
                Arrays.toString(args)) );
        
        LogCheckMain.s_staticArgs = args;
    }

    /**
     * Unix/Linux JSvc start method
     * 
     */
    public void start()
    {
        LOGGER.info("Starting LogCheck via its Unix Service Interface.");
        
        commonStart(LogCheckMain.s_staticArgs);
    }

    /**
     * Service stop method.
     * 
     */
    public void stop()
    {
        LOGGER.info("Service stop called...");
        
        ;
    }
    
    /**
     * Stop the ProcRun Windows Service.
     * 
     * @param args 
     */
    public static void windowsStop(String args[])
    {
        LOGGER.info("Windows service stop called...");

        commonStop(args);
    }

    /**
     * Start the ProcRun Windows Service.  This method must be kept running.
     * The stop method may also be run from a separate thread.
     * 
     * An example of the ProcRun install command which must be run prior...
     * 
     * C:\commons-daemon\amd64\prunsrv.exe  //IS/LOGCHECK 
     *   --Classpath=c:\src\fastsitesoft.com\svn\src\target\logcheck-0.9.jar 
     *   --StartMode=jvm --StartClass=com.sludev.logs.logcheck.main.LogCheckMain 
     *   --StartMethod=windowsStart --StopMethod=windowsStop
     *   --JavaHome="C:\Program Files\Java\jdk1.7.0_51" 
     *   --Jvm="C:\Program Files\Java\jdk1.7.0_51\jre\bin\server\jvm.dll" 
     *   --StdOutput=auto --StdError=auto
     * 
     * Then the service can be run using...
     * C:\commons-daemon\amd64\prunsrv.exe  //RS/LOGCHECK
     * or...
     * C:\commons-daemon\amd64\prunsrv.exe  //TS/LOGCHECK
     * 
     * or deleted...
     * C:\commons-daemon\amd64\prunsrv.exe  //DS/LOGCHECK
     * 
     * @param args 
     */
    public static void windowsStart(String args[])
    {
        LOGGER.info("Starting LogCheck via its Windows Service Interface.");
        
        commonStart(args);
    }
    
    /**
     * Start a process thread for doing the actual work.
     *
     * FIXME : Does not return.
     *
     * @param configs
     * @return  
     * @throws LogCheckException
     */
    public static Map<Integer, LogCheckResult> processStart(LinkedHashMap<Integer, LogCheckConfig> configs) throws LogCheckException
    {
        Map<Integer, LogCheckResult> resp = null;
        LogCheckRun currRun = new LogCheckRun(configs);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
            .namingPattern("runThread-%d")
            .build();

        s_mainThreadExe = Executors.newSingleThreadExecutor(thFactory);

        LogCheckConfig mainConf = configs.get(0);

        try
        {
            long i=0;
            do
            {
                LOGGER.debug(String.format("processStart() : Run #%d", i++));

                Future<Map<Integer, LogCheckResult>> currTask = s_mainThreadExe.submit(currRun);

                //s_mainThreadExe.shutdown();

                try
                {
                    resp = currTask.get();
                    if( (resp != null) && (resp.get(0).getStatus() == LCResultStatus.FAIL) )
                    {
                        String msg = String.format("Run task returned an error status '%s'",
                                resp.get(0).getStatus());

                        LOGGER.debug(msg);

                        s_run = false;
                    }
                }
                catch( InterruptedException ex )
                {
                    LOGGER.warn("processStart() : Application 'main' thread was interrupted", ex);
                   // throw new LogCheckException("Application thread was interrupted", ex);
                }
                catch( ExecutionException ex )
                {
                    LOGGER.error("processStart() : Application 'main' thread execution error", ex);
                    throw new LogCheckException("Application execution error", ex);
                }
                finally
                {
                    LOGGER.debug("processStart() : Clean-up.");
                }
            }
            while( s_run && BooleanUtils.isTrue(mainConf.isService()) );
        }
        finally
        {
            // If main leaves for any reason, shutdown all threads
            s_mainThreadExe.shutdown();

            try
            {
                Thread.sleep(100);
            }
            catch( InterruptedException ex )
            {
                ;
            }

            s_mainThreadExe.shutdownNow();
        }
        
        return resp;
    }
    
    /**
     * Stop the currently running main thread process of the service and related
     * threads.
     * 
     * @param config
     * @return
     */
    public static LogCheckResult processStop(LinkedHashMap<Integer, LogCheckConfig> config)
    {
        LogCheckResult resp = LogCheckResult.from(LCResultStatus.SUCCESS);

        s_run = false;

        if( s_mainThreadExe != null )
        {
            // Shutdown the main thread if we have one.
            // This will only work in ProcRun/JSvc JVM hosted mode.
            // IPC would be needed otherwise
            s_mainThreadExe.shutdownNow();
        }
        
        return resp;
    }
}
