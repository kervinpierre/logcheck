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
    private static final Logger log = LogManager.getLogger(LogEntryStore.class);

    private final ILogEntrySource mainLogEntrySource;
    private final List<ILogEntryStore> entryStores;
    private final Path deDupeLogOutputPath;
    private final String jobName;
    private final UUID runUUID;
    private final Integer deDupeMaxLogsBeforeWrite;
    private final Integer deDupeMaxLogsPerFile;
    private final Integer deDupeMaxLogFiles;

    private LogEntryStore(final ILogEntrySource mainLogEntrySource,
                          final List<ILogEntryStore> entryStores,
                          final Path deDupeLogOutputPath,
                          final String jobName,
                          final UUID runUUID,
                          final Integer deDupeMaxLogsBeforeWrite,
                          final Integer deDupeMaxLogsPerFile,
                          final Integer deDupeMaxLogFiles)
    {
        this.mainLogEntrySource = mainLogEntrySource;
        this.entryStores = entryStores;
        this.deDupeLogOutputPath = deDupeLogOutputPath;
        this.jobName = jobName;
        this.runUUID = runUUID;
        this.deDupeMaxLogsBeforeWrite = deDupeMaxLogsBeforeWrite;
        this.deDupeMaxLogsPerFile = deDupeMaxLogsPerFile;
        this.deDupeMaxLogFiles = deDupeMaxLogFiles;
    }

    public static LogEntryStore from(final ILogEntrySource mainLogEntrySource,
                                     final List<ILogEntryStore> entryStore,
                                     final Path deDupeLogOutputPath,
                                     final String jobName,
                                     final UUID runUUID,
                                     final Integer deDupeMaxLogsBeforeWrite,
                                     final Integer deDupeMaxLogsPerFile,
                                     final Integer deDupeMaxLogFiles)
    {
        LogEntryStore store = new LogEntryStore(mainLogEntrySource,
                entryStore,
                deDupeLogOutputPath,
                jobName,
                runUUID,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile, deDupeMaxLogFiles);

        return store;
    }

    @Override
    public LogCheckResult call() throws Exception
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.NONE);

        String currJobName = jobName;
        if( StringUtils.isBlank(currJobName) )
        {
            currJobName = LogCheckConstants.DEFAULT_SET_NAME;
        }
        currJobName = String.format("%s_%s", currJobName, Instant.now());
        currJobName = currJobName.replaceAll("[^a-zA-Z0-9.-]", "_");

        Path currDeDupePath = null;

        if( deDupeLogOutputPath != null )
        {
            currDeDupePath = deDupeLogOutputPath.resolve(currJobName);
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

        try
        {
            res = ILogEntryStore.process(mainLogEntrySource,
                    entryStores,
                    currDeDupePath,
                    runUUID,
                    deDupeMaxLogsBeforeWrite,
                    deDupeMaxLogsPerFile,
                    deDupeMaxLogFiles);
        }
        catch( InterruptedException ex )
        {
            log.debug("ILogEntryStore.process() interrupted.", ex);
            res = LogCheckResult.from(LCResultStatus.INTERRUPTED);
        }

        return res;
    }
}
