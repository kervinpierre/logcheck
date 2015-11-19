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

import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
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

    private final ILogEntryStore entryStore;
    private final Path deDupeLogOutputPath;
    private final String jobName;
    private final UUID runUUID;

    private LogEntryStore(final ILogEntryStore entryStore,
                          final Path deDupeLogOutputPath,
                          final String jobName,
                          final UUID runUUID)
    {
        this.entryStore = entryStore;
        this.deDupeLogOutputPath = deDupeLogOutputPath;
        this.jobName = jobName;
        this.runUUID = runUUID;
    }

    public static LogEntryStore from(final ILogEntryStore entryStore,
                                     final Path deDupeLogOutputPath,
                                     final String jobName,
                                     final UUID runUUID)
    {
        LogEntryStore store = new LogEntryStore(entryStore,
                deDupeLogOutputPath,
                jobName,
                runUUID);

        return store;
    }

    @Override
    public LogCheckResult call() throws Exception
    {
        LogCheckResult res;

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

        res = ILogEntryStore.process(entryStore.getMainLogEntrySource(),
                entryStore,
                currDeDupePath,
                runUUID);

        return res;
    }
}
