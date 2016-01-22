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
package com.sludev.logs.logcheck.log.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;

import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for queuing outgoing log entries.  This implementation uses a blocking queue
 * for queuing between threads.
 * 
 * @author kervin
 */
public final class LogEntryQueueSink implements ILogEntrySink
{
    private static final Logger LOGGER
                             = LogManager.getLogger(LogEntryQueueSink.class);
    
    /**
     * TODO : The 'Completed Entries' queue should be serialized on disk in case of an
     * application crash. Maybe http://www.mapdb.org/ helpful
     */
    private final BlockingDeque<LogEntry> m_completedLogEntries;

    private final Set<LogEntry> m_currentProcessedEntries;

    /**
     * TODO : Logs should not be duplicated for this duration.
     * 
     * TODO : Duplication check should be allowed to ignore specified fields, 
     *         e.g. timestamp or exception stack or id fields.
     */
    private final Duration m_logDeduplicationDuration;
    
    /**
     * TODO : The date that should be ignored when processing old logs
     */
    private final LocalTime logCutoffDate;
    
    /**
     * TODO : Do not process logs this duration in the past
     */
    private final Duration logCutoffDuration;
    
    /**
     * TODO : Use SHA256 for log deduplication
     */
    private final MessageDigest mainDigest;
    
    /**
     * Log Entry SHA256 hash and the last time an enqueue was attempted.
     */
    private PassiveExpiringMap<Byte[],LocalTime> sessionHashes;

    private LogEntryQueueSink( final BlockingDeque<LogEntry> completedLogEntries,
                               final Duration logDeduplicationDuration,
                               final LocalTime logCutoffDate,
                               final Duration logCutoffDuration,
                               final PassiveExpiringMap<Byte[], LocalTime> sessionHashes) throws LogCheckException
    {
        this.m_completedLogEntries = completedLogEntries;
        this.m_logDeduplicationDuration = logDeduplicationDuration;
        this.logCutoffDate = logCutoffDate;
        this.logCutoffDuration = logCutoffDuration;
        this.sessionHashes = sessionHashes;
        this.m_currentProcessedEntries = new HashSet<>(10);

        MessageDigest tempDig = null;
        try
        {
            tempDig  = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException ex)
        {
            LOGGER.debug("Error creating 'SHA-256' digest", ex);

            throw new LogCheckException("Error creating 'SHA-256' digest", ex);
        }

        mainDigest = tempDig;
    }

    public static LogEntryQueueSink from( final BlockingDeque<LogEntry> completedLogEntries,
                               final Duration logDeduplicationDuration,
                               final LocalTime logCutoffDate,
                               final Duration logCutoffDuration,
                               final PassiveExpiringMap<Byte[], LocalTime> sessionHashes) throws LogCheckException
    {
        LogEntryQueueSink res = new LogEntryQueueSink(completedLogEntries,
                logDeduplicationDuration,
                logCutoffDate,
                logCutoffDuration,
                sessionHashes);

        return res;
    }

    /**
     * Put a log entry in this class store.
     * 
     * @param le Log entry to store
     * @throws java.lang.InterruptedException
     */
    @Override
    public void put(LogEntry le) throws InterruptedException
    {
        if( validate(le) == false )
        {
            return;
        }

        if( m_currentProcessedEntries.contains(le) )
        {
            LOGGER.debug("Adding a Log Entry that already exists '%s'", le);
        }
        else
        {
            m_currentProcessedEntries.add(le);
        }

        m_completedLogEntries.put(le);
    }

    /**
     * Check if a supplied LogEntry will be allowed in the Sink.
     * 
     * @param le
     * @return
     */
    @Override
    public boolean validate(LogEntry le)
    {
        boolean res = true;
        
        // Use the crypto-hash of the Value Object for deduplication
        // There are probably many other ways to do this.
        // TODO  : Investigate other hashing approaches

        return res;
    }
}
