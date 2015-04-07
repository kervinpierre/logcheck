/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sludev.logs.logcheck.utils;

import com.sludev.logs.logcheck.enums.LogCheckResultStatusEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class LogCheckResult
{
    private static final Logger log 
                      = LogManager.getLogger(LogCheckResult.class);
    
    private LogCheckResultStatusEnum status;

    public LogCheckResultStatusEnum getStatus()
    {
        return status;
    }

    public void setStatus(LogCheckResultStatusEnum s)
    {
        this.status = s;
    }
    
    public LogCheckResult()
    {
        status = LogCheckResultStatusEnum.NONE;
    }
}
