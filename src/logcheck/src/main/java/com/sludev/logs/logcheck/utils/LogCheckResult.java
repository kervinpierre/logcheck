/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sludev.logs.logcheck.utils;

import com.sludev.logs.logcheck.enums.LCResultStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents the state or status of a call or object.
 *
 * @author Administrator
 */
public final class LogCheckResult
{
    private static final Logger log 
                      = LogManager.getLogger(LogCheckResult.class);
    
    private final LCResultStatus status;

    public LCResultStatus getStatus()
    {
        return status;
    }
    
    private LogCheckResult(LCResultStatus status)
    {
        this.status = status;
    }

    public static LogCheckResult from(LCResultStatus status)
    {
        LogCheckResult res = new LogCheckResult(status);

        return res;
    }
}
