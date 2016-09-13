/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sludev.logs.logcheck.utils;

import com.sludev.logs.logcheck.enums.LCResultStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the state or status of a call or object.
 *
 * @author Administrator
 */
public final class LogCheckResult
{
    private static final Logger LOGGER
                      = LogManager.getLogger(LogCheckResult.class);
    
    private final List<LCResultStatus> statuses;

    public List<LCResultStatus> getStatuses()
    {
        return statuses;
    }


    private LogCheckResult(List<LCResultStatus> statuses)
    {
        if( statuses == null )
        {
            this.statuses = new ArrayList<>();
        }
        else
        {
            this.statuses = statuses;
        }
    }

    public static LogCheckResult from()
    {
        LogCheckResult res = new LogCheckResult(null);

        return res;
    }

    public static LogCheckResult from(List<LCResultStatus> statuses)
    {
        LogCheckResult res = new LogCheckResult(statuses);

        return res;
    }

    public static LogCheckResult from(LCResultStatus status)
    {
        List<LCResultStatus> statuses = new ArrayList<>();
        statuses.add(status);

        LogCheckResult res = new LogCheckResult(statuses);

        return res;
    }
}
