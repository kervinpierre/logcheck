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

import java.util.concurrent.BlockingDeque;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public class LogEntryQueueSink implements ILogEntrySink
{
    private static final Logger log 
                             = LogManager.getLogger(LogEntryQueueSink.class);
    
    private BlockingDeque<LogEntry> completedLogEntries;

    public BlockingDeque<LogEntry> getCompletedLogEntries()
    {
        return completedLogEntries;
    }

    @Override
    public void setCompletedLogEntries(BlockingDeque<LogEntry> c)
    {
        this.completedLogEntries = c;
    }
        
    /**
     * Put a log entry in this class store.
     * 
     * @param le Log entry to store
     */
    @Override
    public void put(LogEntry le)
    {
        try
        {
            completedLogEntries.put(le);
        }
        catch (InterruptedException ex)
        {
            log.debug("put() : interrupted.", ex);
        }
    }
    
}
