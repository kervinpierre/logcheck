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
package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.tail.tailer.TailerListener;
import com.sludev.logs.logcheck.tail.tailer.Tailer;
import com.sludev.logs.logcheck.log.LogEntryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public class LogCheckTailerListener implements TailerListener
{
    private static final Logger log 
                             = LogManager.getLogger(LogCheckTailerListener.class);

    private LogEntryBuilder mainLogEntryBuilder;
    private Tailer mainTailer;

    public LogEntryBuilder getMainLogEntryBuilder()
    {
        return mainLogEntryBuilder;
    }

    public void setMainLogEntryBuilder(LogEntryBuilder m)
    {
        this.mainLogEntryBuilder = m;
    }
    
    public LogCheckTailerListener()
    {
        mainLogEntryBuilder = null;
    }
    
    @Override
    public void init(Tailer t)
    {
        log.debug("init() called.");
        
        this.mainTailer = t;
    }

    @Override
    public void fileNotFound()
    {
        log.debug("fileNotFound() called.");
    }

    @Override
    public void fileRotated()
    {
        log.debug("fileRotated() called.");
    }

    @Override
    public void handle(String str)
    {
        log.debug( String.format("handle() : '%s'\n", str));
        
        /**
         * BUG : Commons-IO 2.4 ignores InterruptedException but we should not
         */
        try
        {
            mainLogEntryBuilder.handleLogLine(str);
        }
        catch( InterruptedException ex )
        {
            log.debug("LogCheckTailerListener.handle() was interrupted.");
            
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void handle(Exception ex)
    {
        log.error("handle() Exception called.", ex);
    }
}
