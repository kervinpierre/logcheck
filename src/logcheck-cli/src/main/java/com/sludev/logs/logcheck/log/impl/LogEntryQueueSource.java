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

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public final class LogEntryQueueSource implements ILogEntrySource
{
    private static final Logger LOGGER
                             = LogManager.getLogger(LogEntryQueueSource.class);
    
    private final BlockingDeque<LogEntry> m_completedLogEntries;

    private LogEntryQueueSource(final BlockingDeque<LogEntry> completedLogEntries)
    {
        this.m_completedLogEntries = completedLogEntries;
    }

    public static LogEntryQueueSource from(final BlockingDeque<LogEntry> completedLogEntries)
    {
        LogEntryQueueSource res = new LogEntryQueueSource(completedLogEntries);

        return res;
    }

    /**
     * Get a log entry from this class store.  Blocks until one is available.
     * 
     * @return Returns the log entry or blocks.
     * @throws java.lang.InterruptedException
     */
    @Override
    public LogEntry next() throws InterruptedException
    {
        LogEntry currLE = null;

        while( currLE == null )
        {
            currLE = next(1, TimeUnit.MINUTES);
        }
        
        return currLE;
    }

    @Override
    public LogEntry next( final long timeout,
                          final TimeUnit unit ) throws InterruptedException
    {
        LogEntry currLE = null;

        currLE = m_completedLogEntries.poll(timeout, unit);

        return currLE;
    }
    
}
