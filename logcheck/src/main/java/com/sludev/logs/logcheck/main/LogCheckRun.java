/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sludev.logs.logcheck.main;

import com.sludev.logs.logcheck.config.LogCheckConfig;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import java.util.concurrent.Callable;

/**
 *
 * @author Administrator
 */
public class LogCheckRun implements Callable<LogCheckResult>
{
    private LogCheckConfig config;

    public LogCheckConfig getConfig()
    {
        return config;
    }

    public void setConfig(LogCheckConfig config)
    {
        this.config = config;
    }

    @Override
    public LogCheckResult call() throws Exception
    {
        LogCheckResult res = new LogCheckResult();
        
        return res;
    }
    
}
