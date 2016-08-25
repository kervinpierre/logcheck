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

import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.config.entities.LogCheckDeDupeLog;
import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.config.entities.impl.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.parsers.LogCheckStateParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.config.writers.LogCheckStateWriter;
import com.sludev.logs.logcheck.dedupe.ContinueUtil;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Track the statistics on an ongoing Tailer job.
 *
 * It saves and restores where in a log file we are/were.
 *
 * * It does NOT validate
 * * It does NOT perform or cause I/O on the log file
 *
 * Created by kervin on 10/27/2015.
 */
public final class TailerStatistics
{
    private static final Logger LOGGER
                = LogManager.getLogger(TailerStatistics.class);

    private final Path m_stateFile;
    private final Path m_errorFile;
    private final String m_setName;

    // Mutable
    private final BlockingDeque<LogCheckStateBase> m_savedStates;
    private final BlockingDeque<LogCheckStateBase> m_pendingSaveStates;
    private final BlockingDeque<LogCheckStateBase> m_restoredStates;

    private Instant m_LastProcessedTimeStart;

    public Instant getLastProcessedTimeStart()
    {
        return m_LastProcessedTimeStart;
    }

    public synchronized void setLastProcessedTimeStart( Instant inst)
    {
        m_LastProcessedTimeStart = inst;
    }

    public BlockingDeque<LogCheckStateBase> getRestoredStates()
    {
        return m_restoredStates;
    }

    private TailerStatistics(final Path stateFile,
                             final Path errorFile,
                             final String setName)
    {
        this.m_stateFile = stateFile;
        this.m_errorFile = errorFile;
        this.m_setName = setName;
        this.m_savedStates = new LinkedBlockingDeque<>();
        this.m_pendingSaveStates = new LinkedBlockingDeque<>();
        this.m_restoredStates = new LinkedBlockingDeque<>();
    }

    public static TailerStatistics from(final Path stateFile,
                                        final Path errorFile,
                                        final String setName)
    {
        TailerStatistics res = new TailerStatistics( stateFile,
                                                    errorFile,
                                                    setName);

        return res;
    }

    public synchronized void putPendingSaveState(LogCheckStateBase lcs) throws InterruptedException
    {
        m_pendingSaveStates.putFirst(lcs);
    }

    public synchronized void clearPendingSaveState() throws InterruptedException
    {
        if( m_pendingSaveStates.size() > 0 )
        {
            LOGGER.debug(String.format("Deleting %d states in the pending queue.",
                    m_pendingSaveStates.size()));
        }

        m_pendingSaveStates.clear();
    }

    public synchronized void saveLastPending( final boolean clearQueue,
                                              final boolean resetPosition,
                                              final boolean ignoreMissingLogFile )
            throws InterruptedException, LogCheckException
    {
        LogCheckStateBase state = null;

        if( m_pendingSaveStates.isEmpty() )
        {
            return;
        }

        state = m_pendingSaveStates.removeFirst();

        if( m_pendingSaveStates.size() > 0 )
        {
            LOGGER.debug(String.format("Skipping %d states in the pending queue.", m_pendingSaveStates.size()));
        }

        if( m_savedStates.contains(state) )
        {
            LOGGER.error(String.format("State has already been saved.\n%s", state));

            return;
        }

        if( clearQueue )
        {
            clearPendingSaveState();
        }

        save(state, resetPosition, ignoreMissingLogFile);
    }

    public synchronized void saveOnce(  final LogCheckState currState,
                                    final boolean resetPosition,
                                    final boolean ignoreMissingLogFile )
            throws LogCheckException, InterruptedException
    {
        if( currState == null )
        {
            LOGGER.debug("saveOnce() : Log Check State is null.");

            return;
        }

        if( m_savedStates.contains(currState) )
        {
            LOGGER.debug(String.format("saveOnce() : Log Check State already saved.\n%s", currState));

            return;
        }

        save(currState, resetPosition, ignoreMissingLogFile);
    }

    public synchronized void save(  final LogCheckStateBase currState,
                                    final boolean resetPosition,
                                    final boolean ignoreMissingLogFile )
            throws LogCheckException, InterruptedException
    {
        save(currState, m_stateFile, m_errorFile, resetPosition, ignoreMissingLogFile);

        m_savedStates.putFirst(currState);
    }

    public static synchronized void save(final LogCheckStateBase state,
                                         final Path stateFile,
                                         final Path errorFile,
                                         final Boolean resetPosition,
                                         final Boolean ignoreMissingLogFile ) throws LogCheckException
    {
        LOGGER.debug(String.format("Saving statistics to '%s'.", stateFile));

        Pair<Path,Path> files;

        // FIXME : Use inheritance instead of a class check
        if( state instanceof LogCheckState )
        {
            LogFileState currLFS = ((LogCheckState)state).getLogFile();

            if( (currLFS == null) && BooleanUtils.isNotTrue(ignoreMissingLogFile) )
            {
                throw new LogCheckException("LogFile cannot be null.");
            }

            if( BooleanUtils.isNotTrue(resetPosition) )
            {
                if( (currLFS != null)
                        && (currLFS.getLastProcessedPosition() < 1) )
                {
                    // Don't save a log file that hasn't processed data
                    LOGGER.debug(String.format("TailerStatistics::save() called but no data processed since LastProcessedPosition is %d",
                            currLFS.getLastProcessedPosition()));

                    return;
                }
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

    public synchronized LogCheckState restore(final Path deDupeDir,
                                              final Integer logFileCount,
                                              final Integer maxLogEntries) throws LogCheckException, InterruptedException
    {
        LogCheckState res;

        res = restore(m_stateFile, deDupeDir, logFileCount, maxLogEntries);

        m_restoredStates.putFirst(res);

        return res;
    }

    public synchronized LogCheckState restore(final Path stateFile,
                                              final Path deDupeDir,
                                 final Integer logFileCount,
                                 final Integer maxLogEntries) throws LogCheckException, InterruptedException
    {
        LogCheckState res;

        res = restore(stateFile, deDupeDir, m_setName, logFileCount, maxLogEntries);

        m_restoredStates.putFirst(res);

        return res;
    }

    public static synchronized LogCheckState restore(final Path stateFile,
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
                        LCFileFormat.LCSTATE));


        if( LOGGER.isDebugEnabled() )
        {
//            try
//            {
//                LOGGER.debug(String.format("restore()'ed :\n%s\n", new String(Files.readAllBytes(stateFile))));
//            }
//            catch( IOException ex )
//            {
//                LOGGER.debug("Error dumping State File", ex);
//            }

            if( (lcConf.getLogFile() == null)
                    && ((lcConf.getCompletedLogFiles() == null)
                                    || lcConf.getCompletedLogFiles().isEmpty()) )
            {
                LOGGER.debug("restore() : getLogFile() returned null and getCompletedLogFiles() is also null.");
            }
            else if( (lcConf.getLogFile() != null) && (lcConf.getLogFile().getLastProcessedPosition() < 1) )
            {
                LOGGER.debug(String.format("restore() : Last Processed Position is %d",
                        lcConf.getLogFile().getLastProcessedPosition()));
            }
        }

        // TODO : Allow 'look back' support to allow using the deduplication logs for confirming the file pointer's accuracy

        return lcConf;
    }

    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder(100);

        res.append("TailerStatistics\n{\n");
        res.append(String.format("    Set Name       : '%s'\n", m_setName));
        res.append(String.format("    State File     : '%s'\n", m_stateFile));
        res.append(String.format("    Error File     : '%s'\n", m_errorFile));
        res.append("}\n");

        return res.toString();
    }
}