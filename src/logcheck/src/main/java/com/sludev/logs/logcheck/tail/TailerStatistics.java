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

import com.sludev.logs.logcheck.config.entities.LogCheckDeDupeLog;
import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.parsers.LogCheckStateParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.config.writers.LogCheckStateWriter;
import com.sludev.logs.logcheck.dedupe.ContinueUtil;
import com.sludev.logs.logcheck.enums.LCFileFormats;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
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
    private static final Logger LOGGER
                = LogManager.getLogger(TailerStatistics.class);

    private final Path m_logFile;
    private final Path m_stateFile;
    private final Path m_errorFile;
    private final LCHashType m_hashType;
    private final Integer m_idBlockSize;
    private final String m_setName;

    // Mutable
    private volatile long m_lastProcessedPosition;
    private Instant m_lastProcessedTimeStart;
    private Instant m_lastProcessedTimeEnd;

    public Path getLogFile()
    {
        return m_logFile;
    }

    private LCHashType getHashType()
    {
        return m_hashType;
    }

    public Instant getLastProcessedTimeStart()
    {
        return m_lastProcessedTimeStart;
    }

    public void setLastProcessedTimeStart(Instant lastProcessedTimeStart)
    {
        this.m_lastProcessedTimeStart = lastProcessedTimeStart;
    }

    public Instant getLastProcessedTimeEnd()
    {
        return m_lastProcessedTimeEnd;
    }

    public void setLastProcessedTimeEnd(Instant lastProcessedTimeEnd)
    {
        this.m_lastProcessedTimeEnd = lastProcessedTimeEnd;
    }

    public long getLastProcessedPosition()
    {
        return m_lastProcessedPosition;
    }

    public void setLastProcessedPosition(long lastProcessedPosition)
    {
        this.m_lastProcessedPosition = lastProcessedPosition;
    }

    private TailerStatistics(final Path logFile,
                             final Path stateFile,
                             final Path errorFile,
                             final LCHashType hashType,
                             final Integer idBlockSize,
                             final String setName)
    {
        this.m_logFile = logFile;
        this.m_stateFile = stateFile;
        this.m_errorFile = errorFile;
        this.m_idBlockSize = idBlockSize;

        this.m_lastProcessedPosition = 0L;
        this.m_hashType = hashType;
        this.m_setName = setName;
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
                m_logFile,
                0L,
                m_idBlockSize,
                m_hashType);

        return res;
    }

    public LogFileBlock getLastBlock() throws LogCheckException
    {
        long pos = m_lastProcessedPosition - m_idBlockSize;
        if( pos < 0 )
        {
            LOGGER.debug(String.format("Not enough data. Last Position = %d, ID Block Size = %d",
                    m_lastProcessedPosition, m_idBlockSize));

            return null;
        }

        LogFileBlock res = LogFileBlock.from("LAST_BLOCK",
                m_logFile,
                pos,
                m_idBlockSize,
                m_hashType);

        return res;
    }

    public void save(final Boolean resetPosition,
                     final Boolean ignoreMissingLogFile) throws LogCheckException
    {
        save(getState(ignoreMissingLogFile), m_stateFile, m_errorFile, resetPosition, ignoreMissingLogFile);
    }

    public static void save(final LogCheckState state,
                            final Path stateFile,
                            final Path errorFile,
                            final Boolean resetPosition,
                            final Boolean ignoreMissingLogFile) throws LogCheckException
    {
        LOGGER.debug(String.format("Saving statistics to '%s'.", stateFile));

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
                LOGGER.debug(String.format("TailerStatistics::save() called but no data processed since LastProcessedPosition is %d",
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
            LOGGER.debug("Error creating temp state files.", ex);

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

                LOGGER.debug(errMsg, ex);

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

                LOGGER.debug(errMsg, ex);

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

                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public LogCheckState restore(final Path deDupeDir,
                                 final Integer logFileCount,
                                 final Integer maxLogEntries) throws LogCheckException
    {
        LogCheckState res;

        res = restore(m_stateFile, deDupeDir, m_setName, logFileCount, maxLogEntries);

        return res;
    }


    public static LogCheckState restore(final Path stateFile,
                                              final Path deDupeDir,
                                              final String setName,
                                              final Integer logFileCount,
                                              final Integer maxLogEntries) throws LogCheckException
    {
        // Read the last run's deduplication logs
        List<LogCheckDeDupeLog> ddLogs
                = ContinueUtil.readLastDeDupeLogs(deDupeDir,
                setName,
                null,
                logFileCount);

        List<LogEntryDeDupe> ddObjs
                = ContinueUtil.lastLogEntryDeDupes(ddLogs, maxLogEntries);

        // Read state file for information about the last run
        LogCheckState lcConf = LogCheckStateParser.readConfig(
                ParserUtil.readConfig(stateFile,
                        LCFileFormats.LCSTATE));

        // TODO : Allow 'look back' support to allow using the deduplication logs for confirming the file pointer's accuracy

        return lcConf;
    }

    public LogCheckState getState(final Boolean ignoreMissingLogFile)
    {
        LogCheckState res = null;

        LogFileState currLogFile = null;

        // Generate the Log File tailer statistics
        try
        {
            LogFileBlock firstBlock = null;
            LogFileBlock lastBlock = null;

            if( Files.notExists(m_logFile)
                    && BooleanUtils.isNotTrue(ignoreMissingLogFile))
            {
                throw new LogCheckException(String.format("Log File does not exist '%s",
                        m_logFile));
            }

            if( Files.exists(m_logFile) )
            {
                firstBlock = getFirstBlock();
                lastBlock = getLastBlock();
            }

            currLogFile = LogFileState.from(m_logFile,
                    m_lastProcessedTimeStart,
                    Instant.now(),
                    m_lastProcessedPosition,
                    null,
                    null,
                    lastBlock,
                    firstBlock);
        }
        catch( LogCheckException ex )
        {
            String errMsg = String.format("Error generating statistics for '%s'",
                    m_logFile);

            LOGGER.debug(errMsg, ex);
        }

        res = LogCheckState.from(currLogFile,
                Instant.now(),
                UUID.randomUUID(),
                m_setName,
                null);

        return res;
    }

}