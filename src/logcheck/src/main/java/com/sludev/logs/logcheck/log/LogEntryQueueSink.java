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
package com.sludev.logs.logcheck.log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.BlockingDeque;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for queuing outgoing log entries.  This implementation uses a blocking queue
 * for queuing between threads.
 * 
 * @author kervin
 */
public class LogEntryQueueSink implements ILogEntrySink
{
    private static final Logger log 
                             = LogManager.getLogger(LogEntryQueueSink.class);
    
    /**
     * TODO : The 'Completed Entries' queue should be serialized on disk in case of an
     * application crash. Maybe http://www.mapdb.org/ helpful
     */
    private BlockingDeque<LogEntry> completedLogEntries;
    
    /**
     * TODO : Logs should not be duplicated for this duration.
     * 
     * TODO : Duplication check should be allowed to ignore specified fields, 
     *         e.g. timestamp or exception stack or id fields.
     */
    private Duration logDeduplicationDuration;
    
    /**
     * TODO : The date that should be ignored when processing old logs
     */
    private LocalTime logCutoffDate;
    
    /**
     * TODO : Do not process logs this duration in the past
     */
    private Duration logCutoffDuration;
    
    /**
     * TODO : Use SHA256 for log deduplication
     */
    private MessageDigest mainDigest;
    
    /**
     * Log Entry SHA256 hash and the last time an enqueue was attempted.
     */
    private PassiveExpiringMap<Byte[],LocalTime> sessionHashes;
    

    /**
     *
     * @param l
     */
    @Override
    public void setLogCutoffDate(LocalTime l)
    {
        this.logCutoffDate = l;
    }

    /**
     *
     * @return
     */
    @Override
    public Duration getLogCutoffDuration()
    {
        return logCutoffDuration;
    }

    /**
     *
     * @param l
     */
    @Override
    public void setLogCutoffDuration(Duration l)
    {
        this.logCutoffDuration = l;
    }

    public BlockingDeque<LogEntry> getCompletedLogEntries()
    {
        return completedLogEntries;
    }

    @Override
    public void setCompletedLogEntries(BlockingDeque<LogEntry> c)
    {
        this.completedLogEntries = c;
    }
       
    public void LogEntryQueueSink()
    {
        try
        {
            mainDigest = MessageDigest.getInstance("SHA256");
        }
        catch (NoSuchAlgorithmException ex)
        {
            mainDigest = null;
            log.error("Error creating 'SHA256' digest", ex);
        }
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
        
        completedLogEntries.put(le);
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
        LogEntryVO currVO = LogEntryBuilder.logEntry2VO(le);
        String voStr = LogEntryBuilder.vo2JS(currVO);
        
       // byte[] currHash = mainDigest.digest(voStr.getBytes());
        
        return res;
    }

    @Override
    public Duration getLogDeduplicationDuration()
    {
        return logDeduplicationDuration;
    }

    @Override
    public void setLogDeduplicationDuration(Duration d)
    {
        logDeduplicationDuration = d;
    }

    @Override
    public LocalTime getLogCutoffDate()
    {
        return logCutoffDate;
    }
    
}
