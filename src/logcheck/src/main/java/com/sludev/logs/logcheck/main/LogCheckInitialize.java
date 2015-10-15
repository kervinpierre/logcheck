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
package com.sludev.logs.logcheck.main;

import com.sludev.logs.logcheck.config.LogCheckConfig;
import com.sludev.logs.logcheck.config.LogCheckConfigFile;
import com.sludev.logs.logcheck.utils.LogCheckException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Initialize the LogCheck application.
 * 
 * @author kervin
 */
public class LogCheckInitialize 
{
    private static final Logger log 
                             = LogManager.getLogger(LogCheckInitialize.class);
    
    /**
     * @param args the command line arguments
     */
    public static LogCheckConfig initialize(String[] args)
    {  
        CommandLineParser parser = new DefaultParser();
        Options options = configureOptions();
        LogCheckConfig config = new LogCheckConfig();
        
        try
        {
            // Get the command line argument list from the OS
            CommandLine line;
            try
            {
                line = parser.parse(options, args);
            }
            catch (ParseException ex)
            {
                throw new LogCheckException( 
                        String.format("Error parsing command line.'%s'", 
                                ex.getMessage()), ex);
            }
            
            // First check for an argument file, so it can override command
            // line arguments from above.
            //
            // NB: You can't use command line arguments AND argfile at the same
            //     time
            if (line.hasOption("arg-file"))
            {
                String argfile = line.getOptionValue("arg-file");
                
                try
                {
                    BufferedReader reader = new BufferedReader( new FileReader(argfile));
                    String argLine = "";
                    
                    do
                    {
                        argLine = reader.readLine();
                        if( argLine != null )
                        {
                            argLine = argLine.trim();
                        }
                    }
                    while(  argLine != null 
                                && (argLine.length() < 1 ||  argLine.startsWith("#")) );
                    
                    String[] argArray = new String[0];
                    
                    if( argLine != null )
                    {
                        argArray = argLine.split("\\s+");
                    }
                    
                            
                    log.debug( String.format("LogCheckMain main() : Argfile parsed : %s\n", 
                            Arrays.toString(argArray)) );
        
                    try
                    {
                        line = parser.parse(options, argArray);
                    }
                    catch (ParseException ex)
                    {
                        throw new LogCheckException( 
                            String.format("Error parsing command line.'%s'", 
                                ex.getMessage()), ex);
                    }
                }
                catch (FileNotFoundException ex)
                {
                     log.error( "Error : File not found :", ex);
                    throw new LogCheckException("ERROR: Argument file not found.", ex);
                }
                catch (IOException ex)
                {
                     log.error( "Error : IO : ", ex);
                    throw new LogCheckException("ERROR: Argument file can not be read.", ex);
                }
            }
            
            // Second, check for the configuration file.  It needs to be parsed
            // before all command line arguments.  So that these arguments later
            // override the configuration file values
            if (line.hasOption("config-file"))
            {
                String configfile = line.getOptionValue("config-file");
                
                if( Files.isReadable(Paths.get(configfile)) == false )
                {
                    throw new LogCheckException(
                        String.format("Invalid configuration file '%s'.",
                                        configfile));
                }
                
                config.setConfigFilePath(configfile);
                
                if( config.getConfigFilePath() != null )
                {
                    LogCheckConfigFile confFile = new LogCheckConfigFile();
                    confFile.setFilePath(config.getConfigFilePath());
                    confFile.setConfig(config);

                    confFile.read();
                }
            }
            
            if( line.getOptions().length < 1 )
            {
                // At least one option is mandatory
                throw new LogCheckException("No program arguments were found.");
            }
            
            // Argument order can be important. We may be creating THEN changing a folder's attributes.
            // It would be important to create the folder first.
            Iterator cmdI = line.iterator();
            while( cmdI.hasNext())
            {
                Option currOpt = (Option)cmdI.next();
                String currOptName = currOpt.getLongOpt();

                switch( currOptName )
                {
                    case "service":
                        // Run as a service
                        config.setService(true);
                        break;
                      
                    case "poll-interval":
                        // File polling interval
                        config.setPollIntervalSeconds(currOpt.getValue());
                        break;
                      
                    case "email-on-error":
                        // Send an email when we have an error
                        config.setEmailOnError(currOpt.getValue());
                        break;
                      
                    case "smtp-server":
                        // SMTP host server
                        config.setSmtpServer(currOpt.getValue());
                        break;
                    
                    case "smtp-port":
                        // SMTP server port
                        config.setSmtpPort(currOpt.getValue());
                        break;
                        
                    case "smtp-user":
                        // SMTP Login user
                        config.setSmtpUser(currOpt.getValue());
                        break;
                        
                    case "smtp-pass":
                        // SMTP Login password
                        config.setSmtpPass(currOpt.getValue());
                        break;
                        
                    case "smtp-proto":
                        // STMP Protocol type
                        config.setSmtpProto(currOpt.getValue());
                        break;
                        
                    case "dry-run":
                        // For testing, do not update the database
                        config.setDryRun(true);
                        break;
                        
                    case "version":
                        // Show the application version and exit
                        config.setShowVersion(true);
                        break;
                        
                     case "log-deduplication-duration":
                        // Don't send the same log twice
                        config.setLogDeduplicationDuration(currOpt.getValue());
                        break;
                        
                    case "lock-file":
                        // Write a file preventing multiple instances
                        config.setLockFilePath(currOpt.getValue());
                        break;
                        
                    case "log-file":
                        // Log file for monitoring
                        config.setLogPath(currOpt.getValue());
                        break;
                        
                    case "log-cutoff-duration":
                        // Do not process before specified period
                        config.setLogCutoffDuration(currOpt.getValue());
                        break;
                        
                     case "log-cutoff-date":
                        // Do not process before specified period
                        config.setLogCutoffDate(currOpt.getValue());
                        break;
                         
                    case "file-from-start":
                        // Process the specified log file from its start
                        config.setTailFromEnd(false);
                        break;
                        
                    case "elasticsearch-url":
                        // The Elasticsearch URL
                        config.setElasticsearchURL(currOpt.getValue());
                        break;
                        
                    case "status-file":
                        // Write session data
                        config.setStatusFilePath(currOpt.getValue());
                        break;
                }
            }
        }
        catch (LogCheckException ex)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            
            pw.append( String.format("Error : '%s'\n\n", ex.getMessage()));
            
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( pw, 80,"\njava -jar logcheck-0.9.jar ", 
                                        "\nThe logcheck application can be used in a variety of options and modes.\n", options,
                                        0, 2, "© All Rights Reserved.",
                                        true);
            
            System.out.println(sw.toString());
            
            System.exit(1);
        }
        
        return config;
    }
    
    private static Options configureOptions()
    {
        Options options = new Options();

        options.addOption( Option.builder().longOpt("config-file")
                                .desc( "Configuration file." )
                                .hasArg()
                                .argName("CONFFILE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "service" )
                                .desc( "Run as a background service" )
                                .build() );

        options.addOption( Option.builder().longOpt( "arg-file" )
                                .desc( "Command-line argument file." )
                                .hasArg()
                                .argName("ARGFILE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "holding-folder" )
                                .desc( "Local folder for keeping downloaded data." )
                                .hasArg()
                                .argName("LOGFILE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "poll-interval" )
                                .desc( "Seconds between polling the log file." )
                                .hasArg()
                                .argName("POLLINTERVAL")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "email-on-error" )
                                .desc( "Send an email to this person on failure" )
                                .hasArg()
                                .argName("EMAILONERROR")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "smtp-server" )
                                .desc( "SMTP Server name" )
                                .hasArg()
                                .argName("SMTPSERVERNAME")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "smtp-port" )
                                .desc( "SMTP Server Port" )
                                .hasArg()
                                .argName("SMTPSERVERPORT")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "smtp-user" )
                                .desc( "SMTP User" )
                                .hasArg()
                                .argName("SMTPUSER")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "smtp-pass" )
                                .desc( "SMTP User password" )
                                .hasArg()
                                .argName("SMTPUSERPASS")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "smtp-proto" )
                                .desc( "SMTP Protocol" )
                                .hasArg()
                                .argName("SMTPPROTO")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "dry-run" )
                                .desc( "Do not update the database" )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "version" )
                                .desc( "Show the application version" )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "lock-file" )
                                .desc( "Prevent multiple instances of the service from running." )
                                .hasArg()
                                .argName("LOCKFILE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "log-file" )
                                .desc( "Log file required for checking." )
                                .hasArg()
                                .argName("LOGFILE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "log-cutoff-duration" )
                                .desc( "Do not process logs older than this duration. E.g. \"P2DT3H4M\"  becomes \"2 days, 3 hours and 4 minutes\"" )
                                .hasArg()
                                .argName("LOGCUTOFFPERIOD")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "log-cutoff-date" )
                                .desc( "Do not process logs older than this date. YYYYMMDD-hhmmss" )
                                .hasArg()
                                .argName("LOGCUTOFFDATE")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "log-deduplicate-duration" )
                                .desc( "Do not not send the same log entry more than once per session for the specified duration. See 'log-cutoff-duration" )
                                .hasArg()
                                .argName("LOGDEDUP")
                                .build() );
        
        options.addOption( Option.builder().longOpt( "file-from-start" )
                                .desc( "Process all records already in the log file." )
                                .build() );
        
        options.addOption( Option.builder().longOpt( "elasticsearch-url" )
                                .desc( "Elasticsearch URL, including protocol and port." )
                                .hasArg()
                                .argName("ELASTICSEARCHURL")
                                .build() );

        
        options.addOption( Option.builder().longOpt( "status-file" )
                                .desc( "Status file used for session data." )
                                .hasArg()
                                .argName("STATUSFILE")
                                .build() );
        
        
        return options;
    }
}
