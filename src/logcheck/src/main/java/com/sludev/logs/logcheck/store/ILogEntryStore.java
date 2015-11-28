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
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author kervin
 */
public interface ILogEntryStore
{
    static final Logger log = LogManager.getLogger(ILogEntryStore.class);

    public void init() throws LogCheckException;
    public void destroy() throws LogCheckException;

    public LogCheckResult put(LogEntryVO le) throws InterruptedException, LogCheckException;

    public static LogCheckResult process( final ILogEntrySource src,
                                          final List<ILogEntryStore> dstStores,
                                          final Path deDupeDirPath,
                                          final UUID runUUID,
                                          final Integer deDupeMaxLogsBeforeWrite,
                                          final Integer deDupeMaxLogsPerFile,
                                          final Integer deDupeMaxLogFiles) throws InterruptedException, LogCheckException
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);
        LogEntry currEntry;
        LogEntryVO currEntryVO;
        MessageDigest md = null;
        int logCount = 0;

        Path deDupeFileName = null;
        LogCheckDeDupeLog currDeDupeLog = null;

        try
        {
            md = MessageDigest.getInstance("SHA-256");
        }
        catch( NoSuchAlgorithmException ex )
        {
            log.debug("Failed getting SHA-256 Hash", ex);
        }

        boolean shouldWrite = false;
        do
        {
            // Setup the deduplication directory if we need it.
            // Rotate the log file after a certain number of log stores.
            if( deDupeDirPath != null
                    && (logCount == 0
                        || (deDupeMaxLogsPerFile != null
                            && deDupeMaxLogsPerFile > 0
                            && logCount % deDupeMaxLogsPerFile == 0)) )
            {
                if( shouldWrite )
                {
                    // We should write out the final log entries
                    LogCheckDeDupeLogWriter.write(currDeDupeLog, deDupeFileName);
                }

                deDupeFileName = LogCheckDeDupeLog.nextFileName(deDupeDirPath,deDupeMaxLogFiles,true);
                currDeDupeLog = LogCheckDeDupeLog.from(runUUID, Instant.now(), null);
            }

            // Block until the next log entry
            currEntry = src.next();
            currEntryVO = LogEntry.toValueObject(currEntry);

            // TODO : Check the deduplication list before put

            // TODO : Define duplication rules E.g. skip or quit

            // TODO : Have duplication list for current run, and for past runs

            LogCheckResult putRes = null;
            for( ILogEntryStore dst : dstStores )
            {
                putRes = dst.put(currEntryVO);
            }

            logCount++;

            // Update deduplication statistics
            if( currDeDupeLog != null
                    && md != null )
            {
                md.update(LogEntryVO.toJSON(currEntryVO).getBytes());
                String currHashStr = String.format("%x", new java.math.BigInteger(1, md.digest()));

                LogEntryDeDupe currLEDD = LogEntryDeDupe.from(null,
                        currHashStr,
                        null,
                        null,
                        null,
                        Instant.now());

                currDeDupeLog.getLogEntryDeDupes().add(currLEDD);
                shouldWrite = true;

                boolean doWrite = true;
                if( putRes != null
                        && deDupeMaxLogsBeforeWrite != null
                        && deDupeMaxLogsBeforeWrite > 0
                        && logCount % deDupeMaxLogsBeforeWrite != 0 )
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

            if( putRes == null )
            {
                break;
            }
        }
        while( true );

        return res;
    }
}
