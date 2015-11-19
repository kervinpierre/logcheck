/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.log.impl.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.log.ILogEntrySink;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.config.entities.LogEntryVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility for processing LogEntry and related objects.
 * 
 * @author kervin
 */
public final class MultiLineDelimitedBuilder implements ILogEntryBuilder
{
    private static final Logger log
            = LogManager.getLogger(MultiLineDelimitedBuilder.class);

    private final Pattern logRowStartPattern;
    private final Pattern logRowEndPattern;
    private final Pattern logColumnStartPattern;
    private final Pattern logColumnEndPattern;
    private final List<Pattern> logIngoreLine;
    private final StringBuilder columnString;
    private final ILogEntrySink completionCallback;

    // Mutable
    private LogEntry currentLogEntry;
    private Integer columnCount;

    private MultiLineDelimitedBuilder( final Pattern logRowStartPattern,
                                      final Pattern logRowEndPattern,
                                      final Pattern logColumnStartPattern,
                                      final Pattern logColumnEndPattern,
                                      final List<Pattern> ignoreLinePattern,
                                      final ILogEntrySink completionCallback)
    {
        this.logRowStartPattern = logRowStartPattern;
        this.logRowEndPattern = logRowEndPattern;
        this.logColumnStartPattern = logColumnStartPattern;
        this.logColumnEndPattern = logColumnEndPattern;
        this.completionCallback = completionCallback;

        if( ignoreLinePattern != null )
        {
            logIngoreLine = ignoreLinePattern;
        }
        else
        {
            logIngoreLine = new ArrayList<>();
        }

        columnString = new StringBuilder();

        this.currentLogEntry = null;
        this.columnCount = null;
    }

    public static MultiLineDelimitedBuilder from( final String logRowStartPatternStr,
                                                  final String logRowEndPatternStr,
                                                  final String logColStartPatternStr,
                                                  final String logColEndPatternStr,
                                                  final List<String> ignoreLinePatternList,
                                                  final ILogEntrySink completionCallback )
    {
        Pattern logRowStartPattern = null;
        Pattern logRowEndPattern = null;
        Pattern logColStartPattern = null;
        Pattern logColEndPattern = null;
        List<Pattern> ignoreLineList = null;

        if(StringUtils.isNoneBlank(logRowStartPatternStr))
        {
            logRowStartPattern = Pattern.compile(logRowStartPatternStr);
        }

        if(StringUtils.isNoneBlank(logRowEndPatternStr))
        {
            logRowEndPattern = Pattern.compile(logRowEndPatternStr);
        }

        if(StringUtils.isNoneBlank(logColStartPatternStr))
        {
            logColStartPattern = Pattern.compile(logColStartPatternStr);
        }

        if(StringUtils.isNoneBlank(logColEndPatternStr))
        {
            logColEndPattern = Pattern.compile(logColEndPatternStr);
        }

        if(ignoreLinePatternList != null && ignoreLinePatternList.size() > 0)
        {
            ignoreLineList = new ArrayList<>();
            for(String s : ignoreLinePatternList)
            {
                ignoreLineList.add(Pattern.compile(s));
            }
        }

        MultiLineDelimitedBuilder res = from(logRowStartPattern,
                logRowEndPattern,
                logColStartPattern,
                logColEndPattern,
                ignoreLineList,
                completionCallback);

        return res;
    }

    public static MultiLineDelimitedBuilder from( final Pattern logRowStartPattern,
                                                   final Pattern logRowEndPattern,
                                                   final Pattern logColumnStartPattern,
                                                   final Pattern logColumnEndPattern,
                                                  final List<Pattern> ignoreLinePatternList,
                                                   final ILogEntrySink completionCallback )
    {
        MultiLineDelimitedBuilder res = new MultiLineDelimitedBuilder(logRowStartPattern,
                logRowEndPattern,
                logColumnStartPattern,
                logColumnEndPattern,
                ignoreLinePatternList,
                completionCallback);

        return res;
    }

    /**
     * Process a new line from the log file.
     *
     * @param currLineStr The line to be processed into the current log entry.
     * @throws java.lang.InterruptedException
     */
    @Override
    public void handleLogLine(String currLineStr) throws InterruptedException
    {
        if( ILogEntryBuilder.ignoreLine(logIngoreLine, currLineStr) )
        {
            return;
        }

        if(logRowStartPattern != null
                && logRowStartPattern.matcher(currLineStr).matches())
        {
            currentLogEntry = LogEntry.from(null);
            columnString.setLength(0);
            columnCount = 0;

            // Marks a row start
            Matcher currM = logRowStartPattern.matcher(currLineStr);
            currM.matches();
            int currGC = currM.groupCount();
            if(currGC > 0)
            {
                // Use the row start pattern for the first columns
                currentLogEntry.setLevel(currM.group(1));
                currentLogEntry.setTimeStamp(currM.group(2));
                currentLogEntry.setLogger(currM.group(3));
                currentLogEntry.setHost(currM.group(4));
            }
        }
        else if(currentLogEntry != null
                && logColumnEndPattern != null
                && logColumnEndPattern.matcher(currLineStr).matches())
        {
            // Marks a column end
            switch(columnCount)
            {
                case 0:
                    currentLogEntry.setMessage(columnString.toString());
                    break;

                case 1:
                    currentLogEntry.setException(columnString.toString());
                    break;

                default:
                    log.error(String.format("Error : Unexpected column '%s'\n",
                            columnString.toString()));
                    break;
            }

            columnString.setLength(0);
            columnCount++;
        }
        else if(currentLogEntry != null
                && logRowEndPattern != null
                && logRowEndPattern.matcher(currLineStr).matches())
        {
            if(columnString.length() > 0)
            {
                // We end the final column with the row marker only
                switch(columnCount)
                {
                    case 0:
                        currentLogEntry.setMessage(columnString.toString());
                        break;

                    case 1:
                        currentLogEntry.setException(columnString.toString());
                        break;

                    default:
                        log.error(String.format("Error : Unexpected column '%s'\n",
                                columnString.toString()));
                        break;
                }
            }

            log.debug(String.format("Completed log\n====\n%s\n====\n",
                    LogEntryVO.toJSON(
                            LogEntry.toValueObject(currentLogEntry))));

            // Marks a row end
            if(completionCallback != null)
            {
                completionCallback.put(currentLogEntry);
            }

            currentLogEntry = null;
        }
        else
        {
            // Store as column data
            columnString.append(currLineStr);
            columnString.append("\n");
        }
    }
}
