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
package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.enums.LCIndexNameFormat;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;

/**
 * The main configuration class for the LogCheck application.
 *
 * @author kervin
 */
public final class LogCheckConfig
{
    private static final org.apache.logging.log4j.Logger log 
                             = LogManager.getLogger(LogCheckConfig.class);
    
    private final Boolean service;
    private final Long pollIntervalSeconds;
    private final String emailOnError;
    private final String smtpServer;
    private final String smtpPort;
    private final String smtpPass;
    private final String smtpUser;
    private final String smtpProto;
    private final Boolean dryRun;
    private final Boolean showVersion;
    private final Boolean tailFromEnd;
    private final Boolean printLog;
    private final Boolean saveState;
    private final Boolean continueState;
    private final Path lockFilePath;
    private final Path logPath;
    private final Path statusFilePath;
    private final Path stateFilePath;
    private final Path configFilePath;
    private final Path holdingDirPath;
    private final URL elasticsearchURL;
    private final String elasticsearchIndexName;
    private final String elasticsearchIndexPrefix;
    private final String elasticsearchLogType;
    private final LocalTime logCutoffDate;
    private final Duration logCutoffDuration;
    private final Duration logDeduplicationDuration;
    private final LCIndexNameFormat elasticsearchIndexNameFormat;
    private final LCLogEntryBuilderType logEntryBuilder;

    public Boolean getSaveState()
    {
        return saveState;
    }

    public Boolean getContinueState()
    {
        return continueState;
    }

    public Path getStateFilePath()
    {
        return stateFilePath;
    }

    public LCLogEntryBuilderType getLogEntryBuilder()
    {
        return logEntryBuilder;
    }

    public String getElasticsearchLogType()
    {
        return elasticsearchLogType;
    }

    public Boolean getPrintLog()
    {
        return printLog;
    }

    public Boolean getService()
    {
        return service;
    }

    public Boolean getDryRun()
    {
        return dryRun;
    }

    public Boolean getShowVersion()
    {
        return showVersion;
    }

    public Boolean getTailFromEnd()
    {
        return tailFromEnd;
    }

    public Duration getLogDeduplicationDuration()
    {
        return logDeduplicationDuration;
    }
    
    public LocalTime getLogCutoffDate()
    {
        return logCutoffDate;
    }

    public Duration getLogCutoffDuration()
    {
        return logCutoffDuration;
    }

    public LCIndexNameFormat getElasticsearchIndexNameFormat()
    {
        return elasticsearchIndexNameFormat;
    }

    public String getElasticsearchIndexName()
    {
        return elasticsearchIndexName;
    }

    public String getElasticsearchIndexPrefix()
    {
        return elasticsearchIndexPrefix;
    }

    public Boolean isTailFromEnd()
    {
        return tailFromEnd;
    }

    public Path getHoldingDirPath()
    {
        return holdingDirPath;
    }

    public Path getConfigFilePath()
    {
        return configFilePath;
    }

    public Boolean isService()
    {
        return service;
    }

    public Long getPollIntervalSeconds()
    {
        return pollIntervalSeconds;
    }

    public String getEmailOnError()
    {
        return emailOnError;
    }

    public String getSmtpServer()
    {
        return smtpServer;
    }

    public String getSmtpPort()
    {
        return smtpPort;
    }

    public String getSmtpPass()
    {
        return smtpPass;
    }

    public String getSmtpUser()
    {
        return smtpUser;
    }

    public String getSmtpProto()
    {
        return smtpProto;
    }

    public Path getLockFilePath()
    {
        return lockFilePath;
    }

    public Path getLogPath()
    {
        return logPath;
    }

    public Path getStatusFilePath()
    {
        return statusFilePath;
    }
    
    public URL getElasticsearchURL()
    {
        return elasticsearchURL;
    }
    
    private LogCheckConfig( final LogCheckConfig orig,
                            final Boolean service,
                            final String emailOnError,
                            final String smtpServer,
                            final String smtpPort,
                            final String smtpPass,
                            final String smtpUser,
                            final String smtpProto,
                            final Boolean dryRun,
                            final Boolean showVersion,
                            final Boolean printLog,
                            final Boolean tailFromEnd,
                            final Boolean saveState,
                            final Boolean continueState,
                            final Path lockFilePath,
                            final Path logPath,
                            final Path statusFilePath,
                            final Path stateFilePath,
                            final Path configFilePath,
                            final Path holdingDirPath,
                            final URL elasticsearchURL,
                            final String elasticsearchIndexName,
                            final String elasticsearchIndexPrefix,
                            final String elasticsearchLogType,
                            final LCIndexNameFormat elasticsearchIndexNameFormat,
                            final LocalTime logCutoffDate,
                            final Duration logCutoffDuration,
                            final Duration logDeduplicationDuration,
                            final Long pollIntervalSeconds,
                            final LCLogEntryBuilderType logEntryBuilder) throws LogCheckException
    {
        if( service != null )
        {
            this.service = service;
        }
        else if( orig != null && orig.isService() != null )
        {
            this.service = orig.isService();
        }
        else
        {
            this.service = false;
        }

        if( logEntryBuilder != null )
        {
            this.logEntryBuilder = logEntryBuilder;
        }
        else if( orig != null && orig.getLogEntryBuilder() != null )
        {
            this.logEntryBuilder = orig.getLogEntryBuilder();
        }
        else
        {
            this.logEntryBuilder = null;
        }

        if( emailOnError != null )
        {
            this.emailOnError = emailOnError;
        }
        else if( orig != null && orig.getEmailOnError() != null )
        {
            this.emailOnError = orig.getEmailOnError();
        }
        else
        {
            this.emailOnError = null;
        }

        if( elasticsearchURL != null )
        {
            this.elasticsearchURL
                    = elasticsearchURL;
        }
        else if( orig != null && orig.getElasticsearchURL() != null )
        {
            this.elasticsearchURL
                    = orig.getElasticsearchURL();
        }
        else
        {
//            try
//            {
//                this.elasticsearchURL
//                        = new URL(LogCheckConstants.DEFAULT_ELASTICSEARCH_URL);
//            }
//            catch(MalformedURLException ex)
//            {
//                String errMsg = String.format("Invalid ElasticSearch URL '%s'",
//                        LogCheckConstants.DEFAULT_ELASTICSEARCH_URL);
//
//                log.debug(errMsg, ex);
//                throw new LogCheckException(errMsg, ex);
//            }

            this.elasticsearchURL = null;
        }

        if( elasticsearchIndexPrefix != null )
        {
            this.elasticsearchIndexPrefix
                    = elasticsearchIndexPrefix;
        }
        else if( orig != null && orig.getElasticsearchIndexPrefix() != null )
        {
            this.elasticsearchIndexPrefix
                    = orig.getElasticsearchIndexPrefix();
        }
        else
        {
            this.elasticsearchIndexPrefix
                    = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_PREFIX;
        }

        if( elasticsearchIndexNameFormat  != null )
        {
            this.elasticsearchIndexNameFormat
                    = elasticsearchIndexNameFormat ;
        }
        else if( orig != null
                && orig.getElasticsearchIndexNameFormat() != null )
        {
            this.elasticsearchIndexNameFormat
                    = orig.getElasticsearchIndexNameFormat();
        }
        else
        {
            this.elasticsearchIndexNameFormat
                    = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT;
        }

        if( elasticsearchLogType  != null )
        {
            this.elasticsearchLogType
                    = elasticsearchLogType;
        }
        else if( orig != null
                && orig.getElasticsearchLogType() != null )
        {
            this.elasticsearchLogType
                    = orig.getElasticsearchLogType();
        }
        else
        {
            this.elasticsearchLogType
                    = LogCheckConstants.DEFAULT_LOG_TYPE;
        }

        if( elasticsearchIndexName != null )
        {
            this.elasticsearchIndexName = elasticsearchIndexName;
        }
        else if( orig != null && orig.isService() != null )
        {
            this.elasticsearchIndexName = orig.getElasticsearchIndexName();
        }
        else
        {
            this.elasticsearchIndexName = null;
        }

        if( smtpServer != null )
        {
            this.smtpServer = smtpServer;
        }
        else if( orig != null && orig.getSmtpServer() != null )
        {
            this.smtpServer = orig.getSmtpServer();
        }
        else
        {
            this.smtpServer = null;
        }

        if( smtpPort != null )
        {
            this.smtpPort = smtpPort;
        }
        else if( orig != null && orig.getSmtpPort() != null )
        {
            this.smtpPort = orig.getSmtpPort();
        }
        else
        {
            this.smtpPort = null;
        }

        if( smtpPass != null )
        {
            this.smtpPass = smtpPass;
        }
        else if( orig != null && orig.getSmtpPass() != null )
        {
            this.smtpPass = orig.getSmtpPass();
        }
        else
        {
            this.smtpPass = null;
        }

        if( smtpUser != null )
        {
            this.smtpUser = smtpUser;
        }
        else if( orig != null && orig.getSmtpUser() != null )
        {
            this.smtpUser = orig.getSmtpUser();
        }
        else
        {
            this.smtpUser = null;
        }

        if( smtpProto != null )
        {
            this.smtpProto = smtpProto;
        }
        else if( orig != null && orig.getSmtpProto() != null )
        {
            this.smtpProto = orig.getSmtpProto();
        }
        else
        {
            this.smtpProto = null;
        }

        if( dryRun != null )
        {
            this.dryRun = dryRun;
        }
        else if( orig != null && orig.getDryRun() != null )
        {
            this.dryRun = orig.getDryRun();
        }
        else
        {
            this.dryRun = null;
        }

        if( showVersion != null )
        {
            this.showVersion = showVersion;
        }
        else if( orig != null && orig.getShowVersion() != null )
        {
            this.showVersion = orig.getShowVersion();
        }
        else
        {
            this.showVersion = null;
        }

        if( lockFilePath != null )
        {
            this.lockFilePath = lockFilePath;
        }
        else if( orig != null && orig.getLockFilePath() != null )
        {
            this.lockFilePath = orig.getLockFilePath();
        }
        else
        {
            this.lockFilePath = null;
        }

        if( logPath != null )
        {
            this.logPath = logPath;
        }
        else if( orig != null && orig.getLogPath() != null )
        {
            this.logPath = orig.getLogPath();
        }
        else
        {
            this.logPath = null;
        }

        if( statusFilePath != null )
        {
            this.statusFilePath = statusFilePath;
        }
        else if( orig != null && orig.getStatusFilePath() != null )
        {
            this.statusFilePath = orig.getStatusFilePath();
        }
        else
        {
            this.statusFilePath = null;
        }

        if( stateFilePath != null )
        {
            this.stateFilePath = stateFilePath;
        }
        else if( orig != null && orig.getStateFilePath() != null )
        {
            this.stateFilePath = orig.getStateFilePath();
        }
        else
        {
            this.stateFilePath = null;
        }

        if( configFilePath != null )
        {
            this.configFilePath = configFilePath;
        }
        else if( orig != null && orig.getConfigFilePath() != null )
        {
            this.configFilePath = orig.getConfigFilePath();
        }
        else
        {
            this.configFilePath = null;
        }

        if( holdingDirPath != null )
        {
            this.holdingDirPath = holdingDirPath;
        }
        else if( orig != null && orig.getHoldingDirPath() != null )
        {
            this.holdingDirPath = orig.getHoldingDirPath();
        }
        else
        {
            this.holdingDirPath = null;
        }

        if( logCutoffDate != null )
        {
            this.logCutoffDate = logCutoffDate;
        }
        else if( orig != null && orig.getLogCutoffDate() != null )
        {
            this.logCutoffDate = orig.getLogCutoffDate();
        }
        else
        {
            this.logCutoffDate = null;
        }

        if( logCutoffDuration != null )
        {
            this.logCutoffDuration = logCutoffDuration;
        }
        else if( orig != null && orig.getLogCutoffDuration() != null )
        {
            this.logCutoffDuration = orig.getLogCutoffDuration();
        }
        else
        {
            this.logCutoffDuration = null;
        }

        if( logDeduplicationDuration != null )
        {
            this.logDeduplicationDuration
                    = logDeduplicationDuration;
        }
        else if( orig != null && orig.getLogDeduplicationDuration() != null )
        {
            this.logDeduplicationDuration
                    = orig.getLogDeduplicationDuration();
        }
        else
        {
            this.logDeduplicationDuration = null;
        }

        if( printLog != null )
        {
            this.printLog = printLog;
        }
        else if( orig != null && orig.getPrintLog() != null )
        {
            this.printLog = orig.getPrintLog();
        }
        else
        {
            this.printLog = null;
        }

        if( saveState != null )
        {
            this.saveState = saveState;
        }
        else if( orig != null && orig.getSaveState() != null )
        {
            this.saveState = orig.getSaveState();
        }
        else
        {
            this.saveState = null;
        }

        if( continueState != null )
        {
            this.continueState = continueState;
        }
        else if( orig != null && orig.getContinueState() != null )
        {
            this.continueState = orig.getContinueState();
        }
        else
        {
            this.continueState = null;
        }

        if( pollIntervalSeconds != null )
        {
            this.pollIntervalSeconds = pollIntervalSeconds;
        }
        else if( orig != null && orig.getPollIntervalSeconds() != null )
        {
            this.pollIntervalSeconds = orig.getPollIntervalSeconds();
        }
        else
        {
            this.pollIntervalSeconds = null;
        }

        if( tailFromEnd != null )
        {
            this.tailFromEnd = tailFromEnd;
        }
        else if( orig != null && orig.getTailFromEnd() != null )
        {
            this.tailFromEnd = orig.getTailFromEnd();
        }
        else
        {
            this.tailFromEnd = null;
        }
    }

    /**
     * Create a new instance of the LogCheck configuration.
     *
     * Parse most of the options from strings.
    **/
     public static LogCheckConfig from( final LogCheckConfig orig,
                                       final Boolean service,
                                       final String emailOnError,
                                       final String smtpServer,
                                       final String smtpPort,
                                       final String smtpPass,
                                       final String smtpUser,
                                       final String smtpProto,
                                       final Boolean dryRun,
                                       final Boolean showVersion,
                                       final Boolean printLog,
                                       final Boolean tailFromEnd,
                                       final Boolean saveState,
                                       final Boolean continueState,
                                       final String lockFilePathStr,
                                       final String logPathStr,
                                       final String statusFilePathStr,
                                       final String stateFilePathStr,
                                       final String configFilePathStr,
                                       final String holdingDirPathStr,
                                       final String elasticsearchURLStr,
                                       final String elasticsearchIndexName,
                                       final String elasticsearchIndexPrefix,
                                       final String elasticsearchLogType,
                                       final String elasticsearchIndexNameFormatStr,
                                       final String logCutoffDateStr,
                                       final String logCutoffDurationStr,
                                       final String logDeduplicationDurationStr,
                                       final String pollIntervalSecondsStr,
                                       final String logEntryBuilderStr) throws LogCheckException
    {
        Path lockFilePath = null;
        Path logPath = null;
        Path statusFilePath = null;
        Path stateFilePath = null;
        Path configFilePath = null;
        Path holdingDirPath = null;
        URL elasticsearchURL = null;
        LCIndexNameFormat elasticsearchIndexNameFormat = null;
        LocalTime logCutoffDate = null;
        Duration logCutoffDuration = null;
        Duration logDeduplicationDuration = null;
        Long pollIntervalSeconds = null;
        LCLogEntryBuilderType logEntryBuilder  = null;

        if(StringUtils.isNoneBlank(lockFilePathStr))
        {
            lockFilePath = Paths.get(lockFilePathStr);
        }

        if(StringUtils.isNoneBlank(logPathStr))
        {
            logPath = Paths.get(logPathStr);
        }

        if(StringUtils.isNoneBlank(statusFilePathStr))
        {
            statusFilePath = Paths.get(statusFilePathStr);
        }

        if(StringUtils.isNoneBlank(stateFilePathStr))
        {
            stateFilePath = Paths.get(stateFilePathStr);
        }

        if(StringUtils.isNoneBlank(configFilePathStr))
        {
            configFilePath = Paths.get(configFilePathStr);
        }

        if(StringUtils.isNoneBlank(holdingDirPathStr))
        {
            holdingDirPath = Paths.get(holdingDirPathStr);
        }

        if(StringUtils.isNoneBlank(elasticsearchURLStr))
        {
            try
            {
                elasticsearchURL = new URL(elasticsearchURLStr);
            }
            catch(MalformedURLException ex)
            {
                String errMsg = String.format("Invalid URL string '%s'", elasticsearchURLStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(elasticsearchIndexNameFormatStr))
        {
            elasticsearchIndexNameFormat
                    = LCIndexNameFormat.from(elasticsearchIndexNameFormatStr);
        }

        if(StringUtils.isNoneBlank(logCutoffDateStr))
        {
            logCutoffDate = LocalTime.parse(logCutoffDateStr);
        }

        if(StringUtils.isNoneBlank(logCutoffDurationStr))
        {
            logCutoffDuration = Duration.parse(logCutoffDurationStr);
        }

        if(StringUtils.isNoneBlank(logDeduplicationDurationStr))
        {
            logDeduplicationDuration
                    = Duration.parse(logDeduplicationDurationStr);
        }

        if(StringUtils.isNoneBlank(pollIntervalSecondsStr))
        {
            pollIntervalSeconds = Long.parseLong(pollIntervalSecondsStr);
        }

        if(StringUtils.isNoneBlank(logEntryBuilderStr))
        {
            logEntryBuilder = LCLogEntryBuilderType.from(logEntryBuilderStr);
        }

        LogCheckConfig res = new LogCheckConfig(orig,
                service,
                emailOnError,
                smtpServer,
                smtpPort,
                smtpPass,
                smtpUser,
                smtpProto,
                dryRun,
                showVersion,
                printLog,
                saveState,
                continueState,
                tailFromEnd,
                lockFilePath,
                logPath,
                statusFilePath,
                stateFilePath,
                configFilePath,
                holdingDirPath,
                elasticsearchURL,
                elasticsearchIndexName,
                elasticsearchIndexPrefix,
                elasticsearchLogType,
                elasticsearchIndexNameFormat,
                logCutoffDate,
                logCutoffDuration,
                logDeduplicationDuration,
                pollIntervalSeconds,
                logEntryBuilder);

        return res;
    }

    public static LogCheckConfig from( final LogCheckConfig orig,
                            final Boolean service,
                            final String emailOnError,
                            final String smtpServer,
                            final String smtpPort,
                            final String smtpPass,
                            final String smtpUser,
                            final String smtpProto,
                            final Boolean dryRun,
                            final Boolean showVersion,
                            final Boolean printLog,
                            final Boolean tailFromEnd,
                            final Boolean saveState,
                            final Boolean continueState,
                            final Path lockFilePath,
                            final Path logPath,
                            final Path statusFilePath,
                            final Path stateFilePath,
                            final Path configFilePath,
                            final Path holdingDirPath,
                            final URL elasticsearchURL,
                            final String elasticsearchIndexName,
                            final String elasticsearchIndexPrefix,
                            final String elasticsearchLogType,
                            final LCIndexNameFormat elasticsearchIndexNameFormat,
                            final LocalTime logCutoffDate,
                            final Duration logCutoffDuration,
                            final Duration logDeduplicationDuration,
                            final Long pollIntervalSeconds,
                            final LCLogEntryBuilderType logEntryBuilder) throws LogCheckException
    {
        LogCheckConfig res = new LogCheckConfig(orig,
                service,
                emailOnError,
                smtpServer,
                smtpPort,
                smtpPass,
                smtpUser,
                smtpProto,
                dryRun,
                showVersion,
                printLog,
                saveState,
                continueState,
                tailFromEnd,
                lockFilePath,
                logPath,
                statusFilePath,
                stateFilePath,
                configFilePath,
                holdingDirPath,
                elasticsearchURL,
                elasticsearchIndexName,
                elasticsearchIndexPrefix,
                elasticsearchLogType,
                elasticsearchIndexNameFormat,
                logCutoffDate,
                logCutoffDuration,
                logDeduplicationDuration,
                pollIntervalSeconds,
                logEntryBuilder);

        return res;
    }
}
