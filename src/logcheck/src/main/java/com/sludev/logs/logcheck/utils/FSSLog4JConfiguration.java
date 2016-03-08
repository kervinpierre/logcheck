/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2016] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * We don't rely on log4j configuration files in the agent.  This class instead
 * configures log4j programatically.
 * 
 * @author Kervin Pierre <info@sludev.com>
 */
public class FSSLog4JConfiguration extends AbstractConfiguration
{
    private static final Logger LOGGER
            = LogManager.getLogger(FSSLog4JConfiguration.class);

    /**
     */
    private static final String PATTERN = "%d | %p | %c\n%m%n";
    private static final String ROOTLOGGER = "com.sludev";
    private static final Level DEFAULT_LEVEL = Level.WARN;
    
    /**
     */
    public FSSLog4JConfiguration() 
    {
        super(ConfigurationSource.NULL_SOURCE);
        super.setName("FSLog4JConfiguration");
    }

    /**
     * @see org.apache.logging.log4j.core.config.AbstractConfiguration#doConfigure()
     */
    @Override
    protected void doConfigure() 
    {
        String levelStr = System.getProperty(LogCheckConstants.CMDLINE_VERBOSITY_PROPERTY);
        String outRedirectStr = System.getProperty(LogCheckConstants.CMDLINE_OUTREDIRECT_PROPERTY);
        
        Level currLevel = DEFAULT_LEVEL;
        Path currOutDirect = null;
        
        if( StringUtils.isNotBlank(levelStr) )
        {
            currLevel = Level.toLevel(levelStr, currLevel);
        }
        
        if( StringUtils.isNotBlank(outRedirectStr) )
        {
            // Redirect IO here before command line args are parsed to catch
            // early logging.
            currOutDirect = Paths.get(outRedirectStr);
            try
            {
                outputRedirect(currOutDirect);
            } 
            catch (LogCheckException ex)
            {
                // Unfortunately we can't even log yet.
            }
        }
        
        final Layout<? extends Serializable> layout = PatternLayout.newBuilder().withPattern(PATTERN).build();
        final Appender appender =
            ConsoleAppender.createAppender(layout, null, ConsoleAppender.Target.SYSTEM_OUT, "CONSOLE", true, true);
        appender.start();
        
        super.addAppender(appender);

        final LoggerConfig logger01 = new LoggerConfig(
                            FSSLog4JConfiguration.ROOTLOGGER, currLevel, false);
        
        // This appender is ok to use with ALL levels
        logger01.addAppender(appender, Level.ALL, null);
        super.addLogger(logger01.getName(), logger01);

        final LoggerConfig root = super.getRootLogger();
        root.addAppender(appender, Level.ALL, null);
        root.setLevel(currLevel);
    }
    
    /**
     * Set a new Log Level on the root logger.
     * 
     * Useful for changing the default "ALL" logging level to something more
     * restrictive.
     * 
     * @param l The new Root Logger log level.
     */
    public static void setLevel(Level l)
    {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        
        LoggerConfig loggerConfig = config.getLoggerConfig(ROOTLOGGER);   
        loggerConfig.setLevel(l);
        ctx.updateLoggers();
        
        loggerConfig = config.getLoggerConfig("");   
        loggerConfig.setLevel(l);
        ctx.updateLoggers();
    }

    /**
     *  Set the application's verbosity level.  Prior to this call here, it's
     *  set to WARN by default in FSSLog4JConfiguration, or by the -DFSSVERBOSITY
     * java command line property.
     *
     *  --verbose has the top precedence.  Using this argument to set the
     *      verbosity on any command run.
     *
     *  -DFSSVERBOSITY can be used as a default system would verbosity.  Since
     *      it sets the logger's verbosity from the start, you should set it so
     *      that you do not lose any early messages.
     *
     * To be sure of catching all logging messages, set both flags with the same
     * value.
     *
     * Values can be ALL, DEBUG, INFO, WARN, ERROR
     *
     * @param verbosity The verbosity level for logging.
     */
    public static void setVerbosity( FSSVerbosityEnum verbosity )
    {
        Level currLevel = null;

        if( verbosity == null )
        {
            LOGGER.info( String.format("Verbosity should not be null"));

            return;
        }

        switch(verbosity)
        {
            case MAXIMUM:
                currLevel = Level.ALL;
                break;

            case MINIMUM:
                currLevel = Level.ERROR;
                break;

            case NONE:
                currLevel = Level.OFF;
                break;

            default:
                currLevel = Level.toLevel(verbosity.toString());
                break;
        }

        if( currLevel != null )
        {
            FSSLog4JConfiguration.setLevel(currLevel);
        }
    }

    /**
     * Duplicate standard output streams and send to a file.
     *
     * @param f File path to send output to.
     */
    public static void outputRedirect(Path f) throws LogCheckException
    {
        if( f == null )
        {
            throw new LogCheckException("Output file cannot be null.");
        }

        OutputStream currFileStream;

        try
        {
            currFileStream = Files.newOutputStream(f, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        }
        catch (IOException ex)
        {
            String err = String.format("Failed opening standard output file '%s'\n", f);

            LOGGER.error( err, ex);
            throw new LogCheckException(err, ex);
        }

        if ( currFileStream == null )
        {
            throw new LogCheckException( String.format(
                    "Invalid standard output file '%s'\n", f ));
        }

        try
        {
            TeeOutputStream stdOut = new TeeOutputStream(System.out, currFileStream);
            System.setOut(new PrintStream(stdOut));
        }
        catch (Exception ex)
        {
            String err = String.format("Failed setting System.stdOut.\n");
            LOGGER.error(err, ex);
            throw new LogCheckException(err, ex );
        }

        try
        {
            TeeOutputStream stdErr = new TeeOutputStream(System.err, currFileStream);
            System.setErr(new PrintStream(stdErr));
        }
        catch (Exception ex)
        {
            String err = String.format("Failed setting System.stdErr.\n");
            LOGGER.error(err, ex);
            throw new LogCheckException(err, ex );
        }

        //      FSSLog4JConfiguration.reconfigureAppender();
    }
}
