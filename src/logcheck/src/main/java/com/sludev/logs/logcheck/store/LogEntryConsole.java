package com.sludev.logs.logcheck.store;

import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.model.LogEntry;
import com.sludev.logs.logcheck.model.LogEntryVO;
import com.sludev.logs.logcheck.utils.LogCheckException;
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

    private final ILogEntrySource mainLogEntrySource;

    public ILogEntrySource getMainLogEntrySource()
    {
        return mainLogEntrySource;
    }

    private LogEntryConsole(ILogEntrySource mainLogEntrySource)
    {
        this.mainLogEntrySource = mainLogEntrySource;
    }

    public static LogEntryConsole from(ILogEntrySource mainLogEntrySource)
    {
        LogEntryConsole res = new LogEntryConsole(mainLogEntrySource);

        return res;
    }

    @Override
    public void init()
    {

    }

    @Override
    public LogCheckResult put(LogEntry le) throws InterruptedException, LogCheckException
    {
        LogCheckResult res
                = LogCheckResult.from(LCResultStatus.SUCCESS);

        System.out.println(LogEntryVO.toJSON(LogEntry.toValueObject(le)));

        return res;
    }

    @Override
    public LogCheckResult call() throws Exception
    {
        LogCheckResult res;

        res = ILogEntryStore.process(mainLogEntrySource, this);

        return res;
    }
}
