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
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
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
        CommandLineParser parser = new GnuParser();
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
                        
                    case "lock-file":
                        // Write a file preventing multiple instances
                        config.setLockFilePath(currOpt.getValue());
                        break;
                        
                    case "log-file":
                        // Log file for monitoring
                        config.setLogPath(currOpt.getValue());
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

        options.addOption( OptionBuilder.withLongOpt( "config-file" )
                                .withDescription( "Configuration file." )
                                .hasArg()
                                .withArgName("CONFFILE")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "service" )
                                .withDescription( "Run as a background service" )
                                .create() );

        options.addOption( OptionBuilder.withLongOpt( "arg-file" )
                                .withDescription( "Command-line argument file." )
                                .hasArg()
                                .withArgName("ARGFILE")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "holding-folder" )
                                .withDescription( "Local folder for keeping downloaded data." )
                                .hasArg()
                                .withArgName("LOGFILE")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "poll-interval" )
                                .withDescription( "Seconds between polling the log file." )
                                .hasArg()
                                .withArgName("POLLINTERVAL")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "email-on-error" )
                                .withDescription( "Send an email to this person on failure" )
                                .hasArg()
                                .withArgName("EMAILONERROR")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "smtp-server" )
                                .withDescription( "SMTP Server name" )
                                .hasArg()
                                .withArgName("SMTPSERVERNAME")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "smtp-port" )
                                .withDescription( "SMTP Server Port" )
                                .hasArg()
                                .withArgName("SMTPSERVERPORT")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "smtp-user" )
                                .withDescription( "SMTP User" )
                                .hasArg()
                                .withArgName("SMTPUSER")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "smtp-pass" )
                                .withDescription( "SMTP User password" )
                                .hasArg()
                                .withArgName("SMTPUSERPASS")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "smtp-proto" )
                                .withDescription( "SMTP Protocol" )
                                .hasArg()
                                .withArgName("SMTPPROTO")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "dry-run" )
                                .withDescription( "Do not update the database" )
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "version" )
                                .withDescription( "Show the application version" )
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "lock-file" )
                                .withDescription( "Prevent multiple instances of the service from running." )
                                .hasArg()
                                .withArgName("LOCKFILE")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "log-file" )
                                .withDescription( "Log file required for checking." )
                                .hasArg()
                                .withArgName("LOGFILE")
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "file-from-start" )
                                .withDescription( "Process all records already in the log file." )
                                .create() );
        
        options.addOption( OptionBuilder.withLongOpt( "elasticsearch-url" )
                                .withDescription( "Elasticsearch URL, including protocol and port." )
                                .hasArg()
                                .withArgName("ELASTICSEARCHURL")
                                .create() );

        
        options.addOption( OptionBuilder.withLongOpt( "status-file" )
                                .withDescription( "Status file used for session data." )
                                .hasArg()
                                .withArgName("STATUSFILE")
                                .create() );
        
        
        return options;
    }
}
