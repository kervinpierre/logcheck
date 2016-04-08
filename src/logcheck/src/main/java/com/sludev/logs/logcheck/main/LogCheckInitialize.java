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

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.config.parsers.LogCheckConfigParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.enums.LCFileFormat;
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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
    public static LogCheckConfig initialize(String[] args)
    {  
        CommandLineParser parser = new DefaultParser();
        Options options = configureOptions();
        LogCheckConfig config = null;

        Boolean currService = null;
        Boolean currDryRun = null;
        Boolean currShowVersion = null;
        Boolean currTailFromEnd = null;
        Boolean currPrintLogs = null;
        Boolean currSaveState = null;
        Boolean currContinue = null;
        Boolean currReadReOpenLogFile = null;
        Boolean currStoreReOpenLogFile = null;
        Boolean currStartPositionIgnoreError = null;
        Boolean currValidateTailerStats = null;
        Boolean currTailerBackupReadLogs = null;
        Boolean currTailerBackupReadLogsReverse = null;
        Boolean currTailerBackupReadPriorLogs = null;
        Boolean currStopOnEOF = null;
        Boolean currReadOnlyFileMode = null;
        Boolean currCreateMissingDirs = null;
        String currPollIntervalSeconds = null;
        String currEmailOnError = null;
        String currSmtpServer = null;
        String currSmtpPort = null;
        String currSmtpUser = null;
        String currSmtpPass = null;
        String currSmtpProto = null;
        String currLogDeduplicationDuration = null;
        String currLockFile = null;
        String currLogPath = null;
        String currLogCutoffDuration = null;
        String currLogCutoffDate = null;
        String currElasticsearchUrl = null;
        String currStatusFile = null;
        String currStateFile = null;
        String currProcessedLogsFile = null;
        String currErrorFile = null;
        String currIdBlockHashtype = null;
        String currIdBlockSize = null;
        String currSetName = null;
        String currDeDupeDirPath = null;
        String currDeDupeMaxLogsPerFile = null;
        String currDeDupeMaxLogFiles = null;
        String currDeDupeMaxLogsBeforeWrite = null;
        String currDeDupeDefaultAction = null;
        String currDeDupeIgnoreUntilPercent = null;
        String currDeDupeIgnoreUntilCount = null;
        String currDeDupeSkipUntilPercent = null;
        String currDeDupeSkipUntilCount = null;
        String currStopAfter = null;
        String currReadLogFileCount = null;
        String currReadMaxDeDupeEntries = null;
        String currStoreLogFile = null;
        String currTailerBackupLogNameRegex = null;
        String currTailerBackupLogCompression = null;
        String currTailerBackupLogDir = null;
        String currVerbosity = null;
        String currStdOutFile = null;
        String currPreferredDir = null;
        String[] currLEBuilderType = null;
        String[] currLEStoreType = null;
        String[] currTailerBackupLogNameComps = null;
        String[] currDebugFlags = null;

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
                // override the configuration file values
                if( currLine.hasOption("config-file") )
                {
                    String configfile = currLine.getOptionValue("config-file");

                    if( Files.isReadable(Paths.get(configfile)) == false )
                    {
                        throw new LogCheckException(
                                String.format("Invalid configuration file '%s'.",
                                        configfile));
                    }

                    config = LogCheckConfigParser.readConfig(
                            ParserUtil.readConfig(Paths.get(configfile),
                                    LCFileFormat.LCCONFIG));
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

                    switch( currOptName )
                    {
                        case "service":
                            // Run as a service
                            currService = true;
                            break;

                        case "poll-interval":
                            // File polling interval
                            currPollIntervalSeconds = currOpt.getValue();
                            break;

                        case "email-on-error":
                            // Send an email when we have an error
                            currEmailOnError = null;
                            break;

                        case "smtp-server":
                            // SMTP host server
                            currSmtpServer = currOpt.getValue();
                            break;

                        case "smtp-port":
                            // SMTP server port
                            currSmtpPort = currOpt.getValue();
                            break;

                        case "smtp-user":
                            // SMTP Login user
                            currSmtpUser = currOpt.getValue();
                            break;

                        case "smtp-pass":
                            // SMTP Login password
                            currSmtpPass = currOpt.getValue();
                            break;

                        case "smtp-proto":
                            // STMP Protocol type
                            currSmtpProto = currOpt.getValue();
                            break;

                        case "dry-run":
                            // For testing, do not update the database
                            currDryRun = true;
                            break;

                        case "version":
                            // Show the application version and exit
                            currShowVersion = true;
                            break;

                        case "log-deduplication-duration":
                            // Don't send the same log twice
                            currLogDeduplicationDuration = currOpt.getValue();
                            break;

                        case "lock-file":
                            // Write a file preventing multiple instances
                            currLockFile = currOpt.getValue();
                            break;

                        case "log-file":
                            // Log file for monitoring
                            currLogPath = currOpt.getValue();
                            break;

                        case "log-cutoff-duration":
                            // Do not process before specified period
                            currLogCutoffDuration = currOpt.getValue();
                            break;

                        case "log-cutoff-date":
                            // Do not process before specified period
                            currLogCutoffDate = currOpt.getValue();
                            break;

                        case "file-from-start":
                            // Process the specified log file from its start
                            currTailFromEnd = false;
                            break;

                        case "elasticsearch-url":
                            // The Elasticsearch URL
                            currElasticsearchUrl = currOpt.getValue();
                            break;

                        case "state-file":
                            // Save the full state of a completed job for future
                            // continuation
                            currStateFile = currOpt.getValue();
                            break;

                        case "processed-logs-state-file":
                            // Track already processed log files
                            currProcessedLogsFile = currOpt.getValue();
                            break;

                        case "error-file":
                            // Save the full state of a completed job for future
                            // continuation
                            currErrorFile = currOpt.getValue();
                            break;

                        case "status-file":
                            // Write session data
                            currStatusFile = currOpt.getValue();
                            break;

                        case "print-logs":
                            // Print logs to console
                            currPrintLogs = true;
                            break;

                        case "continue":
                            // Continue the last job
                            currContinue = true;
                            break;

                        case "save-state":
                            // Continue the last job
                            currSaveState = true;
                            break;

                        case "id-block-hashtype":
                            //
                            currIdBlockHashtype = currOpt.getValue();
                            break;

                        case "id-block-size":
                            //
                            currIdBlockSize = currOpt.getValue();
                            break;

                        case "set-name":
                            //
                            currSetName = StringUtils.removeStart(currOpt.getValue(), "\"");
                            break;

                        case "dedupe-dir-path":
                            //
                            currDeDupeDirPath = currOpt.getValue();
                            break;

                        case "dedupe-log-per-file":
                            //
                            currDeDupeMaxLogsPerFile = currOpt.getValue();
                            break;

                        case "dedupe-max-log-files":
                            //
                            currDeDupeMaxLogFiles = currOpt.getValue();
                            break;

                        case "dedupe-max-before-write":
                            //
                            currDeDupeMaxLogsBeforeWrite = currOpt.getValue();
                            break;

                        case "dedupe-default-action":
                            //
                            currDeDupeDefaultAction = currOpt.getValue();
                            break;

                        case "dedupe-ignore-percent":
                            //
                            currDeDupeIgnoreUntilPercent = currOpt.getValue();
                            break;

                        case "dedupe-ignore-count":
                            //
                            currDeDupeIgnoreUntilCount = currOpt.getValue();
                            break;

                        case "dedupe-skip-percent":
                            //
                            currDeDupeSkipUntilPercent = currOpt.getValue();
                            break;

                        case "dedupe-skip-count":
                            //
                            currDeDupeSkipUntilCount = currOpt.getValue();
                            break;

                        case "log-entry-builder-type":
                            // Specify the log entry builder type to use
                            currLEBuilderType = currOpt.getValues();
                            break;

                        case "log-entry-store-type":
                            // Specify the log entry store type to use
                            currLEStoreType = currOpt.getValues();
                            break;

                        case "stop-after":
                            // How long to run the tailer
                            currStopAfter = currOpt.getValue();
                            break;

                        case "read-log-file-count":
                            // Read log file count for deduplication logs
                            currReadLogFileCount = currOpt.getValue();
                            break;

                        case "read-max-dedupe-entries":
                            // Maximum deduplication log entries
                            currReadMaxDeDupeEntries = currOpt.getValue();
                            break;

                        case "read-reopen-log-file":
                            // Specify the log entry builder type to use
                            currReadReOpenLogFile = true;
                            break;

                        case "store-reopen-log-file":
                            // Specify the log entry builder type to use
                            currStoreReOpenLogFile = true;
                            break;

                        case "store-log-file":
                            // Maximum deduplication log entries
                            currStoreLogFile = currOpt.getValue();
                            break;

                        case "start-position-ignore-error":
                            // If there is a discrepancy between the State File and the Log File
                            currStartPositionIgnoreError = true;
                            break;

                        case "tailer-validate-log-file":
                            // Validate Tailer Statistics on disk periodically to make sure the logs
                            // have not been rotated.
                            currValidateTailerStats = true;
                            break;

                        case "tailer-read-backup-log":
                            // Find the backup log files after they've been rotated.
                            currTailerBackupReadLogs = true;
                            break;

                        case "tailer-read-prior-backup-log":
                            // Read the old backup logs prior to tailing
                            currTailerBackupReadPriorLogs = true;
                            break;

                        case "tailer-backup-log-file-name-regex":
                            // The file regular expression for matching backup log files.
                            currTailerBackupLogNameRegex = currOpt.getValue();
                            break;

                        case "tailer-backup-log-file-name-component":
                            // These match the file name grouping the the related file name regex.
                            if( currTailerBackupLogNameComps == null )
                            {
                                currTailerBackupLogNameComps = currOpt.getValues();
                            }
                            else
                            {
                                currTailerBackupLogNameComps
                                        = ArrayUtils.addAll(currTailerBackupLogNameComps, currOpt.getValues());
                            }
                            break;

                        case "debug-flags":
                            // Miscellaneous debug flags
                            if( currDebugFlags == null )
                            {
                                currDebugFlags = currOpt.getValues();
                            }
                            else
                            {
                                currDebugFlags
                                        = ArrayUtils.addAll(currDebugFlags, currOpt.getValues());
                            }
                            break;

                        case "tailer-backup-log-file-compression":
                            // Decompress the already backed up log file before reading.
                            currTailerBackupLogCompression = currOpt.getValue();
                            break;

                        case "tailer-backup-log-dir":
                            // Log backups folder
                            currTailerBackupLogDir = currOpt.getValue();
                            break;

                        case "tailer-stop-on-eof":
                            // Stop after EOF
                            currStopOnEOF = true;
                            break;

                        case "tailer-read-backup-reverse-order":
                            // Read backup logs in reverse order
                            currTailerBackupReadLogsReverse = true;
                            break;

                        case "tailer-read-only-file":
                            // Read Only File
                            currReadOnlyFileMode = true;
                            break;

                        case "create-missing-dirs":
                            // Create missing directories
                            currCreateMissingDirs = true;
                            break;

                        case "verbosity":
                            // Verbosity
                            currVerbosity = currOpt.getValue();
                            break;

                        case "stdout-file":
                            // Standard Output
                            currStdOutFile = currOpt.getValue();
                            break;

                        case "preferred-dir":
                            // Preferred directory
                            currPreferredDir = currOpt.getValue();
                            break;
                    }
                }
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
            FSSVerbosityEnum currVerbose;
            if( StringUtils.isBlank(currVerbosity) && (config != null) && (config.getVerbosity() != null) )
            {
                currVerbose = config.getVerbosity();
            }
            else
            {
                currVerbose = FSSVerbosityEnum.from(currVerbosity);
            }

            if( currVerbose != null )
            {
                FSSLog4JConfiguration.setVerbosity(currVerbose);
            }

            /*
              Or use -DFSSOUTREDIRECT=/path/to/file
             */
            Path usedStdOut = null;
            if( (config != null)
                    && (config.getStdOutFile() != null) )
            {
                usedStdOut = config.getStdOutFile();
            }
            else if( StringUtils.isNoneBlank(currStdOutFile) )
            {
                usedStdOut = Paths.get(currStdOutFile);
            }

            if( usedStdOut != null )
            {
                LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
                FSSLog4JConfiguration logConf = (FSSLog4JConfiguration)ctx.getConfiguration();

                FSSLog4JConfiguration.outputRedirect(usedStdOut);
            }

            config = LogCheckConfig.from(config,
                    currService, // service,
                    currEmailOnError, // emailOnError,
                    currSmtpServer,
                    currSmtpPort,
                    currSmtpPass,
                    currSmtpUser,
                    currSmtpProto,
                    currSetName,
                    currDryRun,
                    currShowVersion, // showVersion,
                    currPrintLogs, // printLog,
                    currTailFromEnd, // tailFromEnd,
                    currReadReOpenLogFile, // reOpenLogFile
                    currStoreReOpenLogFile, // --store-reopen-log-file
                    currSaveState,  // saveState
                    null, // collectState
                    currContinue,
                    currStartPositionIgnoreError,
                    currValidateTailerStats,
                    currTailerBackupReadLogs,
                    currTailerBackupReadLogsReverse,
                    currTailerBackupReadPriorLogs,
                    currStopOnEOF,
                    currReadOnlyFileMode,
                    currCreateMissingDirs,
                    currLockFile,
                    currLogPath,
                    currStoreLogFile,
                    currStatusFile,
                    currStateFile,
                    currProcessedLogsFile,
                    currErrorFile,
                    null, // configFilePath,
                    null, // holdingDir
                    currDeDupeDirPath,
                    currTailerBackupLogDir,
                    currPreferredDir,
                    currStdOutFile,
                    currElasticsearchUrl,
                    null, // elasticsearchIndexName,
                    null, // elasticsearchIndexPrefix,
                    null, // elasticsearchLogType,
                    null, // elasticsearchIndexNameFormat,
                    currLogCutoffDate, // logCutoffDate,
                    currLogCutoffDuration, // logCutoffDuration,
                    currLogDeduplicationDuration, // logDeduplicationDuration,
                    currPollIntervalSeconds,
                    currStopAfter,
                    currDeDupeIgnoreUntilCount,
                    currDeDupeSkipUntilCount,
                    currReadLogFileCount,
                    currReadMaxDeDupeEntries,
                    currIdBlockSize,
                    currDeDupeMaxLogsBeforeWrite,
                    currDeDupeMaxLogsPerFile,
                    currDeDupeMaxLogFiles,
                    currDeDupeIgnoreUntilPercent,
                    currDeDupeSkipUntilPercent,
                    currVerbosity,
                    currDeDupeDefaultAction,
                    currLEBuilderType,
                    currLEStoreType,
                    currTailerBackupLogNameComps,
                    currIdBlockHashtype,
                    currTailerBackupLogCompression,
                    currTailerBackupLogNameRegex,
                    currDebugFlags);
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
