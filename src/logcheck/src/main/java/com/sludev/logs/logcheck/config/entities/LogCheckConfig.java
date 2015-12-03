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

import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCIndexNameFormat;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.enums.LCLogEntryStoreType;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.ParseNumberWithSuffix;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private final Long stopAfter;
    private final Integer idBlockSize;
    private final Integer deDupeMaxLogsPerFile;
    private final Integer deDupeMaxLogsBeforeWrite;
    private final Integer deDupeMaxLogFiles;
    private final Integer readLogFileCount;
    private final Integer readMaxDeDupeEntries;
    private final String emailOnError;
    private final String smtpServer;
    private final String smtpPort;
    private final String smtpPass;
    private final String smtpUser;
    private final String smtpProto;
    private final String setName;
    private final Boolean dryRun;
    private final Boolean showVersion;
    private final Boolean tailFromEnd;
    private final Boolean printLog;
    private final Boolean saveState;
    private final Boolean continueState;
    private final Boolean readReOpenLogFile;
    private final Boolean storeReOpenLogFile;
    private final Boolean startPositionIgnoreError;
    private final Path lockFilePath;
    private final Path logPath;
    private final Path storeLogPath;
    private final Path statusFilePath;
    private final Path stateFilePath;
    private final Path errorFilePath;
    private final Path configFilePath;
    private final Path holdingDirPath;
    private final Path deDupeDirPath;
    private final URL elasticsearchURL;
    private final String elasticsearchIndexName;
    private final String elasticsearchIndexPrefix;
    private final String elasticsearchLogType;
    private final LocalTime logCutoffDate;
    private final Duration logCutoffDuration;
    private final Duration logDeduplicationDuration;
    private final LCIndexNameFormat elasticsearchIndexNameFormat;
    private final List<LCLogEntryBuilderType> logEntryBuilders;
    private final List<LCLogEntryStoreType> logEntryStores;
    private final LCHashType idBlockHashType;

    public Path getErrorFilePath()
    {
        return errorFilePath;
    }

    public Integer getIdBlockSize()
    {
        return idBlockSize;
    }

    public LCHashType getIdBlockHashType()
    {
        return idBlockHashType;
    }

    public Boolean getSaveState()
    {
        return saveState;
    }

    public Boolean getContinueState()
    {
        return continueState;
    }

    public Boolean getStartPositionIgnoreError()
    {
        return startPositionIgnoreError;
    }

    public Path getStateFilePath()
    {
        return stateFilePath;
    }

    public List<LCLogEntryBuilderType> getLogEntryBuilders()
    {
        return logEntryBuilders;
    }

    public List<LCLogEntryStoreType> getLogEntryStores()
    {
        return logEntryStores;
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

    public String getSetName()
    {
        return setName;
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

    public Path getDeDupeDirPath()
    {
        return deDupeDirPath;
    }

    public Boolean isService()
    {
        return service;
    }

    public Boolean getReadReOpenLogFile()
    {
        return readReOpenLogFile;
    }

    public Boolean getStoreReOpenLogFile()
    {
        return storeReOpenLogFile;
    }

    public Path getStoreLogPath()
    {
        return storeLogPath;
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

    public Integer getDeDupeMaxLogsPerFile()
    {
        return deDupeMaxLogsPerFile;
    }

    public Integer getDeDupeMaxLogsBeforeWrite()
    {
        return deDupeMaxLogsBeforeWrite;
    }

    public Integer getDeDupeMaxLogFiles()
    {
        return deDupeMaxLogFiles;
    }

    public Integer getReadLogFileCount()
    {
        return readLogFileCount;
    }

    public Integer getReadMaxDeDupeEntries()
    {
        return readMaxDeDupeEntries;
    }

    public Long getStopAfter()
    {
        return stopAfter;
    }

    private LogCheckConfig(final LogCheckConfig orig,
                           final Boolean service,
                           final String emailOnError,
                           final String smtpServer,
                           final String smtpPort,
                           final String smtpPass,
                           final String smtpUser,
                           final String smtpProto,
                           final String setName,
                           final Boolean dryRun,
                           final Boolean showVersion,
                           final Boolean printLog,
                           final Boolean tailFromEnd,
                           final Boolean readReOpenLogFile,
                           final Boolean storeReOpenLogFile,
                           final Boolean saveState,
                           final Boolean continueState,
                           final Boolean startPositionIgnoreError,
                           final Path lockFilePath,
                           final Path logPath,
                           final Path storeLogPath,
                           final Path statusFilePath,
                           final Path stateFilePath,
                           final Path errorFilePath,
                           final Path configFilePath,
                           final Path holdingDirPath,
                           final Path deDupeDirPath,
                           final URL elasticsearchURL,
                           final String elasticsearchIndexName,
                           final String elasticsearchIndexPrefix,
                           final String elasticsearchLogType,
                           final LCIndexNameFormat elasticsearchIndexNameFormat,
                           final LocalTime logCutoffDate,
                           final Duration logCutoffDuration,
                           final Duration logDeduplicationDuration,
                           final Long pollIntervalSeconds,
                           final Long stopAfter,
                           final Integer readLogFileCount,
                           final Integer readMaxDeDupeEntries,
                           final Integer idBlockSize,
                           final Integer deDupeMaxLogsBeforeWrite,
                           final Integer deDupeMaxLogsPerFile,
                           final Integer deDupeMaxLogFiles,
                           final List<LCLogEntryBuilderType> logEntryBuilders,
                           final List<LCLogEntryStoreType> logEntryStores,
                           final LCHashType idBlockHashType) throws LogCheckException
    {
        if( logEntryStores != null )
        {
            this.logEntryStores = logEntryStores;
        }
        else if( orig != null && orig.getLogEntryStores() != null )
        {
            this.logEntryStores = orig.getLogEntryStores();
        }
        else
        {
            this.logEntryStores = null;
        }

        if( storeReOpenLogFile != null )
        {
            this.storeReOpenLogFile = storeReOpenLogFile;
        }
        else if( orig != null && orig.getStoreReOpenLogFile() != null )
        {
            this.storeReOpenLogFile = orig.getStoreReOpenLogFile();
        }
        else
        {
            this.storeReOpenLogFile = false;
        }

        if( storeLogPath != null )
        {
            this.storeLogPath = storeLogPath;
        }
        else if( orig != null && orig.getStoreLogPath() != null )
        {
            this.storeLogPath = orig.getStoreLogPath();
        }
        else
        {
            this.storeLogPath = null;
        }

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

        if( deDupeMaxLogsBeforeWrite != null )
        {
            this.deDupeMaxLogsBeforeWrite = deDupeMaxLogsBeforeWrite;
        }
        else if( orig != null && orig.getDeDupeMaxLogsBeforeWrite() != null )
        {
            this.deDupeMaxLogsBeforeWrite = orig.getDeDupeMaxLogsBeforeWrite();
        }
        else
        {
            this.deDupeMaxLogsBeforeWrite = LogCheckConstants.DEFAULT_DEDUPE_LOGS_BEFORE_WRITE;
        }

        if( deDupeMaxLogsPerFile != null )
        {
            this.deDupeMaxLogsPerFile = deDupeMaxLogsPerFile;
        }
        else if( orig != null && orig.getDeDupeMaxLogsPerFile() != null )
        {
            this.deDupeMaxLogsPerFile = orig.getDeDupeMaxLogsPerFile();
        }
        else
        {
            this.deDupeMaxLogsPerFile = LogCheckConstants.MAX_DEDUPE_LOGS_PER_FILE;
        }

        if( deDupeMaxLogFiles != null )
        {
            this.deDupeMaxLogFiles = deDupeMaxLogFiles;
        }
        else if( orig != null && orig.getDeDupeMaxLogFiles() != null )
        {
            this.deDupeMaxLogFiles = orig.getDeDupeMaxLogFiles();
        }
        else
        {
            this.deDupeMaxLogFiles = LogCheckConstants.DEFAULT_DEDUPE_LOG_FILES_ROTATE;
        }

        if( idBlockSize != null )
        {
            this.idBlockSize = idBlockSize;
        }
        else if( orig != null && orig.getIdBlockSize() != null )
        {
            this.idBlockSize = orig.getIdBlockSize();
        }
        else
        {
            this.idBlockSize = LogCheckConstants.DEFAULT_ID_BLOCK_SIZE;
        }

        if( idBlockHashType != null )
        {
            this.idBlockHashType = idBlockHashType;
        }
        else if( orig != null && orig.getIdBlockHashType() != null )
        {
            this.idBlockHashType = orig.getIdBlockHashType();
        }
        else
        {
            this.idBlockHashType = LCHashType.SHA2;
        }

        if( logEntryBuilders != null )
        {
            this.logEntryBuilders = logEntryBuilders;
        }
        else if( orig != null && orig.getLogEntryBuilders() != null )
        {
            this.logEntryBuilders = orig.getLogEntryBuilders();
        }
        else
        {
            this.logEntryBuilders = null;
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

        if( startPositionIgnoreError != null )
        {
            this.startPositionIgnoreError = startPositionIgnoreError;
        }
        else if( orig != null && orig.getStartPositionIgnoreError() != null )
        {
            this.startPositionIgnoreError = orig.getStartPositionIgnoreError();
        }
        else
        {
            this.startPositionIgnoreError = null;
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

        if( StringUtils.isNoneBlank(setName) )
        {
            this.setName = setName;
        }
        else if( orig != null && StringUtils.isNoneBlank(orig.getSetName()) )
        {
            this.setName = orig.getSetName();
        }
        else
        {
            this.setName = LogCheckConstants.DEFAULT_SET_NAME;
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

        if( errorFilePath != null )
        {
            this.errorFilePath = errorFilePath;
        }
        else if( orig != null && orig.getErrorFilePath() != null )
        {
            this.errorFilePath = orig.getErrorFilePath();
        }
        else
        {
            this.errorFilePath = null;
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

        if( deDupeDirPath != null )
        {
            this.deDupeDirPath = deDupeDirPath;
        }
        else if( orig != null && orig.getDeDupeDirPath() != null )
        {
            this.deDupeDirPath = orig.getDeDupeDirPath();
        }
        else
        {
            this.deDupeDirPath = null;
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

        if( stopAfter != null )
        {
            this.stopAfter = stopAfter;
        }
        else if( orig != null && orig.getStopAfter() != null )
        {
            this.stopAfter = orig.getStopAfter();
        }
        else
        {
            this.stopAfter = null;
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

        if( readReOpenLogFile != null )
        {
            this.readReOpenLogFile = readReOpenLogFile;
        }
        else if( orig != null && orig.getReadReOpenLogFile() != null )
        {
            this.readReOpenLogFile = orig.getReadReOpenLogFile();
        }
        else
        {
            this.readReOpenLogFile = null;
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

        if( readLogFileCount != null )
        {
            this.readLogFileCount = readLogFileCount;
        }
        else if( orig != null && orig.getReadLogFileCount() != null )
        {
            this.readLogFileCount = orig.getReadLogFileCount();
        }
        else
        {
            this.readLogFileCount = null;
        }

        if( getReadMaxDeDupeEntries() != null )
        {
            this.readMaxDeDupeEntries = readMaxDeDupeEntries;
        }
        else if( orig != null && orig.getReadMaxDeDupeEntries() != null )
        {
            this.readMaxDeDupeEntries = orig.getReadMaxDeDupeEntries();
        }
        else
        {
            this.readMaxDeDupeEntries = null;
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

    public static LogCheckConfig from(final LogCheckConfig orig,
                                      final Boolean service,
                                      final String emailOnError,
                                      final String smtpServer,
                                      final String smtpPort,
                                      final String smtpPass,
                                      final String smtpUser,
                                      final String smtpProto,
                                      final String setName,
                                      final Boolean dryRun,
                                      final Boolean showVersion,
                                      final Boolean printLog,
                                      final Boolean tailFromEnd,
                                      final Boolean readReOpenLogFile,
                                      final Boolean storeReOpenLogFile,
                                      final Boolean saveState,
                                      final Boolean continueState,
                                      final Boolean startPositionIgnoreError,
                                      final Path lockFilePath,
                                      final Path logPath,
                                      final Path storeLogPath,
                                      final Path statusFilePath,
                                      final Path stateFilePath,
                                      final Path errorFilePath,
                                      final Path configFilePath,
                                      final Path holdingDirPath,
                                      final Path deDupeDirPath,
                                      final URL elasticsearchURL,
                                      final String elasticsearchIndexName,
                                      final String elasticsearchIndexPrefix,
                                      final String elasticsearchLogType,
                                      final LCIndexNameFormat elasticsearchIndexNameFormat,
                                      final LocalTime logCutoffDate,
                                      final Duration logCutoffDuration,
                                      final Duration logDeduplicationDuration,
                                      final Long pollIntervalSeconds,
                                      final Long stopAfter,
                                      final Integer readLogFileCount,
                                      final Integer readMaxDeDupeEntries,
                                      final Integer idBlockSize,
                                      final Integer deDupeMaxLogsBeforeWrite,
                                      final Integer deDupeMaxLogsPerFile,
                                      final Integer deDupeMaxLogFiles,
                                      final List<LCLogEntryBuilderType> logEntryBuilders,
                                      final List<LCLogEntryStoreType> logEntryStores,
                                      final LCHashType idBlockHashType) throws LogCheckException
    {
        LogCheckConfig res = new LogCheckConfig(orig,
                service,
                emailOnError,
                smtpServer,
                smtpPort,
                smtpPass,
                smtpUser,
                smtpProto,
                setName,
                dryRun,
                showVersion,
                printLog,
                tailFromEnd,
                readReOpenLogFile,
                storeReOpenLogFile,
                saveState,
                continueState,
                startPositionIgnoreError,
                lockFilePath,
                logPath,
                storeLogPath,
                statusFilePath,
                stateFilePath,
                errorFilePath,
                configFilePath,
                holdingDirPath,
                deDupeDirPath,
                elasticsearchURL,
                elasticsearchIndexName,
                elasticsearchIndexPrefix,
                elasticsearchLogType,
                elasticsearchIndexNameFormat,
                logCutoffDate,
                logCutoffDuration,
                logDeduplicationDuration,
                pollIntervalSeconds,
                stopAfter,
                readLogFileCount,
                readMaxDeDupeEntries,
                idBlockSize,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile,
                deDupeMaxLogFiles,
                logEntryBuilders,
                logEntryStores,
                idBlockHashType);

        return res;
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
                                       final String setName,
                                       final Boolean dryRun,
                                       final Boolean showVersion,
                                       final Boolean printLog,
                                       final Boolean tailFromEnd,
                                       final Boolean readReOpenLogFile,
                                        final Boolean storeReOpenLogFile,
                                       final Boolean saveState,
                                       final Boolean continueState,
                                        final Boolean startPositionIgnoreError,
                                       final String lockFilePathStr,
                                       final String logPathStr,
                                        final String storeLogPathStr,
                                       final String statusFilePathStr,
                                       final String stateFilePathStr,
                                       final String errorFilePathStr,
                                       final String configFilePathStr,
                                       final String holdingDirPathStr,
                                       final String deDupeDirPathStr,
                                       final String elasticsearchURLStr,
                                       final String elasticsearchIndexName,
                                       final String elasticsearchIndexPrefix,
                                       final String elasticsearchLogType,
                                       final String elasticsearchIndexNameFormatStr,
                                       final String logCutoffDateStr,
                                       final String logCutoffDurationStr,
                                       final String logDeduplicationDurationStr,
                                       final String pollIntervalSecondsStr,
                                        final String stopAfterStr,
                                        final String readLogFileCountStr,
                                        final String readMaxDeDupeEntriesStr,
                                       final String idBlockSizeStr,
                                        final String deDupeMaxLogsBeforeWriteStr,
                                        final String deDupeMaxLogsPerFileStr,
                                        final String deDupeMaxLogFilesStr,
                                       final String[] logEntryBuilderStrs,
                                        final String[] logEntryStoreStrs,
                                       final String idBlockHashTypeStr) throws LogCheckException
    {
        Path lockFilePath = null;
        Path logPath = null;
        Path storeLogPath = null;
        Path statusFilePath = null;
        Path stateFilePath = null;
        Path errorFilePath = null;
        Path configFilePath = null;
        Path holdingDirPath = null;
        Path deDupeDirPath = null;
        URL elasticsearchURL = null;
        LCIndexNameFormat elasticsearchIndexNameFormat = null;
        LocalTime logCutoffDate = null;
        Duration logCutoffDuration = null;
        Duration logDeduplicationDuration = null;
        Long pollIntervalSeconds = null;
        List<LCLogEntryBuilderType> logEntryBuilders  = new ArrayList<>();
        List<LCLogEntryStoreType> logEntryStores = new ArrayList<>();
        Integer readLogFileCount = null;
        Integer readMaxDeDupeEntries = null;
        Integer idBlockSize = null;
        Integer deDupeMaxLogsBeforeWrite = null;
        Integer deDupeMaxLogsPerFile = null;
        Integer deDupeMaxLogFiles = null;
        Long stopAfter = null;
        LCHashType idBlockHash = null;

        if(StringUtils.isNoneBlank(lockFilePathStr))
        {
            lockFilePath = Paths.get(lockFilePathStr);
        }

        if(StringUtils.isNoneBlank(logPathStr))
        {
            logPath = Paths.get(logPathStr);
        }

        if(StringUtils.isNoneBlank(storeLogPathStr))
        {
            storeLogPath = Paths.get(storeLogPathStr);
        }

        if(StringUtils.isNoneBlank(statusFilePathStr))
        {
            statusFilePath = Paths.get(statusFilePathStr);
        }

        if(StringUtils.isNoneBlank(stateFilePathStr))
        {
            stateFilePath = Paths.get(stateFilePathStr);
        }

        if(StringUtils.isNoneBlank(errorFilePathStr))
        {
            errorFilePath = Paths.get(errorFilePathStr);
        }

        if(StringUtils.isNoneBlank(configFilePathStr))
        {
            configFilePath = Paths.get(configFilePathStr);
        }

        if(StringUtils.isNoneBlank(holdingDirPathStr))
        {
            holdingDirPath = Paths.get(holdingDirPathStr);
        }

        if(StringUtils.isNoneBlank(deDupeDirPathStr))
        {
            deDupeDirPath = Paths.get(deDupeDirPathStr);
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
            try
            {
                pollIntervalSeconds = Long.parseLong(pollIntervalSecondsStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing long '%s'", pollIntervalSecondsStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(logEntryBuilderStrs != null )
        {
            for( String b : logEntryBuilderStrs )
            {
                if( StringUtils.isNoneBlank(b) )
                {
                    logEntryBuilders.add(LCLogEntryBuilderType.from(b));
                }
            }
        }

        if(logEntryStoreStrs != null )
        {
            for( String s : logEntryStoreStrs )
            {
                if( StringUtils.isNoneBlank(s) )
                {
                    logEntryStores.add(LCLogEntryStoreType.from(s));
                }
            }
        }

        if(StringUtils.isNoneBlank(idBlockSizeStr))
        {
            try
            {
                idBlockSize = Integer.parseInt(idBlockSizeStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", idBlockSizeStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(deDupeMaxLogsBeforeWriteStr))
        {
            try
            {
                deDupeMaxLogsBeforeWrite = Integer.parseInt(deDupeMaxLogsBeforeWriteStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeMaxLogsBeforeWriteStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(deDupeMaxLogsPerFileStr))
        {
            try
            {
                deDupeMaxLogsPerFile = Integer.parseInt(deDupeMaxLogsPerFileStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeMaxLogsPerFileStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(deDupeMaxLogFilesStr))
        {
            try
            {
                deDupeMaxLogFiles = Integer.parseInt(deDupeMaxLogFilesStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeMaxLogFilesStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(readLogFileCountStr))
        {
            try
            {
                readLogFileCount = Integer.parseInt(readLogFileCountStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", readLogFileCountStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(readMaxDeDupeEntriesStr))
        {
            try
            {
                readMaxDeDupeEntries = Integer.parseInt(readMaxDeDupeEntriesStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", readMaxDeDupeEntriesStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(stopAfterStr))
        {
            try
            {
                Pair<Long,TimeUnit> stop = ParseNumberWithSuffix.parseIntWithTimeUnits(stopAfterStr);
                stopAfter = stop.getRight().toSeconds(stop.getLeft());
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", stopAfterStr);

                log.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        LogCheckConfig res = LogCheckConfig.from(orig,
                service,
                emailOnError,
                smtpServer,
                smtpPort,
                smtpPass,
                smtpUser,
                smtpProto,
                setName,
                dryRun,
                showVersion,
                printLog,
                tailFromEnd,
                readReOpenLogFile,
                storeReOpenLogFile,
                saveState,
                continueState,
                startPositionIgnoreError,
                lockFilePath,
                logPath,
                storeLogPath,
                statusFilePath,
                stateFilePath,
                errorFilePath,
                configFilePath,
                holdingDirPath,
                deDupeDirPath,
                elasticsearchURL,
                elasticsearchIndexName,
                elasticsearchIndexPrefix,
                elasticsearchLogType,
                elasticsearchIndexNameFormat,
                logCutoffDate,
                logCutoffDuration,
                logDeduplicationDuration,
                pollIntervalSeconds,
                stopAfter,
                readLogFileCount,
                readMaxDeDupeEntries,
                idBlockSize,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile,
                deDupeMaxLogFiles,
                logEntryBuilders,
                logEntryStores,
                idBlockHash);

        return res;
    }
}
