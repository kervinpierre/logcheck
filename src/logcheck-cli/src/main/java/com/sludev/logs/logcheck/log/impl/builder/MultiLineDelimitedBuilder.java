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

import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
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
    private static final Logger LOGGER
            = LogManager.getLogger(MultiLineDelimitedBuilder.class);

    private final Pattern m_logRowStartPattern;
    private final Pattern m_logRowEndPattern;
    private final Pattern m_logColumnStartPattern;
    private final Pattern m_logColumnEndPattern;
    private final List<Pattern> m_logIngoreLine;
    private final StringBuilder m_columnStrBuild;
    private final ILogEntrySink m_completionCallback;

    private static final LCLogEntryBuilderType type = LCLogEntryBuilderType.MULTILINE_DELIMITED;

    // Mutable
    private LogEntry m_currentLogEntry;
    private Integer m_columnCount;
    private long count = 0;

    private MultiLineDelimitedBuilder( final Pattern logRowStartPattern,
                                      final Pattern logRowEndPattern,
                                      final Pattern logColumnStartPattern,
                                      final Pattern logColumnEndPattern,
                                      final List<Pattern> ignoreLinePattern,
                                      final ILogEntrySink completionCallback)
    {
        this.m_logRowStartPattern = logRowStartPattern;
        this.m_logRowEndPattern = logRowEndPattern;
        this.m_logColumnStartPattern = logColumnStartPattern;
        this.m_logColumnEndPattern = logColumnEndPattern;
        this.m_completionCallback = completionCallback;

        if( ignoreLinePattern != null )
        {
            m_logIngoreLine = ignoreLinePattern;
        }
        else
        {
            m_logIngoreLine = new ArrayList<>();
        }

        m_columnStrBuild = new StringBuilder();

        this.m_currentLogEntry = null;
        this.m_columnCount = null;
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

        if( (ignoreLinePatternList != null) && (ignoreLinePatternList.size() > 0) )
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
        if( ILogEntryBuilder.ignoreLine(m_logIngoreLine, currLineStr) )
        {
            return;
        }

        if( (m_logRowStartPattern != null)
                && m_logRowStartPattern.matcher(currLineStr).matches() )
        {
            m_currentLogEntry = LogEntry.from(null);
            m_columnStrBuild.setLength(0);
            m_columnCount = 0;

            // Marks a row start
            Matcher currM = m_logRowStartPattern.matcher(currLineStr);
            currM.matches();
            int currGC = currM.groupCount();
            if(currGC > 0)
            {
                // Use the row start pattern for the first columns
                m_currentLogEntry.setLevel(currM.group(1));
                m_currentLogEntry.setTimeStamp(currM.group(2));
                m_currentLogEntry.setLogger(currM.group(3));
                m_currentLogEntry.setHost(currM.group(4));
            }
        }
        else if( (m_currentLogEntry != null)
                && (m_logColumnEndPattern != null)
                && m_logColumnEndPattern.matcher(currLineStr).matches() )
        {
            // Marks a column end
            switch( m_columnCount )
            {
                case 0:
                    m_currentLogEntry.setMessage(m_columnStrBuild.toString());
                    break;

                case 1:
                    m_currentLogEntry.setException(m_columnStrBuild.toString());
                    break;

                default:
                    LOGGER.error(String.format("Error : Unexpected column '%s'\n",
                            m_columnStrBuild.toString()));
                    break;
            }

            m_columnStrBuild.setLength(0);
            m_columnCount++;
        }
        else if( (m_currentLogEntry != null)
                && (m_logRowEndPattern != null)
                && m_logRowEndPattern.matcher(currLineStr).matches() )
        {
            if( m_columnStrBuild.length() > 0)
            {
                // We end the final column with the row marker only
                switch( m_columnCount )
                {
                    case 0:
                        m_currentLogEntry.setMessage(m_columnStrBuild.toString());
                        break;

                    case 1:
                        m_currentLogEntry.setException(m_columnStrBuild.toString());
                        break;

                    default:
                        LOGGER.error(String.format("Error : Unexpected column '%s'\n",
                                m_columnStrBuild.toString()));
                        break;
                }
            }

            if( LOGGER.isDebugEnabled() )
            {
                String msg = "";
                if( StringUtils.isNoneBlank(m_currentLogEntry.getMessage()) )
                {
                    msg = m_currentLogEntry.getMessage().replaceAll("[\\s]+", " ");
                    msg = StringUtils.substring(msg, 0, 50);
                }

                LOGGER.debug(String.format("Completed log : TimeStamp [[%s][%s]]",
                        m_currentLogEntry.getTimeStamp(), msg));
            }

//            LOGGER.debug(String.format("Completed log\n====\n%s\n====\n",
//                    LogEntryVO.toJSON(
//                            LogEntry.toValueObject(m_currentLogEntry))));

            // Marks a row end
            if( m_completionCallback != null)
            {
                m_completionCallback.put(m_currentLogEntry);
                count++;
            }

            m_currentLogEntry = null;
        }
        else
        {
            // Store as column data
            m_columnStrBuild.append(currLineStr);
            m_columnStrBuild.append("\n");
        }
    }
}
