package com.sludev.logs.logcheck.log.impl.builder;

import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.model.LogEntry;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple log line builder.
 *
 * Created by kervin on 10/23/2015.
 */
public class SingleLineBuilder implements ILogEntryBuilder
{
    private final List<Pattern> ignoreLinePatternList;
    private final ILogEntrySink completionCallback;

    private SingleLineBuilder(final List<Pattern> ignoreLinePatternList,
                              final ILogEntrySink completionCallback)
    {
        this.completionCallback = completionCallback;
        this.ignoreLinePatternList = ignoreLinePatternList;
    }

    public static SingleLineBuilder from(final List<Pattern> ignoreLinePatternList,
                                         final ILogEntrySink completionCallback)
    {
        SingleLineBuilder res = new SingleLineBuilder(ignoreLinePatternList,
                                                        completionCallback);

        return res;
    }

    @Override
    public void handleLogLine(String currLineStr) throws InterruptedException
    {
        if( ILogEntryBuilder.ignoreLine(ignoreLinePatternList, currLineStr) )
        {
            return;
        }

        LogEntry currentLogEntry = LogEntry.from(null);

        currentLogEntry.setMessage(currLineStr);

        // Marks a row end
        if(completionCallback != null)
        {
            completionCallback.put(currentLogEntry);
        }
    }
}
