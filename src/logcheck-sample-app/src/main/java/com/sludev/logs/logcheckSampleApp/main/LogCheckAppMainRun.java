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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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

        if( config.getDeleteLogs() != null
                && config.getDeleteLogs() )
        {
            deleteLogs(config.getOutputPath());
        }

        IWriteFile wf = newWriteFile(config.getOutputPath());

        do
        {
            res = logFile(wf);
            if(res == LCSAResult.COMPLETED_ROTATE_PENDING)
            {
                wf.closeFile();
                Path bk = IWriteFile.rotateFile(config.getOutputPath(),
                        config.getMaxBackups(),
                        config.getConfirmDeletes());

                wf = newWriteFile(config.getOutputPath());
            }
        }
        while(res == LCSAResult.COMPLETED_ROTATE_PENDING);

        return res;
    }

    private IWriteFile newWriteFile(Path output)
    {
        log.debug(String.format("newWriteFile() called on '%s'", output));

        IWriteFile wf;

        switch( config.getOutputType() )
        {
            case BUFFEREDWRITER:
                wf = new BufferedWriterWriteFile(output,
                        true,
                        config.getAppend(),
                        config.getTruncate(),
                        config.getRotateAfterCount());
                break;

            default:
                wf = null;
        }

        return wf;
    }

    private LCSAResult deleteLogs(Path outFile) throws LogCheckAppException
    {
        LCSAResult res = LCSAResult.NONE;
        Path parent = null;

        if( outFile == null )
        {
            throw new LogCheckAppException("File path cannot be null");
        }

        try
        {
            parent = outFile.toAbsolutePath().getParent();
        }
        catch(Exception ex)
        {
            throw new LogCheckAppException(
                    String.format("Failed retrieving parent directory '%s'", outFile), ex);
        }

        if(Files.notExists(parent))
        {
            String errMsg = String.format("Parent folder '%s' does not exist.", parent);

            log.debug(errMsg);
            throw new LogCheckAppException(errMsg);
        }

        try
        {
            Files.delete(outFile);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error deleting '%s'", outFile);

            log.debug(errMsg);
        }

        Path bakPath;
        for(int i = 0; i <= 9999; i++)
        {
            String newName = String.format("%s.%04d.bak", outFile.getFileName().toString(), i);
            bakPath = parent.resolve(newName);
            if(Files.notExists(bakPath))
            {
                break;
            }

            try
            {
                Files.delete(bakPath);
            }
            catch(IOException ex)
            {
                String errMsg = String.format("Error deleting '%s'", outFile);

                log.debug(errMsg);
            }
        }

        return res;
    }

    private LCSAResult logFile(final IWriteFile wf)
                            throws LogCheckAppException, IOException, InterruptedException
    {
        LCSAResult res = LCSAResult.NONE;

        final ScheduledExecutorService schedulerExe = Executors.newScheduledThreadPool(1);

        setupLockFileShutdownHook();

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

        final AtomicReference<LCSAResult> threadRes = new AtomicReference<>();
        final AtomicInteger callCount = new AtomicInteger(0);

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

            currStr = idg.getLine(String.format("Random Line number %d ", callCount.getAndIncrement()));

            try
            {
                LCSAResult resWrite = wf.writeLine(currStr);

                if( resWrite != LCSAResult.SUCCESS )
                {
                    threadRes.set(resWrite);
                    schedulerExe.shutdown();
                }
            }
            catch( IOException ex )
            {
                log.debug("Failing writing file", ex);
            }
        }, 1,
            config.getOutputFrequency().getLeft(),
            config.getOutputFrequency().getRight());

        schedulerExe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        if( threadRes.get() == LCSAResult.COMPLETED_ROTATE_PENDING )
        {
            res = LCSAResult.COMPLETED_ROTATE_PENDING;
        }

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