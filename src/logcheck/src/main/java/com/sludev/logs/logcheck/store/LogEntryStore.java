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

package com.sludev.logs.logcheck.store;

import com.sludev.logs.logcheck.enums.LCDeDupeAction;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Wrap the Log Entry Store implementation to provide deduplication logging preparation, etc.
 *
 * Created by kervin on 2015-11-17.
 */
public final class LogEntryStore implements Callable<LogCheckResult>
{
    private static final Logger LOGGER = LogManager.getLogger(LogEntryStore.class);

    private final ILogEntrySource m_mainLogEntrySource;
    private final List<ILogEntryStore> m_entryStores;
    private final Path m_deDupeLogOutputPath;
    private final String m_jobName;
    private final UUID m_runUUID;
    private final Integer m_deDupeMaxLogsBeforeWrite;
    private final Integer m_deDupeMaxLogsPerFile;
    private final Integer m_deDupeMaxLogFiles;
    private final Integer m_deDupeIgnorePercent;
    private final Integer m_deDupeSkipPercent;
    private final Long m_deDupeIgnoreCount;
    private final Long m_deDupeSkipCount;
    private final LCDeDupeAction m_deDupeDefaultAction;

    // MUTABLE
    public volatile boolean m_run = true;

    public void stop()
    {
        m_run = false;
    }

    private LogEntryStore(final ILogEntrySource mainLogEntrySource,
                          final List<ILogEntryStore> entryStores,
                          final Path deDupeLogOutputPath,
                          final String jobName,
                          final UUID runUUID,
                          final Integer deDupeMaxLogsBeforeWrite,
                          final Integer deDupeMaxLogsPerFile,
                          final Integer deDupeMaxLogFiles,
                          final Integer deDupeIgnorePercent,
                          final Integer deDupeSkipPercent,
                          final Long deDupeIgnoreCount,
                          final Long deDupeSkipCount,
                          final LCDeDupeAction deDupeDefaultAction)
    {
        this.m_mainLogEntrySource = mainLogEntrySource;
        this.m_entryStores = entryStores;
        this.m_deDupeLogOutputPath = deDupeLogOutputPath;
        this.m_jobName = jobName;
        this.m_runUUID = runUUID;
        this.m_deDupeMaxLogsBeforeWrite = deDupeMaxLogsBeforeWrite;
        this.m_deDupeMaxLogsPerFile = deDupeMaxLogsPerFile;
        this.m_deDupeMaxLogFiles = deDupeMaxLogFiles;
        this.m_deDupeIgnorePercent = deDupeIgnorePercent;
        this.m_deDupeSkipPercent = deDupeSkipPercent;
        this.m_deDupeIgnoreCount = deDupeIgnoreCount;
        this.m_deDupeSkipCount = deDupeSkipCount;
        this.m_deDupeDefaultAction = deDupeDefaultAction;
    }

    public static LogEntryStore from(final ILogEntrySource mainLogEntrySource,
                                     final List<ILogEntryStore> entryStore,
                                     final Path deDupeLogOutputPath,
                                     final String jobName,
                                     final UUID runUUID,
                                     final Integer deDupeMaxLogsBeforeWrite,
                                     final Integer deDupeMaxLogsPerFile,
                                     final Integer deDupeMaxLogFiles,
                                     final Integer deDupeIgnorePercent,
                                     final Integer deDupeSkipPercent,
                                     final Long deDupeIgnoreCount,
                                     final Long deDupeSkipCount,
                                     final LCDeDupeAction deDupeDefaultAction)
    {
        LogEntryStore store = new LogEntryStore(mainLogEntrySource,
                entryStore,
                deDupeLogOutputPath,
                jobName,
                runUUID,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile,
                deDupeMaxLogFiles,
                deDupeIgnorePercent,
                deDupeSkipPercent,
                deDupeIgnoreCount,
                deDupeSkipCount,
                deDupeDefaultAction);

        return store;
    }

    @Override
    public LogCheckResult call() throws LogCheckException
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.NONE);

        String currJobName = m_jobName;
        if( StringUtils.isBlank(currJobName) )
        {
            currJobName = LogCheckConstants.DEFAULT_SET_NAME;
        }
        currJobName = String.format("%s_%s", currJobName, Instant.now());
        currJobName = currJobName.replaceAll("[^a-zA-Z0-9.-]", "_");

        Path currDeDupePath = null;

        if( m_deDupeLogOutputPath != null )
        {
            currDeDupePath = m_deDupeLogOutputPath.resolve(currJobName);
            if( Files.exists(currDeDupePath) )
            {
                throw new LogCheckException(String.format("Deduplication log directory should not exist yet '%s'",
                        currDeDupePath));
            }

            try
            {
                Files.createDirectory(currDeDupePath);
            }
            catch( IOException ex )
            {
                throw new LogCheckException(String.format("Failed creating Deduplication log directory '%s'",
                        currDeDupePath), ex);
            }
        }

        do
        {
            try
            {
                res = ILogEntryStore.process(m_mainLogEntrySource,
                        m_entryStores,
                        currDeDupePath,
                        m_runUUID,
                        m_deDupeMaxLogsBeforeWrite,
                        m_deDupeMaxLogsPerFile,
                        m_deDupeMaxLogFiles,
                        m_deDupeIgnorePercent,
                        m_deDupeSkipPercent,
                        m_deDupeIgnoreCount,
                        m_deDupeSkipCount,
                        m_deDupeDefaultAction,
                        null,
                        null);
            }
            catch( InterruptedException ex )
            {
                LOGGER.debug(
                        String.format("ILogEntryStore.process() interrupted. m_run is %b",
                                m_run), ex);

                // Thread stop procedure : First set m_run to false then interrupt.
                res = LogCheckResult.from(LCResultStatus.INTERRUPTED);
            }
        }
        while( m_run );

        return res;
    }
}
