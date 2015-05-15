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

import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import java.util.concurrent.Callable;

/**
 *
 * @author kervin
 */
public interface ILogEntryStore extends Callable<LogCheckResult>
{
    public void init();
    public LogCheckResult put(LogEntry le) throws InterruptedException, LogCheckException;
    
    public void setMainLogEntrySource(ILogEntrySource src);
}
