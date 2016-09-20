package com.sludev.logs.logcheck.log.impl.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.LogEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Simple log line builder.
 *
 * Created by kervin on 10/23/2015.
 */
public class WindowsEventBuilder implements ILogEntryBuilder
{
    private static final Logger LOGGER
                            = LogManager.getLogger(WindowsEventBuilder.class);

    private final List<Pattern> ignoreLinePatternList;
    private final ILogEntrySink completionCallback;
    private long count = 0;
    private static final LCLogEntryBuilderType type = LCLogEntryBuilderType.WINDOWS_EVENT;

    private WindowsEventBuilder( final List<Pattern> ignoreLinePatternList,
                                 final ILogEntrySink completionCallback)
    {
        this.completionCallback = completionCallback;
        this.ignoreLinePatternList = ignoreLinePatternList;
    }

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
        catch( Exception ex )
        {
            LOGGER.debug(String.format("Invalid JSON...\n========\n'%s'\n========\n", currLineStr));
        }

        LogEntry currentLogEntry = LogEntry.from("WindowsEvent");

        // source
        JsonNode currNode = rootArray.get("source");
        if( currNode != null )
        {
            currentLogEntry.setAppSource(
                    rootArray.get("source").textValue());
        }

        // statusCode
        currNode = rootArray.get("statusCode");
        if( currNode != null )
        {
            currentLogEntry.setAppStatusCode(
                    rootArray.get("statusCode").textValue());
        }

        // channel
        currNode = rootArray.get("channel");
        if( currNode != null )
        {
            currentLogEntry.setAppChannel(
                    rootArray.get("channel").textValue());
        }

        // exception
        currNode = rootArray.get("exception");
        if( currNode != null )
        {
            currentLogEntry.setException(
                    rootArray.get("exception").textValue());
        }

        // severity
        currNode = rootArray.get("severity");
        if( currNode != null )
        {
            String currLevel = null;
            try
            {
                currLevel = rootArray.get("severity").textValue();

                currentLogEntry.setLevel( currLevel );
            }
            catch( Exception ex )
            {
                LOGGER.debug(String.format("handleLogLine() : error parsing unknown Level '%s'",
                        currLevel), ex);
            }
        }

        // recordNumber
        currNode = rootArray.get("recordNumber");
        if( currNode != null )
        {
            currentLogEntry.setAppRecordNumber(
                    rootArray.get("recordNumber").textValue());
        }

        // eventId
        currNode = rootArray.get("eventId");
        if( currNode != null )
        {
            currentLogEntry.setAppEventId(
                    rootArray.get("eventId").textValue());
        }

        // timestamp
        currNode = rootArray.get("timeGenerated");
        if( currNode != null )
        {
            currentLogEntry.setAppTimeGenerated(
                    rootArray.get("timeGenerated").textValue());

            currentLogEntry.setTimeStamp(LocalDateTime.parse(
                    currentLogEntry.getAppTimeGenerated(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
        }

        // data
        currNode = rootArray.get("dataStr");
        if( currNode != null )
        {
            currentLogEntry.setAppDataStr(
                    rootArray.get("dataStr").textValue());
            currentLogEntry.setMessage(currentLogEntry.getAppDataStr());
        }

        // host
        currNode = rootArray.get("computerName");
        if( currNode != null )
        {
            currentLogEntry.setAppComputerName(
                    rootArray.get("computerName").textValue());
            currentLogEntry.setHost(currentLogEntry.getAppComputerName());
        }

        currentLogEntry.setJsonRaw(currLineStr);

        // Marks a row end
        if(completionCallback != null)
        {
            completionCallback.put(currentLogEntry);
        }
        count++;
    }
}
