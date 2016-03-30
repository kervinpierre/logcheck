package com.sludev.logs.elasticsearchApp.main;

import com.sludev.logs.elasticsearchApp.entities.ESAppConfig;
import com.sludev.logs.elasticsearchApp.enums.ESAResult;
import com.sludev.logs.elasticsearchApp.utils.ESAException;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by kervin on 2016-03-29.
 */
public final class EAMain
{
    private static final Logger LOGGER = LogManager.getLogger(EAMain.class);

    private static String[] staticArgs = null;
    private static ExecutorService mainThreadExe = null;

    public static void main(String[] args)
    {
        LOGGER.debug("Starting Backup Tool via its Command Line Interface.");

        commonStart(args);
    }

    public static void commonStart(String[] args)
    {
        LOGGER.debug( String.format("EAMain::commonStart() called [%s]",
                Arrays.toString(args)) );

        // Initialize only.  Don't actually run or do anything else
        ESAppConfig res;

        try
        {
            res = EAInitialize.initialize(args);
        }
        catch( ESAException ex )
        {
            LOGGER.error("Error initializing application.", ex);
            return;
        }

        ESAResult procRes;

        try
        {
            procRes = processStart(res);
        }
        catch (ESAException ex)
        {
            LOGGER.error("Error running application.", ex);
        }
    }

    public static ESAResult processStart(ESAppConfig config) throws ESAException
    {
        ESAResult resp = null;
        EARun currRun = EARun.from(config);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("lcAppMainThread-%d")
                .build();

        mainThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<ESAResult> currRunTask = mainThreadExe.submit(currRun);

        mainThreadExe.shutdown();

        try
        {
            resp = currRunTask.get();
        }
        catch (InterruptedException ex)
        {
            String errMsg = "Application 'main' thread was interrupted";
            LOGGER.debug(errMsg, ex);
            throw new ESAException(errMsg, ex);
        }
        catch (ExecutionException ex)
        {
            String errMsg = "Application 'main' thread execution error";
            LOGGER.debug(errMsg, ex);
            throw new ESAException(errMsg, ex);
        }
        finally
        {
            // If main leaves for any reason, shutdown all threads
            mainThreadExe.shutdownNow();

            LOGGER.debug("processStart() completed");
        }

        return resp;
    }
}
