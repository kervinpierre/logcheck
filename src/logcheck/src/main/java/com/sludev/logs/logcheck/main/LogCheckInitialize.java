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

import com.sludev.logs.logcheck.config.builders.LogCheckConfigBuilder;
import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.config.parsers.LogCheckConfigParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.utils.FSSArgFile;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.FSSLog4JConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.impl.ExtendedClassInfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Initialize the LogCheck application.
 * 
 * @author kervin
 */
public class LogCheckInitialize 
{
    private static final Logger LOGGER
                             = LogManager.getLogger(LogCheckInitialize.class);
    
    /**
     * @param args the command line arguments
     */
    public static LinkedHashMap<Integer,LogCheckConfig> initialize(String[] args)
    {  
        CommandLineParser parser = new DefaultParser();
        Options options = configureOptions();

        // Ordered map
        LinkedHashMap<Integer,LogCheckConfig> configs = new LinkedHashMap<>(10);

        LogCheckConfigBuilder currConfBuilder = null;

//        Boolean currService = null;
//        Boolean currDryRun = null;
//        Boolean currShowVersion = null;
//        Boolean currTailFromEnd = null;
//        Boolean currPrintLogs = null;
//        Boolean currSaveState = null;
//        Boolean currContinue = null;
//        Boolean currReadReOpenLogFile = null;
//        Boolean currStoreReOpenLogFile = null;
//        Boolean currStartPositionIgnoreError = null;
//        Boolean currValidateTailerStats = null;
//        Boolean currTailerBackupReadLogs = null;
//        Boolean currTailerBackupReadLogsReverse = null;
//        Boolean currTailerBackupReadPriorLogs = null;
//        Boolean currStopOnEOF = null;
//        Boolean currReadOnlyFileMode = null;
//        Boolean currCreateMissingDirs = null;
//        String currPollIntervalSeconds = null;
//        String currEmailOnError = null;
//        String currSmtpServer = null;
//        String currSmtpPort = null;
//        String currSmtpUser = null;
//        String currSmtpPass = null;
//        String currSmtpProto = null;
//        String currLogDeduplicationDuration = null;
//        String currLockFile = null;
//        String currLogPath = null;
//        String currLogCutoffDuration = null;
//        String currLogCutoffDate = null;
//        String currElasticsearchUrl = null;
//        String currStatusFile = null;
//        String currStateFile = null;
//        String currProcessedLogsFile = null;
//        String currErrorFile = null;
//        String currIdBlockHashtype = null;
//        String currIdBlockSize = null;
//        String currSetName = null;
//        String currDeDupeDirPath = null;
//        String currDeDupeMaxLogsPerFile = null;
//        String currDeDupeMaxLogFiles = null;
//        String currDeDupeMaxLogsBeforeWrite = null;
//        String currDeDupeDefaultAction = null;
//        String currDeDupeIgnoreUntilPercent = null;
//        String currDeDupeIgnoreUntilCount = null;
//        String currDeDupeSkipUntilPercent = null;
//        String currDeDupeSkipUntilCount = null;
//        String currStopAfter = null;
//        String currReadLogFileCount = null;
//        String currReadMaxDeDupeEntries = null;
//        String currStoreLogFile = null;
//        String currTailerBackupLogNameRegex = null;
//        String currTailerBackupLogCompression = null;
//        String currTailerBackupLogDir = null;
//        String currVerbosity = null;
//        String currLogSource = null;
//        String currWinEventConnection = null;
//        String currMonitorURL = null;
//        String currConfigStr = null;
//        String currStdOutFile = null;
//        String currPreferredDir = null;
//        String[] currLEBuilderType = null;
//        String[] currLEStoreType = null;
//        String[] currTailerBackupLogNameComps = null;
//        String[] currDebugFlags = null;

        try
        {
            // Get the command line argument list from the OS

            List<CommandLine> lines = new ArrayList<>();

            try
            {
                lines.add(parser.parse(options, args));
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
            if( (lines.size() > 0) && lines.get(0).hasOption("argfile"))
            {
                String argfile = lines.get(0).getOptionValue("argfile");

                String[] argArray = FSSArgFile.getArgArray(Paths.get(argfile));

                LOGGER.debug(String.format("LogCheckMain main() : Argfile parsed : %s\n",
                        Arrays.toString(argArray)));

                try
                {
                    lines.add(parser.parse(options, argArray));
                }
                catch( ParseException ex )
                {
                    throw new LogCheckException(
                            String.format("Error parsing command line.'%s'",
                                    ex.getMessage()), ex);
                }
            }

            for( CommandLine currLine : lines )
            {
                // Second, check for the configuration file.  It needs to be parsed
                // before all command line arguments.  So that these arguments later
                // override the configuration file builders
                if( currLine.hasOption("config-file") )
                {
                    String configfile = currLine.getOptionValue("config-file");

                    if( Files.isReadable(Paths.get(configfile)) == false )
                    {
                        throw new LogCheckException(
                                String.format("Invalid configuration file '%s'.",
                                        configfile));
                    }

                    LinkedHashMap<Integer,LogCheckConfig> confConfs = LogCheckConfigParser.readConfig(
                            ParserUtil.readConfig(Paths.get(configfile),
                                    LCFileFormat.LCCONFIG));
                    configs.putAll(confConfs);
                }
            }
            
            if( lines.get(0).getOptions().length < 1 )
            {
                // At least one option is mandatory
                throw new LogCheckException("No program arguments were found.");
            }

            // Reverse to flip precedence?
            Collections.reverse(lines);

            for( CommandLine currLine : lines )
            {
                // Argument order can be important. We may be creating THEN changing a folder's attributes.
                // It would be important to create the folder first.
                Iterator cmdI = currLine.iterator();
                while( cmdI.hasNext() )
                {
                    Option currOpt = (Option) cmdI.next();
                    String currOptName = currOpt.getLongOpt();

                    if( currOptName.equalsIgnoreCase("config") )
                    {
                        // Configuration number
                        String currConfigStr = currOpt.getValue();
                        int pos;
                        try
                        {
                            pos = Integer.parseInt(currConfigStr);
                        }
                        catch( Exception ex )
                        {
                            throw new LogCheckException(
                                    String.format("Error parsing config number '%s'", currConfigStr),
                                    ex);
                        }

                        if( currConfBuilder != null
                                && currConfBuilder.getId() != null
                                && currConfBuilder.getId() >= 0 )
                        {
                            // Check if the config exists
                            LogCheckConfig currLCC = configs.get(currConfBuilder.getId());
                            currLCC = currConfBuilder.toConfig(currLCC);
                            configs.put(currConfBuilder.getId(), currLCC);
                        }

                        // Start a new builder object
                        currConfBuilder = LogCheckConfigBuilder.from(pos);
                    }
                    else
                    {
                        if( currConfBuilder == null )
                        {
                            throw new LogCheckException("Missing leading '--config=[number]' flag");
                        }

                        switch( currOptName )
                        {
                            case "service":
                                // Run as a service
                                currConfBuilder.setService(true);
                                break;

                            case "poll-interval":
                                // File polling interval
                                currConfBuilder.setPollIntervalSeconds(currOpt.getValue());
                                break;

                            case "email-on-error":
                                // Send an email when we have an error
                                currConfBuilder.setEmailOnError(null);
                                break;

                            case "smtp-server":
                                // SMTP host server
                                currConfBuilder.setSmtpServer(currOpt.getValue());
                                break;

                            case "smtp-port":
                                // SMTP server port
                                currConfBuilder.setSmtpPort(currOpt.getValue());
                                break;

                            case "smtp-user":
                                // SMTP Login user
                                currConfBuilder.setSmtpUser(currOpt.getValue());
                                break;

                            case "smtp-pass":
                                // SMTP Login password
                                currConfBuilder.setSmtpPass(currOpt.getValue());
                                break;

                            case "smtp-proto":
                                // STMP Protocol type
                                currConfBuilder.setSmtpProto(currOpt.getValue());
                                break;

                            case "dry-run":
                                // For testing, do not update the database
                                currConfBuilder.setDryRun(true);
                                break;

                            case "version":
                                // Show the application version and exit
                                currConfBuilder.setShowVersion(true);
                                break;

                            case "log-deduplication-duration":
                                // Don't send the same log twice
                                currConfBuilder.setLogDeduplicationDuration(currOpt.getValue());
                                break;

                            case "lock-file":
                                // Write a file preventing multiple instances
                                currConfBuilder.setLockFilePath(currOpt.getValue());
                                break;

                            case "windows-event-connection":
                                // Windows Event Log Connection string
                                currConfBuilder.setWindowsEventConnection(currOpt.getValue());
                                break;

                            case "monitor-url":
                                // URL of listening monitor service
                                currConfBuilder.setMonitorURL(currOpt.getValue());
                                break;

                            case "log-source":
                                // Log source
                                currConfBuilder.setLogSourceType(currOpt.getValue());
                                break;

                            case "log-file":
                                // Log file for monitoring
                                currConfBuilder.setLogPath(currOpt.getValue());
                                break;

                            case "log-cutoff-duration":
                                // Do not process before specified period
                                currConfBuilder.setLogCutoffDuration(currOpt.getValue());
                                break;

                            case "log-cutoff-date":
                                // Do not process before specified period
                                currConfBuilder.setLogCutoffDate(currOpt.getValue());
                                break;

                            case "file-from-start":
                                // Process the specified log file from its start
                                currConfBuilder.setTailFromEnd(false);
                                break;

                            case "elasticsearch-url":
                                // The Elasticsearch URL
                                currConfBuilder.setElasticsearchURL(currOpt.getValue());
                                break;

                            case "state-file":
                                // Save the full state of a completed job for future
                                // continuation
                                currConfBuilder.setStateFilePath(currOpt.getValue());
                                break;

                            case "processed-logs-state-file":
                                // Track already processed log files
                                currConfBuilder.setStateProcessedLogsFilePath(currOpt.getValue());
                                break;

                            case "error-file":
                                // Save the full state of a completed job for future
                                // continuation
                                currConfBuilder.setErrorFilePath(currOpt.getValue());
                                break;

                            case "status-file":
                                // Write session data
                                currConfBuilder.setStatusFilePath(currOpt.getValue());
                                break;

                            case "print-logs":
                                // Print logs to console
                                currConfBuilder.setPrintLog(true);
                                break;

                            case "continue":
                                // Continue the last job
                                currConfBuilder.setContinueState(true);
                                break;

                            case "save-state":
                                // Continue the last job
                                currConfBuilder.setSaveState(true);
                                break;

                            case "id-block-hashtype":
                                //
                                currConfBuilder.setIdBlockHashType(currOpt.getValue());
                                break;

                            case "id-block-size":
                                //
                                currConfBuilder.setIdBlockSize(currOpt.getValue());
                                break;

                            case "set-name":
                                //
                                currConfBuilder.setSetName(currOpt.getValue());
                                break;

                            case "dedupe-dir-path":
                                //
                                currConfBuilder.setDeDupeDirPath(currOpt.getValue());
                                break;

                            case "dedupe-log-per-file":
                                //
                                currConfBuilder.setDeDupeMaxLogsPerFile(currOpt.getValue());
                                break;

                            case "dedupe-max-log-files":
                                //
                                currConfBuilder.setDeDupeMaxLogFiles(currOpt.getValue());
                                break;

                            case "dedupe-max-before-write":
                                //
                                currConfBuilder.setDeDupeMaxLogsBeforeWrite(currOpt.getValue());
                                break;

                            case "dedupe-default-action":
                                //
                                currConfBuilder.setDeDupeDefaultAction(currOpt.getValue());
                                break;

                            case "dedupe-ignore-percent":
                                //
                                currConfBuilder.setDeDupeIgnorePercent(currOpt.getValue());
                                break;

                            case "dedupe-ignore-count":
                                //
                                currConfBuilder.setDeDupeIgnoreCount(currOpt.getValue());
                                break;

                            case "dedupe-skip-percent":
                                //
                                currConfBuilder.setdeDupeSkipPercent(currOpt.getValue());
                                break;

                            case "dedupe-skip-count":
                                //
                                currConfBuilder.setDeDupeSkipCount(currOpt.getValue());
                                break;

                            case "log-entry-builder-type":
                                // Specify the log entry builder type to use
                                currConfBuilder.setLogEntryBuilderStrs(currOpt.getValues());
                                break;

                            case "log-entry-store-type":
                                // Specify the log entry store type to use
                                currConfBuilder.setLogEntryStoreStrs(currOpt.getValues());
                                break;

                            case "stop-after":
                                // How long to run the tailer
                                currConfBuilder.setStopAfter(currOpt.getValue());
                                break;

                            case "read-log-file-count":
                                // Read log file count for deduplication logs
                                currConfBuilder.setReadLogFileCount(currOpt.getValue());
                                break;

                            case "read-max-dedupe-entries":
                                // Maximum deduplication log entries
                                currConfBuilder.setReadMaxDeDupeEntries(currOpt.getValue());
                                break;

                            case "read-reopen-log-file":
                                // Specify the log entry builder type to use
                                currConfBuilder.setReadReOpenLogFile(true);
                                break;

                            case "store-reopen-log-file":
                                // Specify the log entry builder type to use
                                currConfBuilder.setStoreReOpenLogFile(true);
                                break;

                            case "store-log-file":
                                // Maximum deduplication log entries
                                currConfBuilder.setStoreLogPath(currOpt.getValue());
                                break;

                            case "start-position-ignore-error":
                                // If there is a discrepancy between the State File and the Log File
                                currConfBuilder.setStartPositionIgnoreError(true);
                                break;

                            case "tailer-validate-log-file":
                                // Validate Tailer Statistics on disk periodically to make sure the logs
                                // have not been rotated.
                                currConfBuilder.setValidateTailerStats(true);
                                break;

                            case "tailer-read-backup-log":
                                // Find the backup log files after they've been rotated.
                                currConfBuilder.setTailerBackupReadLog(true);
                                break;

                            case "tailer-read-prior-backup-log":
                                // Read the old backup logs prior to tailing
                                currConfBuilder.setTailerBackupReadPriorLog(true);
                                break;

                            case "tailer-backup-log-file-name-regex":
                                // The file regular expression for matching backup log files.
                                currConfBuilder.setTailerBackupLogNameRegex(currOpt.getValue());
                                break;

                            case "tailer-backup-log-file-name-component":
                                // These match the file name grouping the the related file name regex.
                                if( currConfBuilder.getTailerBackupLogNameComps() == null )
                                {
                                    currConfBuilder.setTailerBackupLogNameCompStrs(currOpt.getValues());
                                }else
                                {
                                    currConfBuilder.getTailerBackupLogNameComps()
                                            .addAll(LCFileRegexComponent.from(currOpt.getValues()));
                                }
                                break;

                            case "debug-flags":
                                // Miscellaneous debug flags
                                if( currConfBuilder.getDebugFlags() == null )
                                {
                                    currConfBuilder.setDebugFlagStrs(currOpt.getValues());
                                }
                                else
                                {
                                    currConfBuilder.getDebugFlags().addAll(
                                            LCDebugFlag.from(currOpt.getValues()));
                                }
                                break;

                            case "tailer-backup-log-file-compression":
                                // Decompress the already backed up log file before reading.
                                currConfBuilder.setTailerBackupLogCompression(currOpt.getValue());
                                break;

                            case "tailer-backup-log-dir":
                                // Log backups folder
                                currConfBuilder.setTailerLogBackupDir(currOpt.getValue());
                                break;

                            case "tailer-stop-on-eof":
                                // Stop after EOF
                                currConfBuilder.setStopOnEOF(true);
                                break;

                            case "tailer-read-backup-reverse-order":
                                // Read backup logs in reverse order
                                currConfBuilder.setTailerBackupReadLogReverse(true);
                                break;

                            case "tailer-read-only-file":
                                // Read Only File
                                currConfBuilder.setReadOnlyFileMode(true);
                                break;

                            case "create-missing-dirs":
                                // Create missing directories
                                currConfBuilder.setCreateMissingDirs(true);
                                break;

                            case "verbosity":
                                // Verbosity
                                currConfBuilder.setVerbosity(currOpt.getValue());
                                break;

                            case "stdout-file":
                                // Standard Output
                                currConfBuilder.setStdOutFile(currOpt.getValue());
                                break;

                            case "preferred-dir":
                                // Preferred directory
                                currConfBuilder.setPreferredDir(currOpt.getValue());
                                break;
                        }
                    }
                }
            }

            // Save the last builder
            if( currConfBuilder != null
                    && currConfBuilder.getId() != null
                    && currConfBuilder.getId() >= 0 )
            {
                // Check if the config exists
                LogCheckConfig currLCC = configs.get(currConfBuilder.getId());
                currLCC = currConfBuilder.toConfig(currLCC);
                configs.put(currConfBuilder.getId(), currLCC);
            }

            /*
            Set the application's verbosity level.  Prior to this call here, it's
            set to WARN by default, or by the -DFSSVERBOSITY java command line
            property.

            --verbosity has the top precedence.  Using this argument to set the
               verbosity on any command run.

            -DFSSVERBOSITY can be used as a default system-wide verbosity.  Since
               it sets the logger's verbosity from the "java/jre" start, you should set it so
               that you do not lose any messages
            */
            LogCheckConfig mainConfig = null;

            if( configs.isEmpty() == false && configs.keySet().contains(0))
            {
                mainConfig = configs.get(0);
            }

            if( (mainConfig != null) && (mainConfig.getVerbosity() != null) )
            {
                FSSLog4JConfiguration.setVerbosity(mainConfig.getVerbosity());
            }

            /*
              Or use -DFSSOUTREDIRECT=/path/to/file
             */
            Path usedStdOut = null;
            if( (mainConfig != null)
                    && (mainConfig.getStdOutFile() != null) )
            {
                usedStdOut = mainConfig.getStdOutFile();
            }

            if( usedStdOut != null )
            {
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                FSSLog4JConfiguration logConf = (FSSLog4JConfiguration)ctx.getConfiguration();

                FSSLog4JConfiguration.outputRedirect(usedStdOut);
            }
        }
        catch (LogCheckException ex)
        {

            try(StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw))
            {
                pw.append(String.format("Error : '%s'\n\n", ex.getMessage()));

                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(pw, 80, "\njava [-DFSSVERBOSITY=warn] [-DFSSOUTREDIRECT=/path/to/file] -jar logcheck-0.9.jar ",
                        "\nThe logcheck application can be used in a variety of options and modes.\n", options,
                        0, 2, "© All Rights Reserved.",
                        true);

                System.out.println(sw.toString());
            }
            catch( IOException iex )
            {
                LOGGER.debug("", iex);
            }

            System.exit(1);
        }
        
        return configs;
    }
    
    private static Options configureOptions()
    {
        Options options = new Options();

        options.addOption( Option.builder().longOpt("config-file")
                .desc( "Configuration file." )
                .hasArg()
                .argName("CONFFILE")
                .build() );

        options.addOption( Option.builder().longOpt("log-source")
                .desc( "Log source.  File ( default ), Windows-Event-Log" )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt("windows-event-connection")
                .desc( "[<hostname>|localhost]:[Application|Security|System|,]+" )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt("monitor-url")
                .desc( "Monitor URL. E.g. JMS Url or 'file://'" )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt("config")
                .desc( "The configuration object number.  Configuration "
                        + "number '0' is the special 'main' configuration object" )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt("state-file")
                .desc( "Application state file. Save the full state of a completed job for future\n" +
                        " continuation" )
                .hasArg()
                .argName("STATEFILE")
                .build() );

        options.addOption( Option.builder().longOpt("processed-logs-state-file")
                .desc( "Application state file for tracking previously processed log files." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "error-file" )
                .desc( "Log application errrors." )
                .hasArg()
                .argName("ERRORFILE")
                .build() );

        options.addOption( Option.builder().longOpt( "service" )
                .desc( "Run as a background service.  No normal stop planned." )
                .build() );

        options.addOption( Option.builder().longOpt( "argfile" )
                .desc( "Command-line argument file." )
                .hasArg()
                .argName("ARGFILE")
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

        options.addOption( Option.builder().longOpt( "log-entry-builder-type" )
                .desc( "The method for parsing log entries as they come in. Options are 'single', 'multiline-delimited'" )
                .hasArgs()
                .argName("LCLEBUILDERTYPE")
                .build() );

        options.addOption( Option.builder().longOpt( "log-entry-store-type" )
                .desc( "The method for storing log entries. Options include 'elasticsearch', 'console', and 'file'" )
                .hasArgs()
                .valueSeparator(',')
                .argName("LCLESTORETYPE")
                .build() );

        options.addOption( Option.builder().longOpt( "print-logs" )
                .desc( "Output the logs to the standard out. Mainly for testing." )
                .build() );

        options.addOption( Option.builder().longOpt( "continue" )
                .desc( "Continue that last job if there is one." )
                .build() );

        options.addOption( Option.builder().longOpt( "save-state" )
                .desc( "Save the job state after completion." )
                .build() );

        options.addOption( Option.builder().longOpt("id-block-hashtype")
                .desc("Hash type to use when ID'ing blocks. 'SHA1', 'SHA256', 'MD5'.")
                .hasArg()
                .build());

        options.addOption(Option.builder().longOpt( "id-block-size" )
                .desc( "Size of blocks used for ID'ing file position." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "status-file" )
                .desc( "Status file used for session data." )
                .hasArg()
                .argName("STATUSFILE")
                .build() );

        options.addOption( Option.builder().longOpt( "set-name" )
                .desc( "Optional short id for this job." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-dir-path" )
                .desc( "For storing all the deduplication logs." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-log-per-file" )
                .desc( "The maximum number of log entries per deduplication log file." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-max-log-files" )
                .desc( "The maximum number of log files to keep. Older files are deleted." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-max-before-write" )
                .desc( "The number of log entries that should be stored before the "
                        + "deduplication information is written to disk." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-default-action" )
                .desc( "The default action after a duplicate record is detected. "
                        + "Actions include IGNORE, SKIP, or BREAK." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-ignore-percent" )
                .desc( "The percentage of records that can be duplicate records and also ignored. " )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-ignore-count" )
                .desc( "The number of records that can be duplicate records and also ignored. " )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-skip-percent" )
                .desc( "The percentage of records that can be duplicate records and also skipped. " )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "dedupe-skip-count" )
                .desc( "The number of records that can be duplicate records and also skipped. " )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "stop-after" )
                .desc( "The number of seconds to run before stopping." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "read-log-file-count" )
                .desc( "The number of deduplication log files to read on restore." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "read-max-dedupe-entries" )
                .desc( "The maximum number of deduplication entries to read." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "read-reopen-log-file" )
                .desc( "Close and reopen log files between reading them." )
                .build() );

        options.addOption( Option.builder().longOpt( "store-reopen-log-file" )
                .desc( "Close and reopen the Log Entry Store log file." )
                .build() );

        options.addOption( Option.builder().longOpt( "store-log-file" )
                .desc( "The path to the Log Entry log file on disk." )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "start-position-ignore-error" )
                .desc( "If there is a discrepancy between the State File and the Log File, e.g. invalid position.  Then ignore the state file" )
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-validate-log-file" )
                .desc( "Validate Tailer Log File on disk using Statistics periodically to make sure the logs" +
                        " have not been rotated." )
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-read-backup-log" )
                .desc( "Find the backup log files AFTER they've been rotated.  Read those backups before continuing." )
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-read-backup-reverse-order" )
                .desc( "Are backup files ordered oldest to newest?  Or newest to oldest? "
                        + "True if new logs are in lower ordered backup files." )
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-read-prior-backup-log" )
                .desc( "Find the prior backup log files.  Read those backup logs before starting." )
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-backup-log-file-name-regex" )
                .desc( "The file regular expression for matching backup log files. E.g. (.*?).(\\d).bak" )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-backup-log-file-name-component" )
                .desc( "These match the file name grouping the the related file name regex."
                        + "  Values include FILENAME_PREFIX, INTEGER_INC, TIMESTAMP.")
                .hasArgs()
                .build() );

        options.addOption( Option.builder().longOpt( "debug-flags" )
                .desc( "Allows passing in miscellaneous debug flags."
                        + "  Values include LOG_SOURCE_LC_APP.")
                .hasArgs()
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-backup-log-file-compression" )
                .desc( "Decompress the already backed up log file before reading." )
                .hasArgs()
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-backup-log-dir" )
                .desc( "The directory were log files are backed up to" )
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-stop-on-eof" )
                .desc( "Stop tailing the log file when EOF is reached." )
                .build() );

        options.addOption( Option.builder().longOpt( "tailer-read-only-file" )
                .desc( "The file we're tailing will not be modified in anyway by external programs. E.g. A backup log." )
                .build() );

        options.addOption( Option.builder().longOpt( "verbosity" )
                .desc( "Verbosity level for the application."
                        + "  Values include NONE, ALL, MINIMUM, MAXIMUM, DEBUG, INFO, WARN, ERROR.")
                .hasArgs()
                .build() );

        options.addOption( Option.builder().longOpt( "stdout-file" )
                .desc( "Send out standard output to the specified file.")
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "preferred-dir" )
                .desc( "Use this directory for creating File-System items with relative paths."
                        + "  Also attempt to change the current working directory to this path.")
                .hasArg()
                .build() );

        options.addOption( Option.builder().longOpt( "create-missing-dirs" )
                .desc( "Create missing directories." )
                .build() );

        return options;
    }
}
