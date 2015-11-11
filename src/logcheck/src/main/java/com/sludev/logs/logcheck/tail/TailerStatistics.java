package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.writers.LogCheckStateWriter;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

/**
 * Track the statistics on an ongoing Tailer job.
 *
 * Created by kervin on 10/27/2015.
 */
public class TailerStatistics
{
    private static final Logger log
                = LogManager.getLogger(TailerStatistics.class);

    private final Path logFile;
    private final Path stateFile;
    private final Path errorFile;
    private final LCHashType hashType;
    private final Integer idBlockSize;
    private final String setName;

    // Mutable
    private Long lastProcessedPosition;
    private Instant lastProcessedTimeStart;
    private Instant lastProcessedTimeEnd;

    public Path getLogFile()
    {
        return logFile;
    }

    private LCHashType getHashType()
    {
        return hashType;
    }

    public Instant getLastProcessedTimeStart()
    {
        return lastProcessedTimeStart;
    }

    public void setLastProcessedTimeStart(Instant lastProcessedTimeStart)
    {
        this.lastProcessedTimeStart = lastProcessedTimeStart;
    }

    public Instant getLastProcessedTimeEnd()
    {
        return lastProcessedTimeEnd;
    }

    public void setLastProcessedTimeEnd(Instant lastProcessedTimeEnd)
    {
        this.lastProcessedTimeEnd = lastProcessedTimeEnd;
    }

    public Long getLastProcessedPosition()
    {
        return lastProcessedPosition;
    }

    public void setLastProcessedPosition(Long lastProcessedPosition)
    {
        this.lastProcessedPosition = lastProcessedPosition;
    }

    private TailerStatistics(final Path logFile,
                             final Path stateFile,
                             final Path errorFile,
                             final LCHashType hashType,
                             final Integer idBlockSize,
                             final String setName)
    {
        this.logFile = logFile;
        this.stateFile = stateFile;
        this.errorFile = errorFile;
        this.idBlockSize = idBlockSize;

        this.lastProcessedPosition = 0L;
        this.hashType = hashType;
        this.setName = setName;
    }

    public static TailerStatistics from(final Path logFile,
                                        final Path stateFile,
                                        final Path errorFile,
                                        final LCHashType hashType,
                                        final Integer idBlockSize,
                                        final String setName)
    {
        TailerStatistics res = new TailerStatistics(logFile,
                                                    stateFile,
                                                    errorFile,
                                                    hashType,
                                                    idBlockSize,
                                                    setName);

        return res;
    }

    public LogFileBlock getFirstBlock( ) throws LogCheckException
    {
        LogFileBlock res = LogFileBlock.from(logFile,
                0L,
                idBlockSize,
                hashType);

        return res;
    }

    public LogFileBlock getLastBlock() throws LogCheckException
    {
        long pos = lastProcessedPosition - idBlockSize;
        if( pos < 0 )
        {
            log.debug(String.format("Not enough data. Last Position = %d, ID Block Size = %d",
                    lastProcessedPosition, idBlockSize));

            return null;
        }

        LogFileBlock res = LogFileBlock.from(logFile,
                pos,
                idBlockSize,
                hashType);

        return res;
    }

    public void save() throws LogCheckException
    {
        save(getState(), stateFile, errorFile);
    }

    public static void save(LogCheckState state, Path stateFile, Path errorFile) throws LogCheckException
    {
        log.debug(String.format("Saving statistics to '%s'.", stateFile));

        Pair<Path,Path> files = null;

        // Serialize state
        try
        {
            files = LogCheckStateWriter.write(state);
        }
        catch( LogCheckException ex )
        {
            log.debug("Error creating temp state files.", ex);

            throw ex;
        }

        if( stateFile != null )
        {
            try
            {
                Files.move(files.getLeft(), stateFile, StandardCopyOption.REPLACE_EXISTING);
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error saving state to the file-system for '%s' and '%s'",
                        files.getLeft(), stateFile);

                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( errorFile != null )
        {
            try
            {
                Files.move(files.getRight(), errorFile, StandardCopyOption.REPLACE_EXISTING);
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error saving state-error file to the file-system for '%s' and '%s'",
                        files.getRight(), errorFile);

                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public LogCheckState getState()
    {
        LogCheckState res = null;

        LogFileState currLogFile = null;

        // Generate the logFile tailer statistics
        try
        {
            currLogFile = LogFileState.from(logFile,
                    getLastProcessedTimeStart(),
                    Instant.now(),
                    getLastProcessedPosition(),
                    null,
                    null,
                    getLastBlock(),
                    getFirstBlock());
        }
        catch( LogCheckException ex )
        {
            String errMsg = String.format("Error generating statistics for '%s'",
                    logFile);

            log.debug(errMsg, ex);
        }

        res = LogCheckState.from(currLogFile,
                Instant.now(),
                UUID.randomUUID(),
                setName,
                null);

        return res;
    }

}
