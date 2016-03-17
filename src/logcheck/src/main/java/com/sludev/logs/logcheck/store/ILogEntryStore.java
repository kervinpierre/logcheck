/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.store;

import com.sludev.logs.logcheck.config.entities.LogCheckDeDupeLog;
import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.config.entities.LogEntryVO;
import com.sludev.logs.logcheck.config.writers.LogCheckDeDupeLogWriter;
import com.sludev.logs.logcheck.enums.LCDeDupeAction;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author kervin
 */
public interface ILogEntryStore
{
    static final Logger LOGGER = LogManager.getLogger(ILogEntryStore.class);

    public void init() throws LogCheckException;
    public void destroy() throws LogCheckException;
    public LCResultStatus testConnection() throws LogCheckException;

    public LogCheckResult putValueObj(LogEntryVO entryVO) throws InterruptedException, LogCheckException;

    public static LogCheckResult process(final ILogEntrySource src,
                                         final List<ILogEntryStore> dstStores,
                                         final Path deDupeDirPath,
                                         final UUID runUUID,
                                         final Integer deDupeMaxLogsBeforeWrite,
                                         final Integer deDupeMaxLogsPerFile,
                                         final Integer deDupeMaxLogFiles,
                                         final Integer deDupeIgnorePercent,
                                         final Integer deDupeSkipPercent,
                                         final Long deDupeIgnoreCount,
                                         final Long deDupeSkipCount,
                                         final LCDeDupeAction deDupeDefaultAction,
                                         final Long timeout,
                                         final TimeUnit timeoutUnits) throws InterruptedException, LogCheckException
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);
        LogEntry currEntry;
        LogEntryVO currEntryVO;
        MessageDigest md = null;
        int logCount = 0;

        Path deDupeFileName = null;
        LogCheckDeDupeLog currDeDupeLog = null;

        LOGGER.debug("process() started.");

        try
        {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch( NoSuchAlgorithmException ex )
        {
            LOGGER.debug("Failed getting SHA-256 Hash", ex);
        }

        Set<LogEntryDeDupe> currentProcessedEntries = new HashSet<>(10);
        Long duplicateCount = 0L;
        boolean shouldWrite = false;
        do
        {
            try
            {
                // Setup the deduplication directory if we need it.
                // Rotate the log file after a certain number of log stores.
                if( (deDupeDirPath != null)
                        && ((logCount == 0)
                        || ((deDupeMaxLogsPerFile != null)
                        && (deDupeMaxLogsPerFile > 0)
                        && ((logCount % deDupeMaxLogsPerFile) == 0))) )
                {
                    if( shouldWrite )
                    {
                        // We should write out the final log entries
                        LogCheckDeDupeLogWriter.write(currDeDupeLog, deDupeFileName);
                    }

                    deDupeFileName = LogCheckDeDupeLog.nextFileName(deDupeDirPath, deDupeMaxLogFiles, true);
                    currDeDupeLog = LogCheckDeDupeLog.from(runUUID, Instant.now(), null);
                }
            }
            catch( LogCheckException ex )
            {
                LOGGER.warn("Error writing deduplication logs", ex);
            }

            // Block until the next log entry
            if( (timeout == null) || (timeout < 1) || (timeoutUnits == null) )
            {
                currEntry = src.next();
            }
            else
            {
                currEntry = src.next(timeout, timeoutUnits);
                if( currEntry == null )
                {
                    return LogCheckResult.from(LCResultStatus.TIMEDOUT);
                }
            }

            currEntryVO = LogEntry.toValueObject(currEntry);

            if( LOGGER.isDebugEnabled() )
            {
           //     LOGGER.debug(String.format("doProcess : '%s'",
            //                        LogEntryVO.toJSON(currEntryVO)));
            }

            // Check the deduplication list before putValueObj
            LogEntryDeDupe currLEDD = null;
            LCDeDupeAction currAction = LCDeDupeAction.NONE;
            if( md != null )
            {
                md.update(LogEntryVO.toJSON(currEntryVO).getBytes());
                currLEDD = LogEntryDeDupe.from(null,
                        md.digest(),
                        null,
                        null,
                        null,
                        Instant.now());

                if( currentProcessedEntries.contains(currLEDD) )
                {
                    duplicateCount++;

                    String errMsg = String.format("This Log Entry has already been added in this process() call\n"
                                + "DUPLICATE COUNT : %d\nHASH : '%s'\nJSON : %s\n",
                            duplicateCount,
                            Hex.encodeHexString(currLEDD.getLogHashCode()),
                            LogEntryVO.toJSON(currEntryVO));

                    LOGGER.info(errMsg);

                    // Define duplication rules E.g. skip or quit
                    if(deDupeDefaultAction != null)
                    {
                        currAction = deDupeDefaultAction;
                    }

                    if( (deDupeIgnoreCount != null) && (duplicateCount <= deDupeIgnoreCount) )
                    {
                        currAction = LCDeDupeAction.IGNORE;
                    }
                    else if( (deDupeSkipCount != null) && (duplicateCount <= deDupeSkipCount) )
                    {
                        currAction = LCDeDupeAction.SKIP;
                    }

                    // TODO : Check the deduplication logs on disk as well
                }

                // Register as processed
                currentProcessedEntries.add(currLEDD);
            }

            switch( currAction )
            {
                case SKIP:
                    // Skip storing the current log entry
                    LOGGER.debug("process() : SKIPPING current duplicate log entry...");
                    continue;

                case BREAK:
                    // Stop processing all together
                    LOGGER.debug("process() : BREAKING after current duplicate log entry...");
                    return LogCheckResult.from(LCResultStatus.BREAK);

                case IGNORE:
                    // Continue with updating the log stores
                    LOGGER.debug("process() : IGNORING the fact that the current log entry is a duplicate...");
                    break;
            }

            LogCheckResult putRes = null;
            for( ILogEntryStore dst : dstStores )
            {
                putRes = dst.putValueObj(currEntryVO);
            }

            logCount++;

            try
            {
                // Update deduplication statistics
                if( (currDeDupeLog != null)
                        && (currLEDD != null) )
                {
                    currDeDupeLog.getLogEntryDeDupes().add(currLEDD);
                    shouldWrite = true;

                    boolean doWrite = true;
                    if( (putRes != null)
                            && (deDupeMaxLogsBeforeWrite != null)
                            && (deDupeMaxLogsBeforeWrite > 0)
                            && ((logCount % deDupeMaxLogsBeforeWrite) != 0) )
                    {
                        doWrite = false;
                    }

                    if( doWrite )
                    {
                        // Obviously, we can't rewrite the log file on each log store.
                        // log entries stored before writing out this log is configurable.
                        LogCheckDeDupeLogWriter.write(currDeDupeLog, deDupeFileName);
                        shouldWrite = false;
                    }
                }
            }
            catch( LogCheckException ex )
            {
                LOGGER.warn("Error writing deduplication logs", ex);
            }

            if( putRes == null )
            {
                break;
            }
        }
        while( true );

        return res;
    }
}
