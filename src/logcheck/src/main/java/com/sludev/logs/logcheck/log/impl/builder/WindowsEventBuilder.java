package com.sludev.logs.logcheck.log.impl.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.LogEntry;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple log line builder.
 *
 * Created by kervin on 10/23/2015.
 */
public class WindowsEventBuilder implements ILogEntryBuilder
{
    private final List<Pattern> ignoreLinePatternList;
    private final ILogEntrySink completionCallback;

    private WindowsEventBuilder( final List<Pattern> ignoreLinePatternList,
                                 final ILogEntrySink completionCallback)
    {
        this.completionCallback = completionCallback;
        this.ignoreLinePatternList = ignoreLinePatternList;
    }

    public static WindowsEventBuilder from( final List<Pattern> ignoreLinePatternList,
                                            final ILogEntrySink completionCallback)
    {
        WindowsEventBuilder res = new WindowsEventBuilder(ignoreLinePatternList,
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

        ObjectMapper jsonMapper = new ObjectMapper();

        JsonNode rootArray = null;

        try
        {
            rootArray = jsonMapper.readTree(currLineStr);
        }
        catch( IOException ex )
        {
            ;
        }

        String currVal = rootArray.get("id").textValue();

        LogEntry currentLogEntry = LogEntry.from("WindowsEvent");

        // setId() ??
        
        currentLogEntry.setMessage(currLineStr);

        // Marks a row end
        if(completionCallback != null)
        {
            completionCallback.put(currentLogEntry);
        }
    }
}
