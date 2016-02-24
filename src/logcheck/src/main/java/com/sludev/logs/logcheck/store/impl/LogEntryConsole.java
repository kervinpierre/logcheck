package com.sludev.logs.logcheck.store.impl;

import com.sludev.logs.logcheck.config.entities.LogEntryVO;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.store.ILogEntryStore;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 'Store' log entries to the console.  This is really for debugging.
 *
 * Created by kervin on 10/20/2015.
 */
public final class LogEntryConsole implements ILogEntryStore
{
    private static final Logger log
                           = LogManager.getLogger(LogEntryConsole.class);

    private LogEntryConsole()
    {
        ;
    }

    public static LogEntryConsole from()
    {
        LogEntryConsole res = new LogEntryConsole();

        return res;
    }

    @Override
    public void init()
    {

    }

    @Override
    public void destroy()
    {

    }

    @Override
    public LCResultStatus testConnection() throws LogCheckException
    {
        return null;
    }

    @Override
    public LogCheckResult put(LogEntryVO le) throws InterruptedException, LogCheckException
    {
        LogCheckResult res
                = LogCheckResult.from(LCResultStatus.SUCCESS);

        System.out.println(LogEntryVO.toJSON(le));

        return res;
    }
}
