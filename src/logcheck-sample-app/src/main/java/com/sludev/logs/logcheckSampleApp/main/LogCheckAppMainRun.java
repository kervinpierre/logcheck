package com.sludev.logs.logcheckSampleApp.main;

import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.generator.IDataGenerator;
import com.sludev.logs.logcheckSampleApp.generator.RandomLineGenerator;
import com.sludev.logs.logcheckSampleApp.output.BufferedWriterWriteFile;
import com.sludev.logs.logcheckSampleApp.output.IWriteFile;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 
 * Created by kervin on 2015-10-13.
 */
public final class LogCheckAppMainRun implements Callable<LCSAResult>
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckAppMainRun.class);

    private final LogCheckAppConfig config;

    private final AtomicLong runCount;
    private final AtomicLong stopAfterCount;

    private LogCheckAppConfig getConfig()
    {
        return config;
    }

    public LogCheckAppMainRun(final LogCheckAppConfig config)
    {
        this.config = config;

        stopAfterCount = new AtomicLong(0L);

        if( config.getStartLineNumber() == null )
        {
            runCount = new AtomicLong(0L);
        }
        else
        {
            runCount = new AtomicLong(config.getStartLineNumber());
        }
    }

    @Override
    public LCSAResult call() throws Exception
    {
        LCSAResult res;

        if( config.getDeleteLogs() != null
                && config.getDeleteLogs() )
        {
            deleteLogs(config.getOutputPath());
        }

        // On first call make sure the file does not already
        // contain lines
        Long currRotateAfterCount = config.getRotateAfterCount();
        if( currRotateAfterCount != null )
        {
            if( Files.exists(config.getOutputPath()) )
            {
                long lineCount = Files.lines(config.getOutputPath()).count();
                if( lineCount > 0 && currRotateAfterCount > lineCount )
                {
                    currRotateAfterCount = currRotateAfterCount - lineCount;
                }
            }
        }

        IWriteFile wf = newWriteFile(config.getOutputPath(),
                                        currRotateAfterCount);

        do
        {
            res = logFile(wf);
            if(res == LCSAResult.COMPLETED_ROTATE_PENDING)
            {
                wf.closeFile();
                Path bk = IWriteFile.rotateFile(config.getOutputPath(),
                        config.getMaxBackups(),
                        config.getConfirmDeletes());

                wf = newWriteFile(config.getOutputPath(), config.getRotateAfterCount());
            }
        }
        while(res == LCSAResult.COMPLETED_ROTATE_PENDING);

        return res;
    }

    private IWriteFile newWriteFile(final Path output,
                                    final Long rotateAfterCount)
    {
        LOGGER.debug(String.format("newWriteFile() called on '%s'", output));

        IWriteFile wf;

        switch( config.getOutputType() )
        {
            case BUFFEREDWRITER:
                wf = new BufferedWriterWriteFile(output,
                        true,
                        config.getAppend(),
                        config.getTruncate(),
                        rotateAfterCount);
                break;

            default:
                wf = null;
        }

        return wf;
    }

    private LCSAResult deleteLogs(Path outFile) throws LogCheckAppException
    {
        LCSAResult res = LCSAResult.NONE;
        Path parent;

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

            LOGGER.debug(errMsg);
            throw new LogCheckAppException(errMsg);
        }

        try
        {
            Files.delete(outFile);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error deleting '%s'", outFile);

            LOGGER.debug(errMsg);
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

                LOGGER.debug(errMsg);
            }
        }

        return res;
    }

    private LCSAResult logFile(final IWriteFile wf)
                            throws LogCheckAppException, IOException, InterruptedException
    {
        LCSAResult res;

        BasicThreadFactory scheduledFactory = new BasicThreadFactory.Builder()
                .namingPattern("lcAppScheduleThread-%d")
                .build();

        final ScheduledExecutorService schedulerExe = Executors.newScheduledThreadPool(1, scheduledFactory);

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
        final AtomicLong callCount = new AtomicLong(0L);
        final Long stopAfter = config.getStopAfterCount();
        final Integer randomWaitMin = config.getRandomWaitMin();
        final Integer randomWaitMax = config.getRandomWaitMax();
        final Random rand = new Random();

        schedulerExe.scheduleAtFixedRate(() ->
        {
            String currStr = null;

            int currRandWaitMin = 0;

            if( randomWaitMin != null && randomWaitMin > 0 )
            {
                currRandWaitMin = randomWaitMin;
            }

            if( randomWaitMax != null && randomWaitMax > 0 )
            {
                int currRand = rand.nextInt() % randomWaitMax + currRandWaitMin;

                LOGGER.debug(String.format("Sleeping [ %d ] seconds...", currRand));

                try
                {
                    Thread.sleep( currRand * 1000 );
                }
                catch( InterruptedException ex )
                {
                    LOGGER.debug("Thread wait interrupted", ex);
                }
            }

            if( wf == null )
            {
                LOGGER.debug("IWriteFile object cannot be null");
                return;
            }

            if( idg == null )
            {
                LOGGER.debug("IDataGenerator object cannot be null");
                return;
            }

            long currCallCount = callCount.incrementAndGet();
            long currRunCount = runCount.incrementAndGet();
            long currStopAfterCount = stopAfterCount.incrementAndGet();

            if( stopAfter != null
                    && stopAfter > 0
                    && stopAfter < currStopAfterCount )
            {
                threadRes.set(LCSAResult.SUCCESS);
                schedulerExe.shutdown();

                return;
            }

            currStr = idg.getLine(String.format(" [%05d][%d] Random Line",
                                        currRunCount, currCallCount));

            try
            {
                LCSAResult resWrite = wf.writeLine(currStr);

                if( BooleanUtils.isTrue(config.getOutputToScreen()) )
                {
                    LOGGER.info(String.format("'%s'", currStr));
                }

                if( LOGGER.isDebugEnabled() )
                {
                    long tmpCnt = currRunCount;
                    long tmpInterval = 100L;
                    if( config.getStopAfterCount() != null && config.getStopAfterCount() > 10 )
                    {
                        tmpInterval = config.getStopAfterCount() / 10;
                    }

                    if( tmpCnt % tmpInterval == 0 )
                    {
                        LOGGER.debug(String.format("\nLog Line Count == [%d]", tmpCnt));
                    }
                }

                if( resWrite != LCSAResult.SUCCESS )
                {
                    threadRes.set(resWrite);
                    schedulerExe.shutdown();
                }
            }
            catch( IOException ex )
            {
                LOGGER.debug("Failing writing file", ex);
            }
        }, 1,
            config.getOutputFrequency().getLeft(),
            config.getOutputFrequency().getRight());

        schedulerExe.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        res = threadRes.get();

        LOGGER.debug(String.format("Returning result %s", res));

        return res;
    }

    private void setupLockFileShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                LOGGER.debug("setupLockFileShutdownHook() : Processing shutdown....\n");
            }
        });
    }
}