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
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 *
 * @author kervin
 */
public interface ILogEntryStore
{
    static final Logger log = LogManager.getLogger(ILogEntryStore.class);

    public void init();
    public ILogEntrySource getMainLogEntrySource();
    public LogCheckResult put(LogEntryVO le) throws InterruptedException, LogCheckException;

    public static LogCheckResult process( final ILogEntrySource src,
                                          final ILogEntryStore dst,
                                          final Path deDupeDirPath,
                                          final UUID runUUID ) throws InterruptedException, LogCheckException
    {
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);
        LogEntry currEntry;
        LogEntryVO currEntryVO;
        MessageDigest md = null;
        int logCount = 0;

        Path deDupeFileName = null;
        LogCheckDeDupeLog currDeDupeLog = null;

        if( deDupeDirPath != null )
        {
            deDupeFileName = LogCheckDeDupeLog.nextFileName(deDupeDirPath);
            currDeDupeLog = LogCheckDeDupeLog.from(runUUID, Instant.now(), null);
            try
            {
                md = MessageDigest.getInstance("SHA-256");
            }
            catch( NoSuchAlgorithmException ex )
            {
                log.debug("Failed getting SHA-256 Hash", ex);
            }
        }

        while(true)
        {
            // Block until the next log entry
            currEntry = src.next();
            currEntryVO = LogEntry.toValueObject(currEntry);

            LogCheckResult putRes = dst.put(currEntryVO);

            logCount++;

            // TODO : Make sure each run has its own deduplication log subdirectory.

            //  FIXME : Rotate the log file after a certain number of log stores.

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

                // Log here
                // FIXME : Obviously, we can't rewrite the log file on each log store.

                // FIXME : Make the number of log entries stored before writing out this log configurable.
                LogCheckDeDupeLogWriter.write(currDeDupeLog, deDupeFileName);
            }

            if( putRes == null )
            {
                break;
            }
        }

        return res;
    }
}
