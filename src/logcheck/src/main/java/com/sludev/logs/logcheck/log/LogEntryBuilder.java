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
package com.sludev.logs.logcheck.log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public class LogEntryBuilder
{

    private static final Logger log
            = LogManager.getLogger(LogEntryBuilder.class);

    private LogEntry currentLogEntry;
    private boolean multiLine;
    private Pattern logRowStartPattern;
    private Pattern logRowEndPattern;
    private Pattern logColumnStartPattern;
    private Pattern logColumnEndPattern;
    private List<Pattern> logIngoreLine;
    private final StringBuilder columnString;
    private int columnCount;
    private ILogEntrySink completionCallback;

    public ILogEntrySink getCompletionCallback()
    {
        return completionCallback;
    }

    /**
     * Stores the log entry store used to keep completed log entries.
     * 
     * @param c Newly completed log entry.
     */
    public void setCompletionCallback(ILogEntrySink c)
    {
        this.completionCallback = c;
    }

    public Pattern getLogRowStartPattern()
    {
        return logRowStartPattern;
    }

    public void setLogRowStartPattern(Pattern p)
    {
        this.logRowStartPattern = p;
    }

    public final void setLogRowStartPattern(String s)
    {
        Pattern p = Pattern.compile(s);

        this.logRowStartPattern = p;
    }

    public Pattern getLogRowEndPattern()
    {
        return logRowEndPattern;
    }

    public void setLogRowEndPattern(Pattern p)
    {
        this.logRowEndPattern = p;
    }

    public final void setLogRowEndPattern(String s)
    {
        Pattern p = Pattern.compile(s);

        this.logRowEndPattern = p;
    }

    public Pattern getLogColumnStartPattern()
    {
        return logColumnStartPattern;
    }

    public void setLogColumnStartPattern(Pattern p)
    {
        this.logColumnStartPattern = p;
    }

    public void setLogColumnStartPattern(String s)
    {
        Pattern p = Pattern.compile(s);

        this.logColumnStartPattern = p;
    }

    public Pattern getLogColumnEndPattern()
    {
        return logColumnEndPattern;
    }

    public void setLogColumnEndPattern(Pattern p)
    {
        this.logColumnEndPattern = p;
    }

    public final void setLogColumnEndPattern(String s)
    {
        Pattern p = Pattern.compile(s);

        this.logColumnEndPattern = p;
    }

    public List<Pattern> getLogIngoreLine()
    {
        return logIngoreLine;
    }

    public void setLogIngoreLine(List<Pattern> pl)
    {
        this.logIngoreLine = pl;
    }

    /**
     * Specifies if the builder is handling a current multiline formated log
     * file.
     *
     * @return
     */
    public boolean isMultiLine()
    {
        return multiLine;
    }

    public void setMultiLine(boolean m)
    {
        this.multiLine = m;
    }

    /**
     * The log entry object being built at the moment.
     *
     * @return
     */
    public LogEntry getCurrentLogEntry()
    {
        return currentLogEntry;
    }

    public LogEntryBuilder()
    {
        currentLogEntry = null;
        multiLine = true;
        logIngoreLine = new ArrayList<>();
        columnString = new StringBuilder();
        columnCount = 0;

        setLogRowStartPattern("^[\\s]*([A-Z]+)[\\s]+\\[([\\p{Alnum}:,\\s-]+)\\][\\s]+([\\p{Alnum}\\.]+)[\\s]*$");
        setLogColumnEndPattern("^\\[logging column end\\]$");
        setLogRowEndPattern("^\\[logging row end\\]$");

        // Ignore log lines comprising of 5 equal signs or more.
        // That's probably a border or margin of some sort.
        addLogIgnoreLine("^[=]{5,}$");
    }

    public final void addLogIgnoreLine(String s)
    {
        Pattern p = Pattern.compile(s);

        logIngoreLine.add(p);
    }

    /**
     * Create a new LogEntry Value Object from an existing LogEntry object.
     *
     * @param le
     * @return The new value object
     */
    public static LogEntryVO logEntry2VO(LogEntry le)
    {
        LogEntryVO res = new LogEntryVO();

        res.host      = StringUtils.defaultIfBlank(le.getHost(), "");
        res.exception = StringUtils.defaultIfBlank(le.getException(), "");
        res.level     = StringUtils.defaultIfBlank(le.getLevel().toString(), "");
        res.logger    = StringUtils.defaultIfBlank(le.getLogger(), "");
        res.message   = StringUtils.defaultIfBlank(le.getMessage(), "");
        res.timeStamp = StringUtils.defaultIfBlank(le.getTimeStamp().toString(), "");
        res.type      = StringUtils.defaultIfBlank(le.getType(), "");
        
        return res;
    }

    public static String vo2JS(LogEntryVO vo)
    {
        String res;
        StringBuilder js = new StringBuilder();
        
        js.append("{");
        js.append( String.format("\"@timestamp\":\"%s\",", vo.timeStamp) );
        js.append( String.format("\"level\":\"%s\",", 
                                 StringEscapeUtils.escapeJson(vo.level)) );
        js.append( String.format("\"type\":\"%s\",", 
                                 StringEscapeUtils.escapeJson(vo.type)) );
        js.append( String.format("\"logger\":\"%s\",", 
                                 StringEscapeUtils.escapeJson(vo.logger)) );
        js.append( String.format("\"host\":\"%s\",", 
                                 StringEscapeUtils.escapeJson(vo.host)) );
        js.append( String.format("\"message\":\"%s\",", 
                                 StringEscapeUtils.escapeJson(vo.message)) );
        js.append( String.format("\"exception\":\"%s\"", 
                                 StringEscapeUtils.escapeJson(vo.exception)) );
        js.append("}");
        
        res = js.toString();
        
        return res;
    }

    /**
     * Process a new line from the log file.
     *
     * @param currLineStr The line to be processed into the current log entry.
     */
    public void handleLogLine(String currLineStr)
    {
        for (Pattern pt : logIngoreLine)
        {
            if (pt.matcher(currLineStr).matches())
            {
                // Ignore this line
                return;
            }
        }

        if (logRowStartPattern != null
                && logRowStartPattern.matcher(currLineStr).matches())
        {
            currentLogEntry = new LogEntry();
            columnString.setLength(0);
            columnCount = 0;

            // Marks a row start
            Matcher currM = logRowStartPattern.matcher(currLineStr);
            currM.matches();
            int currGC = currM.groupCount();
            if (currGC > 0)
            {
                // Use the row start pattern for the first columns
                currentLogEntry.setLevel(currM.group(1));
                currentLogEntry.setTimeStamp(currM.group(2));
                currentLogEntry.setLogger(currM.group(3));
            }
        }
        else if (currentLogEntry != null
                && logColumnEndPattern != null
                && logColumnEndPattern.matcher(currLineStr).matches())
        {
            // Marks a column end
            switch (columnCount)
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
        else if (currentLogEntry != null
                && logRowEndPattern != null
                && logRowEndPattern.matcher(currLineStr).matches())
        {
            if( columnString.length() > 0 )
            {
                // We end the final column with the row marker only
                switch (columnCount)
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
                                    vo2JS(logEntry2VO(currentLogEntry))));
            
            // Marks a row end
            if( completionCallback != null )
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
