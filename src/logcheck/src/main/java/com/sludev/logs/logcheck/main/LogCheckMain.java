/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sludev.logs.logcheck.main;

import com.sludev.logs.logcheck.config.LogCheckConfig;
import com.sludev.logs.logcheck.config.LogCheckConfigFile;
import com.sludev.logs.logcheck.utils.LogCheckResult;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Administrator
 */
public class LogCheckMain 
{
    private static final Logger log 
                             = LogManager.getLogger(LogCheckMain.class);
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {  
        CommandLineParser parser = new GnuParser();
        Options options = ConfigureOptions();
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
                      
                    case "cron-schedule":
                        // Cron Schedule
                        config.setCronScheduleString(currOpt.getValue());
                        break;
                      
                    case "email-on-failure":
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
                }
            }
        }
        catch (LogCheckException ex)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            
            pw.append( String.format("Error : '%s'\n\n", ex.getMessage()));
            
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( pw, 80,"\njava -jar mssqlrestore-0.9.jar ", 
                                        "\nThe mssqlrestore application can be used in a variety of options and modes.\n", options,
                                        0, 2, "© All Rights Reserved.",
                                        true);
            
            System.out.println(sw.toString());
            
            System.exit(1);
        }
        
        LogCheckRun currRun = new LogCheckRun();
        FutureTask<LogCheckResult> currRunTask = new FutureTask<>(currRun);
        currRun.setConfig(config);
        
        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
            .namingPattern("mainthread-%d")
            .build();
        
        ExecutorService currExe = Executors.newSingleThreadExecutor(thFactory);
        Future exeRes = currExe.submit(currRunTask);
        
        try
        {
            LogCheckResult resp = currRunTask.get();
        }
        catch (InterruptedException ex)
        {
            log.error("Application thread was interrupted", ex);
        }
        catch (ExecutionException ex)
        {
            log.error("Application execution error", ex);
        }
        
        currExe.shutdown();
        
        log.debug( String.format("LogCheckMain end.\n") );
    }
    
    private static Options ConfigureOptions()
    {
        Options options = new Options();

        options.addOption( OptionBuilder.withLongOpt( "config-file" )
                                .withDescription( "Configuration file." )
                                .hasArg()
                                .withArgName("CONFFILE")
                                .create() );
        
        return options;
    }
}
