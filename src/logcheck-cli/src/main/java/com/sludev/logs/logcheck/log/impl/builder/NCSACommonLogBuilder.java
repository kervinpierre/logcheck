package com.sludev.logs.logcheck.log.impl.builder;

import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.LogEntry;

import java.util.List;
import java.util.regex.Pattern;

/**
 * NCSA Common log format.
 *
 * https://en.wikipedia.org/wiki/Common_Log_Format
 *
 * Created by Kervin on 10/23/2015.
 */
public class NCSACommonLogBuilder implements ILogEntryBuilder
{
    private final List<Pattern> ignoreLinePatternList;
    private final ILogEntrySink completionCallback;

    private long count = 0;
    private static final LCLogEntryBuilderType type = LCLogEntryBuilderType.NCSA_COMMON_LOG;

    @Override
    public LCLogEntryBuilderType getType()
    {
        return type;
    }

    @Override
    public Long getCount()
    {
        return count;
    }

    private NCSACommonLogBuilder(final List<Pattern> ignoreLinePatternList,
                                 final ILogEntrySink completionCallback)
    {
        this.completionCallback = completionCallback;
        this.ignoreLinePatternList = ignoreLinePatternList;
    }

    public static NCSACommonLogBuilder from(final List<Pattern> ignoreLinePatternList,
                                         final ILogEntrySink completionCallback)
    {
        NCSACommonLogBuilder res = new NCSACommonLogBuilder(ignoreLinePatternList,
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

        LogEntry currentLogEntry = null;

        // Parse line to 'LogEntry'

        // Call completion callback
        ;

        // Marks a row end
        if(completionCallback != null)
        {
            completionCallback.put(currentLogEntry);
            count++;
        }
    }
}
