package com.sludev.logs.logcheckSampleApp.main;

import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by kervin on 2015-10-13.
 */
public final class LogCheckAppMain
{
    private static final Logger log = LogManager.getLogger(LogCheckAppMain.class);


    private static String[] staticArgs = null;
    private static ExecutorService mainThreadExe = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        log.debug("Starting Backup Tool via its Command Line Interface.");

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
        log.debug( String.format("LogCheckAppMain::commonStart() called [%s]",
                Arrays.toString(args)) );

        // Initialize only.  Don't actually run or do anything else
        LogCheckAppConfig res = LogCheckAppInitialize.initialize(args);
        LCSAResult procRes;

        try
        {
            procRes = processStart(res);
        }
        catch (LogCheckAppException ex)
        {
            log.error("Error running application.", ex);
        }
    }

    public static void commonStop(String[] args)
    {
        log.debug( String.format("LogCheckAppMain::commonStop() called [%s]",
                Arrays.toString(args)) );

        // Initialize only.  Don't actually run or do anything else
        LogCheckAppConfig res = LogCheckAppInitialize.initialize(args);
        LCSAResult procRes;

        procRes = processStop(res);

    }

    public void destroy()
    {
        log.info("Service destroy called.");

        commonStop(LogCheckAppMain.staticArgs);
    }

    public void init(String[] args)
    {
        log.info( String.format("Service init called.\n[[%s]]\n",
                Arrays.toString(args)) );

        LogCheckAppMain.staticArgs = args;
    }

    /**
     * Unix/Linux JSvc start method
     *
     */
    public void start()
    {
        log.info("Starting BackupTool via its Unix Service Interface.");

        commonStart(LogCheckAppMain.staticArgs);
    }

    /**
     * Service stop method.
     *
     */
    public void stop()
    {
        log.info("Service stop called...");

        ;
    }

    /**
     * Stop the ProcRun Windows Service.
     *
     * @param args
     */
    public static void windowsStop(String args[])
    {
        log.info("Windows service stop called...");

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
     *   --StartMode=jvm --StartClass=com.sludev.logs.logcheck.main.LogCheckAppMain
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
        log.info("Starting BackupTool via its Windows Service Interface.");

        commonStart(args);
    }

    /**
     * Start a process thread for doing the actual work.
     *
     * @param config
     * @return
     * @throws LogCheckAppException
     */
    public static LCSAResult processStart(LogCheckAppConfig config) throws LogCheckAppException
    {
        LCSAResult resp = null;
        LogCheckAppMainRun currRun = new LogCheckAppMainRun(config);
        FutureTask<LCSAResult> currRunTask = new FutureTask<>(currRun);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("main-run-thread-%d")
                .build();

        mainThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future exeRes = mainThreadExe.submit(currRunTask);

        mainThreadExe.shutdown();

        try
        {
            resp = currRunTask.get();
        }
        catch (InterruptedException ex)
        {
            String errMsg = "Application 'main' thread was interrupted";
            log.debug(errMsg, ex);
            throw new LogCheckAppException(errMsg, ex);
        }
        catch (ExecutionException ex)
        {
            String errMsg = "Application 'main' thread execution error";
            log.debug(errMsg, ex);
            throw new LogCheckAppException(errMsg, ex);
        }
        finally
        {
            // If main leaves for any reason, shutdown all threads
            mainThreadExe.shutdownNow();
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
    public static LCSAResult processStop(LogCheckAppConfig config)
    {
        LCSAResult resp = LCSAResult.NONE;

        if( mainThreadExe != null )
        {
            // Shutdown the main thread if we have one.
            // This will only work in ProcRun/JSvc JVM hosted mode.
            // IPC would be needed otherwise
            mainThreadExe.shutdownNow();
        }

        return resp;
    }
}
