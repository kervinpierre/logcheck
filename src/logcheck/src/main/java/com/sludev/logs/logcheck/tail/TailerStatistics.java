/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.writers.LogCheckStateWriter;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormattedMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

/**
 * Track the statistics on an ongoing Tailer job.
 *
 * It saves and restores where in a log file we are/were.  And also whether that file is
 * still the file we think it is.
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
    private volatile long lastProcessedPosition;
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

    public long getLastProcessedPosition()
    {
        return lastProcessedPosition;
    }

    public void setLastProcessedPosition(long lastProcessedPosition)
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
        LogFileBlock res = null;

        res = LogFileBlock.from("FIRST_BLOCK",
                logFile,
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

        LogFileBlock res = LogFileBlock.from("LAST_BLOCK",
                logFile,
                pos,
                idBlockSize,
                hashType);

        return res;
    }

    public void save(final Boolean resetPosition,
                     final Boolean ignoreMissingLogFile) throws LogCheckException
    {
        save(getState(ignoreMissingLogFile), stateFile, errorFile, resetPosition, ignoreMissingLogFile);
    }

    public static void save(final LogCheckState state,
                            final Path stateFile,
                            final Path errorFile,
                            final Boolean resetPosition,
                            final Boolean ignoreMissingLogFile) throws LogCheckException
    {
        log.debug(String.format("Saving statistics to '%s'.", stateFile));

        Pair<Path,Path> files = null;
        LogFileState currLFS = state.getLogFile();

        if( currLFS == null && BooleanUtils.isNotTrue(ignoreMissingLogFile) )
        {
            throw new LogCheckException("LogFile cannot be null.");
        }

        if( BooleanUtils.isNotTrue(resetPosition) )
        {
            if( currLFS != null
                    && currLFS.getLastProcessedPosition() < 1 )
            {
                // Don't save a log file that hasn't processed data
                log.debug(String.format("TailerStatistics::save() called but no data processed since LastProcessedPosition is %d",
                            state.getLogFile().getLastProcessedPosition()));

                return;
            }
        }

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

        if( errorFile == null )
        {
            try
            {
                Files.delete(files.getRight());
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error deleting temp file '%s'",
                        files.getRight());

                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }
        else
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

    public LogCheckState restore() throws LogCheckException
    {
        LogCheckState res;

        res = restore(stateFile, errorFile);

        return res;
    }

    public static LogCheckState restore(Path stateFile, Path errorFile) throws LogCheckException
    {
        LogCheckState res = null;

        return res;
    }

    public LogCheckState getState(final Boolean ignoreMissingLogFile)
    {
        LogCheckState res = null;

        LogFileState currLogFile = null;

        // Generate the logFile tailer statistics
        try
        {
            LogFileBlock firstBlock = null;
            LogFileBlock lastBlock = null;

            if( Files.notExists(logFile)
                    && BooleanUtils.isNotTrue(ignoreMissingLogFile))
            {
                throw new LogCheckException(String.format("Log File does not exist '%s",
                        logFile));
            }

            if( Files.exists(logFile) )
            {
                firstBlock = getFirstBlock();
                lastBlock = getLastBlock();
            }

            currLogFile = LogFileState.from(logFile,
                    getLastProcessedTimeStart(),
                    Instant.now(),
                    getLastProcessedPosition(),
                    null,
                    null,
                    lastBlock,
                    firstBlock);
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