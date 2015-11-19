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
    private static final Logger log 
                             = LogManager.getLogger(LogEntryQueueSource.class);
    
    private final BlockingDeque<LogEntry> completedLogEntries;

    private LogEntryQueueSource(final BlockingDeque<LogEntry> completedLogEntries)
    {
        this.completedLogEntries = completedLogEntries;
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
            currLE = completedLogEntries.take();
        }
        
        return currLE;
    }
    
}
