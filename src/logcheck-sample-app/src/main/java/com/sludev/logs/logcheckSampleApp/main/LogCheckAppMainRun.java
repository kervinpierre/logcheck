package com.sludev.logs.logcheckSampleApp.main;

import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.generator.IDataGenerator;
import com.sludev.logs.logcheckSampleApp.generator.RandomLineGenerator;
import com.sludev.logs.logcheckSampleApp.output.BufferedWriterWriteFile;
import com.sludev.logs.logcheckSampleApp.output.IWriteFile;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 
 * Created by kervin on 2015-10-13.
 */
public final class LogCheckAppMainRun implements Callable<LCSAResult>
{
    private static final Logger log = LogManager.getLogger(LogCheckAppMainRun.class);

    private final LogCheckAppConfig config;

    private LogCheckAppConfig getConfig()
    {
        return config;
    }

    public LogCheckAppMainRun(final LogCheckAppConfig config)
    {
        this.config = config;
    }

    @Override
    public LCSAResult call() throws Exception
    {
        LCSAResult res = LCSAResult.NONE;

        ScheduledExecutorService schedulerExe = Executors.newScheduledThreadPool(1);

        setupLockFileShutdownHook();

        final IWriteFile wf;
        switch( config.getOutputType() )
        {
            case BUFFEREDWRITER:
                wf = new BufferedWriterWriteFile(config.getOutputPath(),
                        true,
                        config.getAppend(),
                        config.getTruncate());
                break;

            default:
                wf = null;
        }

        if( wf == null )
        {
            throw new LogCheckAppException("Could not create file writing object");
        }

        wf.openFile();

        final IDataGenerator idg;
        switch( config.getOutputGeneratorType() )
        {
            case RANDOMLINE:
                idg = new RandomLineGenerator();
                break;

            default:
                idg = null;
        }

        schedulerExe.scheduleAtFixedRate(() ->
        {
            String currStr = null;

            if( wf == null )
            {
                log.debug("IWriteFile object cannot be null");
                return;
            }

            if( idg == null )
            {
                log.debug("IDataGenerator object cannot be null");
                return;
            }

            currStr = idg.getLine();

            try
            {
                wf.writeLine(currStr);
            }
            catch( IOException ex )
            {
                log.debug("Failing writing file", ex);
            }
        }, 1, config.getOutputFrequency(), SECONDS);

        return res;
    }

    private void setupLockFileShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                log.debug("Processing shutdown....\n");
            }
        });
    }
}