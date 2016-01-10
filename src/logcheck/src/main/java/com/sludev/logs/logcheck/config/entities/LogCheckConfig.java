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

import com.sludev.logs.logcheck.enums.LCCompressionType;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCIndexNameFormat;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.enums.LCLogEntryStoreType;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.ParseNumberWithSuffix;
import com.sun.org.apache.xpath.internal.operations.Bool;
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
import java.util.regex.Pattern;

/**
 * The main configuration class for the LogCheck application.
 *
 * @author kervin
 */
public final class LogCheckConfig
{
    private static final org.apache.logging.log4j.Logger LOGGER
                             = LogManager.getLogger(LogCheckConfig.class);

    private final Long m_pollIntervalSeconds;
    private final Long m_stopAfter;
    private final Integer m_idBlockSize;
    private final Integer m_deDupeMaxLogsPerFile;
    private final Integer m_deDupeMaxLogsBeforeWrite;
    private final Integer m_deDupeMaxLogFiles;
    private final Integer m_readLogFileCount;
    private final Integer m_readMaxDeDupeEntries;
    private final String m_emailOnError;
    private final String m_smtpServer;
    private final String m_smtpPort;
    private final String m_smtpPass;
    private final String m_smtpUser;
    private final String m_smtpProto;
    private final String m_setName;
    private final String m_elasticsearchIndexName;
    private final String m_elasticsearchIndexPrefix;
    private final String m_elasticsearchLogType;
    private final Boolean m_service;
    private final Boolean m_dryRun;
    private final Boolean m_showVersion;
    private final Boolean m_tailFromEnd;
    private final Boolean m_printLog;
    private final Boolean m_saveState;
    private final Boolean m_collectState;
    private final Boolean m_continueState;
    private final Boolean m_readReOpenLogFile;
    private final Boolean m_storeReOpenLogFile;
    private final Boolean m_startPositionIgnoreError;
    private final Boolean m_validateTailerStats;
    private final Boolean m_tailerBackupReadLog;
    private final Boolean m_stopOnEOF;
    private final Boolean m_readOnlyFileMode;
    private final Path m_lockFilePath;
    private final Path m_logPath;
    private final Path m_storeLogPath;
    private final Path m_statusFilePath;
    private final Path m_stateFilePath;
    private final Path m_errorFilePath;
    private final Path m_configFilePath;
    private final Path m_holdingDirPath;
    private final Path m_deDupeDirPath;
    private final Path m_tailerLogBackupDir;
    private final URL m_elasticsearchURL;
    private final LocalTime m_logCutoffDate;
    private final Duration m_logCutoffDuration;
    private final Duration m_logDeduplicationDuration;
    private final LCIndexNameFormat m_elasticsearchIndexNameFormat;
    private final List<LCLogEntryBuilderType> m_logEntryBuilders;
    private final List<LCLogEntryStoreType> m_logEntryStores;
    private final List<LCFileRegexComponent> m_tailerBackupLogNameComps;
    private final LCHashType m_idBlockHashType;
    private final LCCompressionType m_tailerBackupLogCompression;
    private final Pattern m_tailerBackupLogNameRegex;

    public Boolean isReadOnlyFileMode()
    {
        return m_readOnlyFileMode;
    }

    public Boolean willStopOnEOF()
    {
        return m_stopOnEOF;
    }

    public Path getErrorFilePath()
    {
        return m_errorFilePath;
    }

    public Path getTailerLogBackupDir()
    {
        return m_tailerLogBackupDir;
    }

    public Boolean willTailerBackupReadLog()
    {
        return m_tailerBackupReadLog;
    }

    public List<LCFileRegexComponent> getTailerBackupLogNameComps()
    {
        return m_tailerBackupLogNameComps;
    }

    public LCCompressionType getTailerBackupLogCompression()
    {
        return m_tailerBackupLogCompression;
    }

    public Pattern getTailerBackupLogNameRegex()
    {
        return m_tailerBackupLogNameRegex;
    }

    public Integer getIdBlockSize()
    {
        return m_idBlockSize;
    }

    public LCHashType getIdBlockHashType()
    {
        return m_idBlockHashType;
    }

    public Boolean willSaveState()
    {
        return m_saveState;
    }

    public Boolean willCollectState()
    {
        return m_collectState;
    }

    public Boolean willContinueState()
    {
        return m_continueState;
    }

    public Boolean willIgnoreStartPositionError()
    {
        return m_startPositionIgnoreError;
    }

    public Path getStateFilePath()
    {
        return m_stateFilePath;
    }

    public List<LCLogEntryBuilderType> getLogEntryBuilders()
    {
        return m_logEntryBuilders;
    }

    public List<LCLogEntryStoreType> getLogEntryStores()
    {
        return m_logEntryStores;
    }

    public String getElasticsearchLogType()
    {
        return m_elasticsearchLogType;
    }

    public Boolean willPrintLog()
    {
        return m_printLog;
    }

    public Boolean isDryRun()
    {
        return m_dryRun;
    }

    public Boolean isShowVersion()
    {
        return m_showVersion;
    }

    public Duration getLogDeduplicationDuration()
    {
        return m_logDeduplicationDuration;
    }
    
    public LocalTime getLogCutoffDate()
    {
        return m_logCutoffDate;
    }

    public Duration getLogCutoffDuration()
    {
        return m_logCutoffDuration;
    }

    public LCIndexNameFormat getElasticsearchIndexNameFormat()
    {
        return m_elasticsearchIndexNameFormat;
    }

    public String getSetName()
    {
        return m_setName;
    }

    public String getElasticsearchIndexName()
    {
        return m_elasticsearchIndexName;
    }

    public String getElasticsearchIndexPrefix()
    {
        return m_elasticsearchIndexPrefix;
    }

    public Boolean isTailFromEnd()
    {
        return m_tailFromEnd;
    }

    public Path getHoldingDirPath()
    {
        return m_holdingDirPath;
    }

    public Path getConfigFilePath()
    {
        return m_configFilePath;
    }

    public Path getDeDupeDirPath()
    {
        return m_deDupeDirPath;
    }

    public Boolean isService()
    {
        return m_service;
    }

    public Boolean willReadReOpenLogFile()
    {
        return m_readReOpenLogFile;
    }

    public Boolean willStoreReOpenLogFile()
    {
        return m_storeReOpenLogFile;
    }

    public Boolean willValidateTailerStats()
    {
        return m_validateTailerStats;
    }

    public Path getStoreLogPath()
    {
        return m_storeLogPath;
    }

    public Long getPollIntervalSeconds()
    {
        return m_pollIntervalSeconds;
    }

    public String getEmailOnError()
    {
        return m_emailOnError;
    }

    public String getSmtpServer()
    {
        return m_smtpServer;
    }

    public String getSmtpPort()
    {
        return m_smtpPort;
    }

    public String getSmtpPass()
    {
        return m_smtpPass;
    }

    public String getSmtpUser()
    {
        return m_smtpUser;
    }

    public String getSmtpProto()
    {
        return m_smtpProto;
    }

    public Path getLockFilePath()
    {
        return m_lockFilePath;
    }

    public Path getLogPath()
    {
        return m_logPath;
    }

    public Path getStatusFilePath()
    {
        return m_statusFilePath;
    }
    
    public URL getElasticsearchURL()
    {
        return m_elasticsearchURL;
    }

    public Integer getDeDupeMaxLogsPerFile()
    {
        return m_deDupeMaxLogsPerFile;
    }

    public Integer getDeDupeMaxLogsBeforeWrite()
    {
        return m_deDupeMaxLogsBeforeWrite;
    }

    public Integer getDeDupeMaxLogFiles()
    {
        return m_deDupeMaxLogFiles;
    }

    public Integer getReadLogFileCount()
    {
        return m_readLogFileCount;
    }

    public Integer getReadMaxDeDupeEntries()
    {
        return m_readMaxDeDupeEntries;
    }

    public Long getStopAfter()
    {
        return m_stopAfter;
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
                           final Boolean collectState,
                           final Boolean continueState,
                           final Boolean startPositionIgnoreError,
                           final Boolean validateTailerStats,
                           final Boolean tailerBackupReadLog,
                           final Boolean stopOnEOF,
                           final Boolean readOnlyFileMode,
                           final Path lockFilePath,
                           final Path logPath,
                           final Path storeLogPath,
                           final Path statusFilePath,
                           final Path stateFilePath,
                           final Path errorFilePath,
                           final Path configFilePath,
                           final Path holdingDirPath,
                           final Path deDupeDirPath,
                           final Path tailerLogBackupDir,
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
                           final List<LCFileRegexComponent> tailerBackupLogNameComps,
                           final LCHashType idBlockHashType,
                           final LCCompressionType tailerBackupLogCompression,
                           final Pattern tailerBackupLogNameRegex) throws LogCheckException
    {
        if( stopOnEOF != null )
        {
            this.m_stopOnEOF = stopOnEOF;
        }
        else if( orig != null && orig.willStopOnEOF() != null )
        {
            this.m_stopOnEOF = orig.willStopOnEOF();
        }
        else
        {
            this.m_stopOnEOF = null;
        }

        if( readOnlyFileMode != null )
        {
            this.m_readOnlyFileMode = readOnlyFileMode;
        }
        else if( orig != null && orig.isReadOnlyFileMode() != null )
        {
            this.m_readOnlyFileMode = orig.isReadOnlyFileMode();
        }
        else
        {
            this.m_readOnlyFileMode = null;
        }

        if( tailerBackupReadLog != null )
        {
            this.m_tailerBackupReadLog = tailerBackupReadLog;
        }
        else if( orig != null && orig.willTailerBackupReadLog() != null )
        {
            this.m_tailerBackupReadLog = orig.willTailerBackupReadLog();
        }
        else
        {
            this.m_tailerBackupReadLog = null;
        }

        if( tailerLogBackupDir != null )
        {
            this.m_tailerLogBackupDir = tailerLogBackupDir;
        }
        else if( orig != null && orig.getTailerLogBackupDir() != null )
        {
            this.m_tailerLogBackupDir = orig.getTailerLogBackupDir();
        }
        else
        {
            this.m_tailerLogBackupDir = null;
        }

        if( tailerBackupLogNameComps != null )
        {
            this.m_tailerBackupLogNameComps = tailerBackupLogNameComps;
        }
        else if( orig != null && orig.getLogEntryStores() != null )
        {
            this.m_tailerBackupLogNameComps = orig.getTailerBackupLogNameComps();
        }
        else
        {
            this.m_tailerBackupLogNameComps = new ArrayList<>();
        }

        if( tailerBackupLogCompression != null )
        {
            this.m_tailerBackupLogCompression = tailerBackupLogCompression;
        }
        else if( orig != null && orig.getTailerBackupLogCompression() != null )
        {
            this.m_tailerBackupLogCompression = orig.getTailerBackupLogCompression();
        }
        else
        {
            this.m_tailerBackupLogCompression = null;
        }

        if( tailerBackupLogNameRegex != null )
        {
            this.m_tailerBackupLogNameRegex = tailerBackupLogNameRegex;
        }
        else if( orig != null && orig.getTailerBackupLogNameRegex() != null )
        {
            this.m_tailerBackupLogNameRegex = orig.getTailerBackupLogNameRegex();
        }
        else
        {
            this.m_tailerBackupLogNameRegex = null;
        }

        if( logEntryStores != null )
        {
            this.m_logEntryStores = logEntryStores;
        }
        else if( orig != null && orig.getLogEntryStores() != null )
        {
            this.m_logEntryStores = orig.getLogEntryStores();
        }
        else
        {
            this.m_logEntryStores = null;
        }

        if( storeReOpenLogFile != null )
        {
            this.m_storeReOpenLogFile = storeReOpenLogFile;
        }
        else if( orig != null && orig.willStoreReOpenLogFile() != null )
        {
            this.m_storeReOpenLogFile = orig.willStoreReOpenLogFile();
        }
        else
        {
            this.m_storeReOpenLogFile = false;
        }

        if( storeLogPath != null )
        {
            this.m_storeLogPath = storeLogPath;
        }
        else if( orig != null && orig.getStoreLogPath() != null )
        {
            this.m_storeLogPath = orig.getStoreLogPath();
        }
        else
        {
            this.m_storeLogPath = null;
        }

        if( service != null )
        {
            this.m_service = service;
        }
        else if( orig != null && orig.isService() != null )
        {
            this.m_service = orig.isService();
        }
        else
        {
            this.m_service = false;
        }

        if( validateTailerStats != null )
        {
            this.m_validateTailerStats = validateTailerStats;
        }
        else if( orig != null && orig.willValidateTailerStats() != null )
        {
            this.m_validateTailerStats = orig.willValidateTailerStats();
        }
        else
        {
            this.m_validateTailerStats = false;
        }
        
        if( deDupeMaxLogsBeforeWrite != null )
        {
            this.m_deDupeMaxLogsBeforeWrite = deDupeMaxLogsBeforeWrite;
        }
        else if( orig != null && orig.getDeDupeMaxLogsBeforeWrite() != null )
        {
            this.m_deDupeMaxLogsBeforeWrite = orig.getDeDupeMaxLogsBeforeWrite();
        }
        else
        {
            this.m_deDupeMaxLogsBeforeWrite = LogCheckConstants.DEFAULT_DEDUPE_LOGS_BEFORE_WRITE;
        }

        if( deDupeMaxLogsPerFile != null )
        {
            this.m_deDupeMaxLogsPerFile = deDupeMaxLogsPerFile;
        }
        else if( orig != null && orig.getDeDupeMaxLogsPerFile() != null )
        {
            this.m_deDupeMaxLogsPerFile = orig.getDeDupeMaxLogsPerFile();
        }
        else
        {
            this.m_deDupeMaxLogsPerFile = LogCheckConstants.MAX_DEDUPE_LOGS_PER_FILE;
        }

        if( deDupeMaxLogFiles != null )
        {
            this.m_deDupeMaxLogFiles = deDupeMaxLogFiles;
        }
        else if( orig != null && orig.getDeDupeMaxLogFiles() != null )
        {
            this.m_deDupeMaxLogFiles = orig.getDeDupeMaxLogFiles();
        }
        else
        {
            this.m_deDupeMaxLogFiles = LogCheckConstants.DEFAULT_DEDUPE_LOG_FILES_ROTATE;
        }

        if( idBlockSize != null )
        {
            this.m_idBlockSize = idBlockSize;
        }
        else if( orig != null && orig.getIdBlockSize() != null )
        {
            this.m_idBlockSize = orig.getIdBlockSize();
        }
        else
        {
            this.m_idBlockSize = LogCheckConstants.DEFAULT_ID_BLOCK_SIZE;
        }

        if( idBlockHashType != null )
        {
            this.m_idBlockHashType = idBlockHashType;
        }
        else if( orig != null && orig.getIdBlockHashType() != null )
        {
            this.m_idBlockHashType = orig.getIdBlockHashType();
        }
        else
        {
            this.m_idBlockHashType = LCHashType.SHA2;
        }

        if( logEntryBuilders != null )
        {
            this.m_logEntryBuilders = logEntryBuilders;
        }
        else if( orig != null && orig.getLogEntryBuilders() != null )
        {
            this.m_logEntryBuilders = orig.getLogEntryBuilders();
        }
        else
        {
            this.m_logEntryBuilders = null;
        }

        if( emailOnError != null )
        {
            this.m_emailOnError = emailOnError;
        }
        else if( orig != null && orig.getEmailOnError() != null )
        {
            this.m_emailOnError = orig.getEmailOnError();
        }
        else
        {
            this.m_emailOnError = null;
        }

        if( startPositionIgnoreError != null )
        {
            this.m_startPositionIgnoreError = startPositionIgnoreError;
        }
        else if( orig != null && orig.willIgnoreStartPositionError() != null )
        {
            this.m_startPositionIgnoreError = orig.willIgnoreStartPositionError();
        }
        else
        {
            this.m_startPositionIgnoreError = null;
        }

        if( elasticsearchURL != null )
        {
            this.m_elasticsearchURL
                    = elasticsearchURL;
        }
        else if( orig != null && orig.getElasticsearchURL() != null )
        {
            this.m_elasticsearchURL
                    = orig.getElasticsearchURL();
        }
        else
        {
//            try
//            {
//                this.m_elasticsearchURL
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

            this.m_elasticsearchURL = null;
        }

        if( elasticsearchIndexPrefix != null )
        {
            this.m_elasticsearchIndexPrefix
                    = elasticsearchIndexPrefix;
        }
        else if( orig != null && orig.getElasticsearchIndexPrefix() != null )
        {
            this.m_elasticsearchIndexPrefix
                    = orig.getElasticsearchIndexPrefix();
        }
        else
        {
            this.m_elasticsearchIndexPrefix
                    = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_PREFIX;
        }

        if( elasticsearchIndexNameFormat  != null )
        {
            this.m_elasticsearchIndexNameFormat
                    = elasticsearchIndexNameFormat ;
        }
        else if( orig != null
                && orig.getElasticsearchIndexNameFormat() != null )
        {
            this.m_elasticsearchIndexNameFormat
                    = orig.getElasticsearchIndexNameFormat();
        }
        else
        {
            this.m_elasticsearchIndexNameFormat
                    = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT;
        }

        if( elasticsearchLogType  != null )
        {
            this.m_elasticsearchLogType
                    = elasticsearchLogType;
        }
        else if( orig != null
                && orig.getElasticsearchLogType() != null )
        {
            this.m_elasticsearchLogType
                    = orig.getElasticsearchLogType();
        }
        else
        {
            this.m_elasticsearchLogType
                    = LogCheckConstants.DEFAULT_LOG_TYPE;
        }

        if( elasticsearchIndexName != null )
        {
            this.m_elasticsearchIndexName = elasticsearchIndexName;
        }
        else if( orig != null && orig.isService() != null )
        {
            this.m_elasticsearchIndexName = orig.getElasticsearchIndexName();
        }
        else
        {
            this.m_elasticsearchIndexName = null;
        }

        if( smtpServer != null )
        {
            this.m_smtpServer = smtpServer;
        }
        else if( orig != null && orig.getSmtpServer() != null )
        {
            this.m_smtpServer = orig.getSmtpServer();
        }
        else
        {
            this.m_smtpServer = null;
        }

        if( smtpPort != null )
        {
            this.m_smtpPort = smtpPort;
        }
        else if( orig != null && orig.getSmtpPort() != null )
        {
            this.m_smtpPort = orig.getSmtpPort();
        }
        else
        {
            this.m_smtpPort = null;
        }

        if( smtpPass != null )
        {
            this.m_smtpPass = smtpPass;
        }
        else if( orig != null && orig.getSmtpPass() != null )
        {
            this.m_smtpPass = orig.getSmtpPass();
        }
        else
        {
            this.m_smtpPass = null;
        }

        if( smtpUser != null )
        {
            this.m_smtpUser = smtpUser;
        }
        else if( orig != null && orig.getSmtpUser() != null )
        {
            this.m_smtpUser = orig.getSmtpUser();
        }
        else
        {
            this.m_smtpUser = null;
        }

        if( smtpProto != null )
        {
            this.m_smtpProto = smtpProto;
        }
        else if( orig != null && orig.getSmtpProto() != null )
        {
            this.m_smtpProto = orig.getSmtpProto();
        }
        else
        {
            this.m_smtpProto = null;
        }

        if( StringUtils.isNoneBlank(setName) )
        {
            this.m_setName = setName;
        }
        else if( orig != null && StringUtils.isNoneBlank(orig.getSetName()) )
        {
            this.m_setName = orig.getSetName();
        }
        else
        {
            this.m_setName = LogCheckConstants.DEFAULT_SET_NAME;
        }

        if( dryRun != null )
        {
            this.m_dryRun = dryRun;
        }
        else if( orig != null && orig.isDryRun() != null )
        {
            this.m_dryRun = orig.isDryRun();
        }
        else
        {
            this.m_dryRun = null;
        }

        if( showVersion != null )
        {
            this.m_showVersion = showVersion;
        }
        else if( orig != null && orig.isShowVersion() != null )
        {
            this.m_showVersion = orig.isShowVersion();
        }
        else
        {
            this.m_showVersion = null;
        }

        if( lockFilePath != null )
        {
            this.m_lockFilePath = lockFilePath;
        }
        else if( orig != null && orig.getLockFilePath() != null )
        {
            this.m_lockFilePath = orig.getLockFilePath();
        }
        else
        {
            this.m_lockFilePath = null;
        }

        if( logPath != null )
        {
            this.m_logPath = logPath;
        }
        else if( orig != null && orig.getLogPath() != null )
        {
            this.m_logPath = orig.getLogPath();
        }
        else
        {
            this.m_logPath = null;
        }

        if( statusFilePath != null )
        {
            this.m_statusFilePath = statusFilePath;
        }
        else if( orig != null && orig.getStatusFilePath() != null )
        {
            this.m_statusFilePath = orig.getStatusFilePath();
        }
        else
        {
            this.m_statusFilePath = null;
        }

        if( stateFilePath != null )
        {
            this.m_stateFilePath = stateFilePath;
        }
        else if( orig != null && orig.getStateFilePath() != null )
        {
            this.m_stateFilePath = orig.getStateFilePath();
        }
        else
        {
            this.m_stateFilePath = null;
        }

        if( errorFilePath != null )
        {
            this.m_errorFilePath = errorFilePath;
        }
        else if( orig != null && orig.getErrorFilePath() != null )
        {
            this.m_errorFilePath = orig.getErrorFilePath();
        }
        else
        {
            this.m_errorFilePath = null;
        }

        if( configFilePath != null )
        {
            this.m_configFilePath = configFilePath;
        }
        else if( orig != null && orig.getConfigFilePath() != null )
        {
            this.m_configFilePath = orig.getConfigFilePath();
        }
        else
        {
            this.m_configFilePath = null;
        }

        if( holdingDirPath != null )
        {
            this.m_holdingDirPath = holdingDirPath;
        }
        else if( orig != null && orig.getHoldingDirPath() != null )
        {
            this.m_holdingDirPath = orig.getHoldingDirPath();
        }
        else
        {
            this.m_holdingDirPath = null;
        }

        if( deDupeDirPath != null )
        {
            this.m_deDupeDirPath = deDupeDirPath;
        }
        else if( orig != null && orig.getDeDupeDirPath() != null )
        {
            this.m_deDupeDirPath = orig.getDeDupeDirPath();
        }
        else
        {
            this.m_deDupeDirPath = null;
        }

        if( logCutoffDate != null )
        {
            this.m_logCutoffDate = logCutoffDate;
        }
        else if( orig != null && orig.getLogCutoffDate() != null )
        {
            this.m_logCutoffDate = orig.getLogCutoffDate();
        }
        else
        {
            this.m_logCutoffDate = null;
        }

        if( logCutoffDuration != null )
        {
            this.m_logCutoffDuration = logCutoffDuration;
        }
        else if( orig != null && orig.getLogCutoffDuration() != null )
        {
            this.m_logCutoffDuration = orig.getLogCutoffDuration();
        }
        else
        {
            this.m_logCutoffDuration = null;
        }

        if( logDeduplicationDuration != null )
        {
            this.m_logDeduplicationDuration
                    = logDeduplicationDuration;
        }
        else if( orig != null && orig.getLogDeduplicationDuration() != null )
        {
            this.m_logDeduplicationDuration
                    = orig.getLogDeduplicationDuration();
        }
        else
        {
            this.m_logDeduplicationDuration = null;
        }

        if( stopAfter != null )
        {
            this.m_stopAfter = stopAfter;
        }
        else if( orig != null && orig.getStopAfter() != null )
        {
            this.m_stopAfter = orig.getStopAfter();
        }
        else
        {
            this.m_stopAfter = null;
        }

        if( printLog != null )
        {
            this.m_printLog = printLog;
        }
        else if( orig != null && orig.willPrintLog() != null )
        {
            this.m_printLog = orig.willPrintLog();
        }
        else
        {
            this.m_printLog = null;
        }

        if( saveState != null )
        {
            this.m_saveState = saveState;
        }
        else if( orig != null && orig.willSaveState() != null )
        {
            this.m_saveState = orig.willSaveState();
        }
        else
        {
            this.m_saveState = null;
        }

        if( collectState != null )
        {
            this.m_collectState= collectState;
        }
        else if( orig != null && orig.willCollectState() != null )
        {
            this.m_collectState= orig.willCollectState();
        }
        else
        {
            this.m_collectState = null;
        }

        if( readReOpenLogFile != null )
        {
            this.m_readReOpenLogFile = readReOpenLogFile;
        }
        else if( orig != null && orig.willReadReOpenLogFile() != null )
        {
            this.m_readReOpenLogFile = orig.willReadReOpenLogFile();
        }
        else
        {
            this.m_readReOpenLogFile = null;
        }

        if( continueState != null )
        {
            this.m_continueState = continueState;
        }
        else if( orig != null && orig.willContinueState() != null )
        {
            this.m_continueState = orig.willContinueState();
        }
        else
        {
            this.m_continueState = null;
        }

        if( pollIntervalSeconds != null )
        {
            this.m_pollIntervalSeconds = pollIntervalSeconds;
        }
        else if( orig != null && orig.getPollIntervalSeconds() != null )
        {
            this.m_pollIntervalSeconds = orig.getPollIntervalSeconds();
        }
        else
        {
            this.m_pollIntervalSeconds = null;
        }

        if( readLogFileCount != null )
        {
            this.m_readLogFileCount = readLogFileCount;
        }
        else if( orig != null && orig.getReadLogFileCount() != null )
        {
            this.m_readLogFileCount = orig.getReadLogFileCount();
        }
        else
        {
            this.m_readLogFileCount = null;
        }

        if( readMaxDeDupeEntries != null )
        {
            this.m_readMaxDeDupeEntries = readMaxDeDupeEntries;
        }
        else if( orig != null && orig.getReadMaxDeDupeEntries() != null )
        {
            this.m_readMaxDeDupeEntries = orig.getReadMaxDeDupeEntries();
        }
        else
        {
            this.m_readMaxDeDupeEntries = null;
        }

        if( tailFromEnd != null )
        {
            this.m_tailFromEnd = tailFromEnd;
        }
        else if( orig != null && orig.isTailFromEnd() != null )
        {
            this.m_tailFromEnd = orig.isTailFromEnd();
        }
        else
        {
            this.m_tailFromEnd = null;
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
                                      final Boolean collectState,
                                      final Boolean continueState,
                                      final Boolean startPositionIgnoreError,
                                      final Boolean validateTailerStats,
                                      final Boolean tailerBackupReadLog,
                                      final Boolean stopOnEOF,
                                      final Boolean readOnlyFileMode,
                                      final Path lockFilePath,
                                      final Path logPath,
                                      final Path storeLogPath,
                                      final Path statusFilePath,
                                      final Path stateFilePath,
                                      final Path errorFilePath,
                                      final Path configFilePath,
                                      final Path holdingDirPath,
                                      final Path deDupeDirPath,
                                      final Path tailerLogBackupDir,
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
                                      final List<LCFileRegexComponent> tailerBackupLogNameComps,
                                      final LCHashType idBlockHashType,
                                      final LCCompressionType tailerBackupLogCompression,
                                      final Pattern tailerBackupLogNameRegex) throws LogCheckException
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
                collectState,
                continueState,
                startPositionIgnoreError,
                validateTailerStats,
                tailerBackupReadLog,
                stopOnEOF,
                readOnlyFileMode,
                lockFilePath,
                logPath,
                storeLogPath,
                statusFilePath,
                stateFilePath,
                errorFilePath,
                configFilePath,
                holdingDirPath,
                deDupeDirPath,
                tailerLogBackupDir,
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
                tailerBackupLogNameComps,
                idBlockHashType,
                tailerBackupLogCompression,
                tailerBackupLogNameRegex);

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
                                        final Boolean collectState,
                                       final Boolean continueState,
                                        final Boolean startPositionIgnoreError,
                                        final Boolean validateTailerStats,
                                        final Boolean tailerBackupReadLog,
                                        final Boolean stopOnEOF,
                                        final Boolean readOnlyFileMode,
                                       final String lockFilePathStr,
                                       final String logPathStr,
                                        final String storeLogPathStr,
                                       final String statusFilePathStr,
                                       final String stateFilePathStr,
                                       final String errorFilePathStr,
                                       final String configFilePathStr,
                                       final String holdingDirPathStr,
                                       final String deDupeDirPathStr,
                                        final String tailerLogBackupDirStr,
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
                                        final String[] tailerBackupLogNameCompStrs,
                                       final String idBlockHashTypeStr,
                                        final String tailBackupLogCompressionStr,
                                        final String tailBackupLogNameRegexStr) throws LogCheckException
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
        Path tailerLogBackupDir = null;
        URL elasticsearchURL = null;
        LCIndexNameFormat elasticsearchIndexNameFormat = null;
        LocalTime logCutoffDate = null;
        Duration logCutoffDuration = null;
        Duration logDeduplicationDuration = null;
        Long pollIntervalSeconds = null;
        List<LCLogEntryBuilderType> logEntryBuilders  = new ArrayList<>(10);
        List<LCLogEntryStoreType> logEntryStores = new ArrayList<>(10);
        List<LCFileRegexComponent> tailerBackupLogNameComps = new ArrayList<>(10);
        Integer readLogFileCount = null;
        Integer readMaxDeDupeEntries = null;
        Integer idBlockSize = null;
        Integer deDupeMaxLogsBeforeWrite = null;
        Integer deDupeMaxLogsPerFile = null;
        Integer deDupeMaxLogFiles = null;
        Long stopAfter = null;
        LCHashType idBlockHash = null;
        LCCompressionType tailerBackupLogCompression = null;
        Pattern tailerBackupLogNameRegex = null;

        if(StringUtils.isNoneBlank(tailBackupLogCompressionStr))
        {
            tailerBackupLogCompression = LCCompressionType.from(tailBackupLogCompressionStr);
        }

        if(StringUtils.isNoneBlank(tailBackupLogNameRegexStr))
        {
            tailerBackupLogNameRegex = Pattern.compile(tailBackupLogNameRegexStr);
        }

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

        if(StringUtils.isNoneBlank(tailerLogBackupDirStr))
        {
            tailerLogBackupDir = Paths.get(tailerLogBackupDirStr);
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

                LOGGER.debug(errMsg, ex);
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

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(tailerBackupLogNameCompStrs != null )
        {
            for( String nameComp : tailerBackupLogNameCompStrs )
            {
                if( StringUtils.isNoneBlank(nameComp) )
                {
                    tailerBackupLogNameComps.add(LCFileRegexComponent.from(nameComp));
                }
            }
        }

        if(logEntryBuilderStrs != null )
        {
            for( String builder : logEntryBuilderStrs )
            {
                if( StringUtils.isNoneBlank(builder) )
                {
                    logEntryBuilders.add(LCLogEntryBuilderType.from(builder));
                }
            }
        }

        if(logEntryStoreStrs != null )
        {
            for( String store : logEntryStoreStrs )
            {
                if( StringUtils.isNoneBlank(store) )
                {
                    logEntryStores.add(LCLogEntryStoreType.from(store));
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

                LOGGER.debug(errMsg, ex);
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

                LOGGER.debug(errMsg, ex);
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

                LOGGER.debug(errMsg, ex);
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

                LOGGER.debug(errMsg, ex);
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

                LOGGER.debug(errMsg, ex);
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

                LOGGER.debug(errMsg, ex);
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

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(idBlockHashTypeStr))
        {
            try
            {
                idBlockHash = LCHashType.from(idBlockHashTypeStr);
            }
            catch( LogCheckException ex )
            {
                String errMsg = String.format("Error parsing ID Block Hash '%s'", idBlockHashTypeStr);

                LOGGER.debug(errMsg, ex);
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
                collectState,
                continueState,
                startPositionIgnoreError,
                validateTailerStats,
                tailerBackupReadLog,
                stopOnEOF,
                readOnlyFileMode,
                lockFilePath,
                logPath,
                storeLogPath,
                statusFilePath,
                stateFilePath,
                errorFilePath,
                configFilePath,
                holdingDirPath,
                deDupeDirPath,
                tailerLogBackupDir,
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
                tailerBackupLogNameComps,
                idBlockHash,
                tailerBackupLogCompression,
                tailerBackupLogNameRegex);

        return res;
    }
}
