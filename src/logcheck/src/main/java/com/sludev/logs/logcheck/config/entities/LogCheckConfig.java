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

import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.enums.LCCompressionType;
import com.sludev.logs.logcheck.enums.LCDeDupeAction;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCIndexNameFormat;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.enums.LCLogEntryStoreType;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.ParseNumberWithSuffix;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final Long m_deDupeIgnoreCount;
    private final Long m_deDupeSkipCount;
    private final Integer m_idBlockSize;
    private final Integer m_deDupeMaxLogsPerFile;
    private final Integer m_deDupeMaxLogsBeforeWrite;
    private final Integer m_deDupeMaxLogFiles;
    private final Integer m_deDupeIgnorePercent;
    private final Integer m_deDupeSkipPercent;
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
    private final Boolean m_tailerBackupReadLogReverse;
    private final Boolean m_tailerBackupReadPriorLog;
    private final Boolean m_stopOnEOF;
    private final Boolean m_readOnlyFileMode;
    private final Boolean m_createMissingDirs;
    private final Path m_lockFilePath;
    private final Path m_logPath;
    private final Path m_storeLogPath;
    private final Path m_statusFilePath;
    private final Path m_stateFilePath;
    private final Path m_stateProcessedLogsFilePath;
    private final Path m_errorFilePath;
    private final Path m_configFilePath;
    private final Path m_holdingDirPath;
    private final Path m_deDupeDirPath;
    private final Path m_tailerLogBackupDir;
    private final Path m_preferredDir;
    private final Path m_stdOutFile;
    private final URL m_elasticsearchURL;
    private final LocalTime m_logCutoffDate;
    private final Duration m_logCutoffDuration;
    private final Duration m_logDeduplicationDuration;
    private final LCDeDupeAction m_deDupeDefaultAction;
    private final LCIndexNameFormat m_elasticsearchIndexNameFormat;
    private final List<LCLogEntryBuilderType> m_logEntryBuilders;
    private final List<LCLogEntryStoreType> m_logEntryStores;
    private final List<LCFileRegexComponent> m_tailerBackupLogNameComps;
    private final Set<LCDebugFlag> m_debugFlags;
    private final LCHashType m_idBlockHashType;
    private final LCCompressionType m_tailerBackupLogCompression;
    private final Pattern m_tailerBackupLogNameRegex;
    private final FSSVerbosityEnum m_verbosity;

    public Path getStdOutFile()
    {
        return m_stdOutFile;
    }

    public Path getPreferredDir()
    {
        return m_preferredDir;
    }

    public Path getStateProcessedLogsFilePath()
    {
        return m_stateProcessedLogsFilePath;
    }

    public Long getDeDupeIgnoreCount()
    {
        return m_deDupeIgnoreCount;
    }

    public Long getDeDupeSkipCount()
    {
        return m_deDupeSkipCount;
    }

    public Integer getDeDupeIgnorePercent()
    {
        return m_deDupeIgnorePercent;
    }

    public Integer getDeDupeSkipPercent()
    {
        return m_deDupeSkipPercent;
    }

    public LCDeDupeAction getDeDupeDefaultAction()
    {
        return m_deDupeDefaultAction;
    }

    public FSSVerbosityEnum getVerbosity()
    {
        return m_verbosity;
    }

    public Boolean isReadOnlyFileMode()
    {
        return m_readOnlyFileMode;
    }

    public Boolean willCreateMissingDirs()
    {
        return m_createMissingDirs;
    }

    public Set<LCDebugFlag> getDebugFlags()
    {
        return m_debugFlags;
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

    public Boolean willTailerBackupReadLogReverse()
    {
        return m_tailerBackupReadLogReverse;
    }

    public Boolean willTailerBackupReadPriorLog()
    {
        return m_tailerBackupReadPriorLog;
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
                           final Boolean tailerBackupReadLogReverse,
                           final Boolean tailerBackupReadPriorLog,
                           final Boolean stopOnEOF,
                           final Boolean readOnlyFileMode,
                           final Boolean createMissingDirs,
                           final Path lockFilePath,
                           final Path logPath,
                           final Path storeLogPath,
                           final Path statusFilePath,
                           final Path stateFilePath,
                           final Path stateProcessedLogsFilePath,
                           final Path errorFilePath,
                           final Path configFilePath,
                           final Path holdingDirPath,
                           final Path deDupeDirPath,
                           final Path tailerLogBackupDir,
                           final Path preferredDir,
                           final Path stdOutFile,
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
                           final Long deDupeIgnoreCount,
                           final Long deDupeSkipCount,
                           final Integer readLogFileCount,
                           final Integer readMaxDeDupeEntries,
                           final Integer idBlockSize,
                           final Integer deDupeMaxLogsBeforeWrite,
                           final Integer deDupeMaxLogsPerFile,
                           final Integer deDupeMaxLogFiles,
                           final Integer deDupeIgnorePercent,
                           final Integer deDupeSkipPercent,
                           final FSSVerbosityEnum verbosity,
                           final LCDeDupeAction deDupeDefaultAction,
                           final List<LCLogEntryBuilderType> logEntryBuilders,
                           final List<LCLogEntryStoreType> logEntryStores,
                           final List<LCFileRegexComponent> tailerBackupLogNameComps,
                           final LCHashType idBlockHashType,
                           final LCCompressionType tailerBackupLogCompression,
                           final Pattern tailerBackupLogNameRegex,
                           final Set<LCDebugFlag> debugFlags) throws LogCheckException
    {
        if( (deDupeIgnoreCount != null)
                && (deDupeIgnoreCount > -1) )
        {
            this.m_deDupeIgnoreCount = deDupeIgnoreCount;
        }
        else if( (orig != null)
                && (orig.getDeDupeIgnoreCount() != null)
                && (orig.getDeDupeIgnoreCount() > -1 ))
        {
            this.m_deDupeIgnoreCount = orig.getDeDupeIgnoreCount();
        }
        else
        {
            this.m_deDupeIgnoreCount = null;
        }

        if( (deDupeIgnorePercent != null)
                && (deDupeIgnorePercent > -1) )
        {
            this.m_deDupeIgnorePercent = deDupeIgnorePercent;
        }
        else if( (orig != null)
                && (orig.getDeDupeIgnorePercent() != null)
                && (orig.getDeDupeIgnorePercent() > -1))
        {
            this.m_deDupeIgnorePercent = orig.getDeDupeIgnorePercent();
        }
        else
        {
            this.m_deDupeIgnorePercent = null;
        }

        if( (deDupeSkipCount != null)
                && (deDupeIgnoreCount > -1) )
        {
            this.m_deDupeSkipCount = deDupeSkipCount;
        }
        else if( (orig != null)
                && (orig.getDeDupeSkipCount() != null)
                && (orig.getDeDupeSkipCount() > -1 ) )
        {
            this.m_deDupeSkipCount = orig.getDeDupeSkipCount();
        }
        else
        {
            this.m_deDupeSkipCount = null;
        }

        if( (deDupeSkipPercent != null)
                && (deDupeSkipPercent > -1) )
        {
            this.m_deDupeSkipPercent = deDupeSkipPercent;
        }
        else if( (orig != null)
                && (orig.getDeDupeSkipPercent() != null)
                && (orig.getDeDupeSkipPercent() > -1) )
        {
            this.m_deDupeSkipPercent = orig.getDeDupeSkipPercent();
        }
        else
        {
            this.m_deDupeSkipPercent = null;
        }

        if( deDupeDefaultAction != null )
        {
            this.m_deDupeDefaultAction = deDupeDefaultAction;
        }
        else if( (orig != null) && (orig.getDeDupeDefaultAction() != null) )
        {
            this.m_deDupeDefaultAction = orig.getDeDupeDefaultAction();
        }
        else
        {
            this.m_deDupeDefaultAction = null;
        }

        if( stopOnEOF != null )
        {
            this.m_stopOnEOF = stopOnEOF;
        }
        else if( (orig != null) && (orig.willStopOnEOF() != null) )
        {
            this.m_stopOnEOF = orig.willStopOnEOF();
        }
        else
        {
            this.m_stopOnEOF = null;
        }

        if( verbosity != null )
        {
            this.m_verbosity = verbosity;
        }
        else if( (orig != null) && (orig.getVerbosity() != null) )
        {
            this.m_verbosity = orig.getVerbosity();
        }
        else
        {
            this.m_verbosity = null;
        }

        if( preferredDir != null )
        {
            this.m_preferredDir = preferredDir;
        }
        else if( (orig != null) && (orig.getPreferredDir() != null) )
        {
            this.m_preferredDir = orig.getPreferredDir();
        }
        else
        {
            this.m_preferredDir = null;
        }

        if( stdOutFile != null )
        {
            this.m_stdOutFile = stdOutFile;
        }
        else if( (orig != null) && (orig.getStdOutFile() != null) )
        {
            this.m_stdOutFile = orig.getStdOutFile();
        }
        else
        {
            this.m_stdOutFile = null;
        }

        if( createMissingDirs != null )
        {
            this.m_createMissingDirs = createMissingDirs;
        }
        else if( (orig != null) && (orig.willCreateMissingDirs() != null) )
        {
            this.m_createMissingDirs = orig.willCreateMissingDirs();
        }
        else
        {
            this.m_createMissingDirs = null;
        }

        if( debugFlags != null )
        {
            this.m_debugFlags = debugFlags;
        }
        else if( (orig != null) && (orig.getDebugFlags() != null) )
        {
            this.m_debugFlags = orig.getDebugFlags();
        }
        else
        {
            this.m_debugFlags= null;
        }

        if( readOnlyFileMode != null )
        {
            this.m_readOnlyFileMode = readOnlyFileMode;
        }
        else if( (orig != null) && (orig.isReadOnlyFileMode() != null) )
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
        else if( (orig != null) && (orig.willTailerBackupReadLog() != null) )
        {
            this.m_tailerBackupReadLog = orig.willTailerBackupReadLog();
        }
        else
        {
            this.m_tailerBackupReadLog = null;
        }

        if( tailerBackupReadLogReverse != null )
        {
            this.m_tailerBackupReadLogReverse = tailerBackupReadLogReverse;
        }
        else if( (orig != null) && (orig.willTailerBackupReadLogReverse() != null) )
        {
            this.m_tailerBackupReadLogReverse = orig.willTailerBackupReadLogReverse();
        }
        else
        {
            this.m_tailerBackupReadLogReverse = null;
        }

        if( tailerBackupReadPriorLog != null )
        {
            this.m_tailerBackupReadPriorLog = tailerBackupReadPriorLog;
        }
        else if( (orig != null) && (orig.willTailerBackupReadPriorLog() != null) )
        {
            this.m_tailerBackupReadPriorLog = orig.willTailerBackupReadPriorLog();
        }
        else
        {
            this.m_tailerBackupReadPriorLog = null;
        }

        if( tailerLogBackupDir != null )
        {
            this.m_tailerLogBackupDir = tailerLogBackupDir;
        }
        else if( (orig != null) && (orig.getTailerLogBackupDir() != null) )
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
        else if( (orig != null) && (orig.getLogEntryStores() != null) )
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
        else if( (orig != null) && (orig.getTailerBackupLogCompression() != null) )
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
        else if( (orig != null) && (orig.getTailerBackupLogNameRegex() != null) )
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
        else if( (orig != null) && (orig.getLogEntryStores() != null) )
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
        else if( (orig != null) && (orig.willStoreReOpenLogFile() != null) )
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
        else if( (orig != null) && (orig.getStoreLogPath() != null) )
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
        else if( (orig != null) && (orig.isService() != null) )
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
        else if( (orig != null) && (orig.willValidateTailerStats() != null) )
        {
            this.m_validateTailerStats = orig.willValidateTailerStats();
        }
        else
        {
            this.m_validateTailerStats = false;
        }
        
        if( (deDupeMaxLogsBeforeWrite != null)
                && (deDupeMaxLogsBeforeWrite > -1) )
        {
            this.m_deDupeMaxLogsBeforeWrite = deDupeMaxLogsBeforeWrite;
        }
        else if( (orig != null)
                && (orig.getDeDupeMaxLogsBeforeWrite() != null)
                && (orig.getDeDupeMaxLogsBeforeWrite() > -1 ) )
        {
            this.m_deDupeMaxLogsBeforeWrite = orig.getDeDupeMaxLogsBeforeWrite();
        }
        else
        {
            this.m_deDupeMaxLogsBeforeWrite = null;
        }

        if( (deDupeMaxLogsPerFile != null)
                && (deDupeMaxLogsPerFile > -1) )
        {
            this.m_deDupeMaxLogsPerFile = deDupeMaxLogsPerFile;
        }
        else if( (orig != null)
                && (orig.getDeDupeMaxLogsPerFile() != null)
                && (orig.getDeDupeMaxLogsPerFile() > -1) )
        {
            this.m_deDupeMaxLogsPerFile = orig.getDeDupeMaxLogsPerFile();
        }
        else
        {
            this.m_deDupeMaxLogsPerFile = null;
        }

        if( (deDupeMaxLogFiles != null)
                && (deDupeMaxLogFiles > -1) )
        {
            this.m_deDupeMaxLogFiles = deDupeMaxLogFiles;
        }
        else if( (orig != null)
                && (orig.getDeDupeMaxLogFiles() != null)
                && (orig.getDeDupeMaxLogFiles() > -1) )
        {
            this.m_deDupeMaxLogFiles = orig.getDeDupeMaxLogFiles();
        }
        else
        {
            this.m_deDupeMaxLogFiles = null;
        }

        if( (idBlockSize != null) && (idBlockSize > -1) )
        {
            this.m_idBlockSize = idBlockSize;
        }
        else if( (orig != null)
                && (orig.getIdBlockSize() != null)
                && (orig.getIdBlockSize() > -1) )
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
        else if( (orig != null) && (orig.getIdBlockHashType() != null) )
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
        else if( (orig != null) && (orig.getLogEntryBuilders() != null) )
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
        else if( (orig != null) && (orig.getEmailOnError() != null) )
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
        else if( (orig != null) && (orig.willIgnoreStartPositionError() != null) )
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
        else if( (orig != null) && (orig.getElasticsearchURL() != null) )
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
        else if( (orig != null) && (orig.getElasticsearchIndexPrefix() != null) )
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
        else if( (orig != null)
                && (orig.getElasticsearchIndexNameFormat() != null) )
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
        else if( (orig != null)
                && (orig.getElasticsearchLogType() != null) )
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
        else if( (orig != null) && (orig.isService() != null) )
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
        else if( (orig != null) && (orig.getSmtpServer() != null) )
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
        else if( (orig != null) && (orig.getSmtpPort() != null) )
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
        else if( (orig != null) && (orig.getSmtpPass() != null) )
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
        else if( (orig != null) && (orig.getSmtpUser() != null) )
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
        else if( (orig != null) && (orig.getSmtpProto() != null) )
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
        else if( (orig != null) && StringUtils.isNoneBlank(orig.getSetName()) )
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
        else if( (orig != null) && (orig.isDryRun() != null) )
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
        else if( (orig != null) && (orig.isShowVersion() != null) )
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
        else if( (orig != null) && (orig.getLockFilePath() != null) )
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
        else if( (orig != null) && (orig.getLogPath() != null) )
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
        else if( (orig != null) && (orig.getStatusFilePath() != null) )
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
        else if( (orig != null) && (orig.getStateFilePath() != null) )
        {
            this.m_stateFilePath = orig.getStateFilePath();
        }
        else
        {
            this.m_stateFilePath = null;
        }

        if( stateProcessedLogsFilePath != null )
        {
            this.m_stateProcessedLogsFilePath = stateProcessedLogsFilePath;
        }
        else if( (orig != null) && (orig.getStateProcessedLogsFilePath() != null) )
        {
            this.m_stateProcessedLogsFilePath = orig.getStateProcessedLogsFilePath();
        }
        else
        {
            this.m_stateProcessedLogsFilePath = null;
        }

        if( errorFilePath != null )
        {
            this.m_errorFilePath = errorFilePath;
        }
        else if( (orig != null) && (orig.getErrorFilePath() != null) )
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
        else if( (orig != null) && (orig.getConfigFilePath() != null) )
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
        else if( (orig != null) && (orig.getHoldingDirPath() != null) )
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
        else if( (orig != null) && (orig.getDeDupeDirPath() != null) )
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
        else if( (orig != null) && (orig.getLogCutoffDate() != null) )
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
        else if( (orig != null) && (orig.getLogCutoffDuration() != null) )
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
        else if( (orig != null) && (orig.getLogDeduplicationDuration() != null) )
        {
            this.m_logDeduplicationDuration
                    = orig.getLogDeduplicationDuration();
        }
        else
        {
            this.m_logDeduplicationDuration = null;
        }

        if( (stopAfter != null) && (stopAfter > -1))
        {
            this.m_stopAfter = stopAfter;
        }
        else if( (orig != null)
                && (orig.getStopAfter() != null)
                && (orig.getStopAfter() > -1) )
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
        else if( (orig != null) && (orig.willPrintLog() != null) )
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
        else if( (orig != null) && (orig.willSaveState() != null) )
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
        else if( (orig != null) && (orig.willCollectState() != null) )
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
        else if( (orig != null) && (orig.willReadReOpenLogFile() != null) )
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
        else if( (orig != null) && (orig.willContinueState() != null) )
        {
            this.m_continueState = orig.willContinueState();
        }
        else
        {
            this.m_continueState = null;
        }

        if( (pollIntervalSeconds != null) && (pollIntervalSeconds > -1) )
        {
            this.m_pollIntervalSeconds = pollIntervalSeconds;
        }
        else if( (orig != null)
                && (orig.getPollIntervalSeconds() != null)
                && (orig.getPollIntervalSeconds() > -1) )
        {
            this.m_pollIntervalSeconds = orig.getPollIntervalSeconds();
        }
        else
        {
            this.m_pollIntervalSeconds = null;
        }

        if( (readLogFileCount != null) && (readLogFileCount > -1) )
        {
            this.m_readLogFileCount = readLogFileCount;
        }
        else if( (orig != null)
                && (orig.getReadLogFileCount() != null)
                && (orig.getReadLogFileCount() > -1) )
        {
            this.m_readLogFileCount = orig.getReadLogFileCount();
        }
        else
        {
            this.m_readLogFileCount = null;
        }

        if( (readMaxDeDupeEntries != null) && (readMaxDeDupeEntries > -1) )
        {
            this.m_readMaxDeDupeEntries = readMaxDeDupeEntries;
        }
        else if( (orig != null)
                && (orig.getReadMaxDeDupeEntries() != null)
                && (orig.getReadMaxDeDupeEntries() > -1) )
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
        else if( (orig != null) && (orig.isTailFromEnd() != null) )
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
                                      final Boolean tailerBackupReadLogReverse,
                                      final Boolean tailerBackupReadPriorLog,
                                      final Boolean stopOnEOF,
                                      final Boolean readOnlyFileMode,
                                      final Boolean createMissingDirs,
                                      final Path lockFilePath,
                                      final Path logPath,
                                      final Path storeLogPath,
                                      final Path statusFilePath,
                                      final Path stateFilePath,
                                      final Path stateProcessedLogsFilePath,
                                      final Path errorFilePath,
                                      final Path configFilePath,
                                      final Path holdingDirPath,
                                      final Path deDupeDirPath,
                                      final Path tailerLogBackupDir,
                                      final Path preferredDir,
                                      final Path stdOutFile,
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
                                      final Long deDupeIgnoreCount,
                                      final Long deDupeSkipCount,
                                      final Integer readLogFileCount,
                                      final Integer readMaxDeDupeEntries,
                                      final Integer idBlockSize,
                                      final Integer deDupeMaxLogsBeforeWrite,
                                      final Integer deDupeMaxLogsPerFile,
                                      final Integer deDupeMaxLogFiles,
                                      final Integer deDupeIgnorePercent,
                                      final Integer deDupeSkipPercent,
                                      final FSSVerbosityEnum verbosity,
                                      final LCDeDupeAction deDupeDefaultAction,
                                      final List<LCLogEntryBuilderType> logEntryBuilders,
                                      final List<LCLogEntryStoreType> logEntryStores,
                                      final List<LCFileRegexComponent> tailerBackupLogNameComps,
                                      final LCHashType idBlockHashType,
                                      final LCCompressionType tailerBackupLogCompression,
                                      final Pattern tailerBackupLogNameRegex,
                                      final Set<LCDebugFlag> debugFlags) throws LogCheckException
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
                tailerBackupReadLogReverse,
                tailerBackupReadPriorLog,
                stopOnEOF,
                readOnlyFileMode,
                createMissingDirs,
                lockFilePath,
                logPath,
                storeLogPath,
                statusFilePath,
                stateFilePath,
                stateProcessedLogsFilePath,
                errorFilePath,
                configFilePath,
                holdingDirPath,
                deDupeDirPath,
                tailerLogBackupDir,
                preferredDir,
                stdOutFile,
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
                deDupeIgnoreCount,
                deDupeSkipCount,
                readLogFileCount,
                readMaxDeDupeEntries,
                idBlockSize,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile,
                deDupeMaxLogFiles,
                deDupeIgnorePercent,
                deDupeSkipPercent,
                verbosity,
                deDupeDefaultAction,
                logEntryBuilders,
                logEntryStores,
                tailerBackupLogNameComps,
                idBlockHashType,
                tailerBackupLogCompression,
                tailerBackupLogNameRegex,
                debugFlags);

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
                                        final Boolean tailerBackupReadLogReverse,
                                        final Boolean tailerBackupReadPriorLog,
                                        final Boolean stopOnEOF,
                                        final Boolean readOnlyFileMode,
                                        final Boolean createMissingDirs,
                                       final String lockFilePathStr,
                                       final String logPathStr,
                                        final String storeLogPathStr,
                                       final String statusFilePathStr,
                                       final String stateFilePathStr,
                                       final String stateProcessedLogsFilePathStr,
                                       final String errorFilePathStr,
                                       final String configFilePathStr,
                                       final String holdingDirPathStr,
                                       final String deDupeDirPathStr,
                                        final String tailerLogBackupDirStr,
                                        final String preferredDirStr,
                                        final String stdOutFileStr,
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
                                        final String deDupeIgnoreCountStr,
                                        final String deDupeSkipCountStr,
                                        final String readLogFileCountStr,
                                        final String readMaxDeDupeEntriesStr,
                                       final String idBlockSizeStr,
                                        final String deDupeMaxLogsBeforeWriteStr,
                                        final String deDupeMaxLogsPerFileStr,
                                        final String deDupeMaxLogFilesStr,
                                        final String deDupeIgnorePercentStr,
                                        final String deDupeSkipPercentStr,
                                        final String verbosityStr,
                                        final String deDupeDefaultActionStr,
                                       final String[] logEntryBuilderStrs,
                                        final String[] logEntryStoreStrs,
                                        final String[] tailerBackupLogNameCompStrs,
                                       final String idBlockHashTypeStr,
                                        final String tailBackupLogCompressionStr,
                                        final String tailBackupLogNameRegexStr,
                                        final String[] debugFlagStrs) throws LogCheckException
    {
        Path lockFilePath = null;
        Path logPath = null;
        Path storeLogPath = null;
        Path statusFilePath = null;
        Path stateFilePath = null;
        Path stateProcessedLogsFilePath = null;
        Path errorFilePath = null;
        Path configFilePath = null;
        Path holdingDirPath = null;
        Path deDupeDirPath = null;
        Path tailerLogBackupDir = null;
        Path preferredDir = null;
        Path stdOutFile = null;
        URL elasticsearchURL = null;
        LCIndexNameFormat elasticsearchIndexNameFormat = null;
        LocalTime logCutoffDate = null;
        Duration logCutoffDuration = null;
        Duration logDeduplicationDuration = null;
        Long pollIntervalSeconds = null;
        List<LCLogEntryBuilderType> logEntryBuilders  = null;
        List<LCLogEntryStoreType> logEntryStores = null;
        List<LCFileRegexComponent> tailerBackupLogNameComps = null;
        Integer readLogFileCount = null;
        Integer readMaxDeDupeEntries = null;
        Integer idBlockSize = null;
        Integer deDupeMaxLogsBeforeWrite = null;
        Integer deDupeMaxLogsPerFile = null;
        Integer deDupeMaxLogFiles = null;
        Integer deDupeIgnorePercent = null;
        Integer deDupeSkipPercent = null;
        Long stopAfter = null;
        Long deDupeIgnoreCount = null;
        Long deDupeSkipCount = null;
        LCHashType idBlockHash = null;
        LCCompressionType tailerBackupLogCompression = null;
        FSSVerbosityEnum verbosity = null;
        LCDeDupeAction deDupeDefaultAction = null;
        Pattern tailerBackupLogNameRegex = null;
        Set<LCDebugFlag> debugFlags = null;

        if(StringUtils.isNoneBlank(tailBackupLogCompressionStr))
        {
            tailerBackupLogCompression = LCCompressionType.from(tailBackupLogCompressionStr);
        }

        if(StringUtils.isNoneBlank(tailBackupLogNameRegexStr))
        {
            tailerBackupLogNameRegex = Pattern.compile(tailBackupLogNameRegexStr);
        }

        if(StringUtils.isNoneBlank(preferredDirStr))
        {
            preferredDir = Paths.get(preferredDirStr);
        }

        if(StringUtils.isNoneBlank(stdOutFileStr))
        {
            stdOutFile = Paths.get(stdOutFileStr);
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

        if(StringUtils.isNoneBlank(stateProcessedLogsFilePathStr))
        {
            stateProcessedLogsFilePath = Paths.get(stateProcessedLogsFilePathStr);
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

        if(StringUtils.isNoneBlank(deDupeIgnoreCountStr))
        {
            try
            {
                deDupeIgnoreCount = Long.parseLong(deDupeIgnoreCountStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing long '%s'", deDupeIgnoreCount);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(deDupeSkipCountStr))
        {
            try
            {
                deDupeSkipCount = Long.parseLong(deDupeSkipCountStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing long '%s'", deDupeSkipCount);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(tailerBackupLogNameCompStrs != null )
        {
            tailerBackupLogNameComps = new ArrayList<>(10);

            for( String nameComp : tailerBackupLogNameCompStrs )
            {
                if( StringUtils.isNoneBlank(nameComp) )
                {
                    String tempStr = nameComp.replace('-', '_')
                                                .toUpperCase()
                                                .trim();

                    tailerBackupLogNameComps.add(LCFileRegexComponent.from(tempStr));
                }
            }
        }

        if(debugFlagStrs != null )
        {
            debugFlags = new HashSet<>();

            for( String debugFlagStr : debugFlagStrs )
            {
                if( StringUtils.isNoneBlank(debugFlagStr) )
                {
                    debugFlags.add(LCDebugFlag.from(debugFlagStr.replace('-', '_')
                            .toUpperCase()
                            .trim()));
                }
            }
        }

        if(logEntryBuilderStrs != null )
        {
            logEntryBuilders = new ArrayList<>(10);

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
            logEntryStores = new ArrayList<>(10);

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

        if(StringUtils.isNoneBlank(deDupeIgnorePercentStr))
        {
            try
            {
                deDupeIgnorePercent = Integer.parseInt(deDupeIgnorePercentStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeIgnorePercent);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(deDupeSkipPercentStr))
        {
            try
            {
                deDupeSkipPercent = Integer.parseInt(deDupeSkipPercentStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeSkipPercent);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(stopAfterStr))
        {
            try
            {
                Pair<Long,TimeUnit> stop = ParseNumberWithSuffix.parseIntWithTimeUnits(stopAfterStr);
                if( stop != null )
                {
                    stopAfter = stop.getRight().toSeconds(stop.getLeft());
                }
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

        if(StringUtils.isNoneBlank(deDupeDefaultActionStr))
        {
            try
            {
                deDupeDefaultAction = LCDeDupeAction.from(deDupeDefaultActionStr);
            }
            catch( LogCheckException ex )
            {
                String errMsg = String.format("Error parsing Deduplication Default Action '%s'", deDupeDefaultActionStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

        if(StringUtils.isNoneBlank(verbosityStr))
        {
            try
            {
                verbosity = FSSVerbosityEnum.from(verbosityStr);
            }
            catch( Exception ex )
            {
                String errMsg = String.format("Error parsing verbosity '%s'", verbosityStr);

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
                tailerBackupReadLogReverse,
                tailerBackupReadPriorLog,
                stopOnEOF,
                readOnlyFileMode,
                createMissingDirs,
                lockFilePath,
                logPath,
                storeLogPath,
                statusFilePath,
                stateFilePath,
                stateProcessedLogsFilePath,
                errorFilePath,
                configFilePath,
                holdingDirPath,
                deDupeDirPath,
                tailerLogBackupDir,
                preferredDir,
                stdOutFile,
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
                deDupeIgnoreCount,
                deDupeSkipCount,
                readLogFileCount,
                readMaxDeDupeEntries,
                idBlockSize,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile,
                deDupeMaxLogFiles,
                deDupeIgnorePercent,
                deDupeSkipPercent,
                verbosity,
                deDupeDefaultAction,
                logEntryBuilders,
                logEntryStores,
                tailerBackupLogNameComps,
                idBlockHash,
                tailerBackupLogCompression,
                tailerBackupLogNameRegex,
                debugFlags);

        return res;
    }

    public Path fixPathWithPreferred(Path path)
    {
        Path res = path;
        if( ( res != null)
                && (res.isAbsolute() == false)
                && ( getPreferredDir() != null ))
        {
            res = getPreferredDir().resolve(res).normalize();
        }

        return res;
    }

    @Override
    public int hashCode()
    {
        int res;

        HashCodeBuilder hcb = new HashCodeBuilder();

        // From field definition section, using VIM :
        // :1,$s/private final [^ ]\+/hcb.append(/gc
        // :1,$s/;/ );/gc

        hcb.append( m_pollIntervalSeconds );
        hcb.append( m_stopAfter );
        hcb.append( m_idBlockSize );
        hcb.append( m_deDupeMaxLogsPerFile );
        hcb.append( m_deDupeMaxLogsBeforeWrite );
        hcb.append( m_deDupeMaxLogFiles );
        hcb.append( m_readLogFileCount );
        hcb.append( m_readMaxDeDupeEntries );
        hcb.append( m_emailOnError );
        hcb.append( m_smtpServer );
        hcb.append( m_smtpPort );
        hcb.append( m_smtpPass );
        hcb.append( m_smtpUser );
        hcb.append( m_smtpProto );
        hcb.append( m_setName );
        hcb.append( m_elasticsearchIndexName );
        hcb.append( m_elasticsearchIndexPrefix );
        hcb.append( m_elasticsearchLogType );
        hcb.append( m_service );
        hcb.append( m_dryRun );
        hcb.append( m_showVersion );
        hcb.append( m_tailFromEnd );
        hcb.append( m_printLog );
        hcb.append( m_saveState );
        hcb.append( m_collectState );
        hcb.append( m_continueState );
        hcb.append( m_readReOpenLogFile );
        hcb.append( m_storeReOpenLogFile );
        hcb.append( m_startPositionIgnoreError );
        hcb.append( m_validateTailerStats );
        hcb.append( m_tailerBackupReadLog );
        hcb.append( m_tailerBackupReadPriorLog );
        hcb.append( m_stopOnEOF );
        hcb.append( m_readOnlyFileMode );
        hcb.append( m_lockFilePath );
        hcb.append( m_logPath );
        hcb.append( m_storeLogPath );
        hcb.append( m_statusFilePath );
        hcb.append( m_stateFilePath );
        hcb.append( m_errorFilePath );
        hcb.append( m_configFilePath );
        hcb.append( m_holdingDirPath );
        hcb.append( m_deDupeDirPath );
        hcb.append( m_tailerLogBackupDir );
        hcb.append( m_elasticsearchURL );
        hcb.append( m_logCutoffDate );
        hcb.append( m_logCutoffDuration );
        hcb.append( m_logDeduplicationDuration );
        hcb.append( m_elasticsearchIndexNameFormat );
        hcb.append( m_idBlockHashType );
        hcb.append( m_tailerBackupLogCompression );
        hcb.append( m_tailerBackupLogNameRegex );
        hcb.append( m_logEntryBuilders );
        hcb.append( m_logEntryStores );
        hcb.append( m_tailerBackupLogNameComps );
        hcb.append( m_debugFlags );

        res = hcb.hashCode();

        return res;
    }

    /**
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj)
    {
        boolean res;

        if ( (obj == null) || ((obj instanceof LogCheckConfig) == false) )
        {
            return false;
        }

        LogCheckConfig lcObj  = (LogCheckConfig)obj;

        EqualsBuilder eb = new EqualsBuilder();

        // From hashCode() above...
        // :1,$s/hcb.append/eb.append/gc
        // :1,$s/\(m_[^ ]\+\)/\1, lcObj.\1/gc
        eb.append( m_pollIntervalSeconds, lcObj.m_pollIntervalSeconds );
        eb.append( m_stopAfter, lcObj.m_stopAfter );
        eb.append( m_idBlockSize, lcObj.m_idBlockSize );
        eb.append( m_deDupeMaxLogsPerFile, lcObj.m_deDupeMaxLogsPerFile );
        eb.append( m_deDupeMaxLogsBeforeWrite, lcObj.m_deDupeMaxLogsBeforeWrite );
        eb.append( m_deDupeMaxLogFiles, lcObj.m_deDupeMaxLogFiles );
        eb.append( m_readLogFileCount, lcObj.m_readLogFileCount );
        eb.append( m_readMaxDeDupeEntries, lcObj.m_readMaxDeDupeEntries );
        eb.append( m_emailOnError, lcObj.m_emailOnError );
        eb.append( m_smtpServer, lcObj.m_smtpServer );
        eb.append( m_smtpPort, lcObj.m_smtpPort );
        eb.append( m_smtpPass, lcObj.m_smtpPass );
        eb.append( m_smtpUser, lcObj.m_smtpUser );
        eb.append( m_smtpProto, lcObj.m_smtpProto );
        eb.append( m_setName, lcObj.m_setName );
        eb.append( m_elasticsearchIndexName, lcObj.m_elasticsearchIndexName );
        eb.append( m_elasticsearchIndexPrefix, lcObj.m_elasticsearchIndexPrefix );
        eb.append( m_elasticsearchLogType, lcObj.m_elasticsearchLogType );
        eb.append( m_service, lcObj.m_service );
        eb.append( m_dryRun, lcObj.m_dryRun );
        eb.append( m_showVersion, lcObj.m_showVersion );
        eb.append( m_tailFromEnd, lcObj.m_tailFromEnd );
        eb.append( m_printLog, lcObj.m_printLog );
        eb.append( m_saveState, lcObj.m_saveState );
        eb.append( m_collectState, lcObj.m_collectState );
        eb.append( m_continueState, lcObj.m_continueState );
        eb.append( m_readReOpenLogFile, lcObj.m_readReOpenLogFile );
        eb.append( m_storeReOpenLogFile, lcObj.m_storeReOpenLogFile );
        eb.append( m_startPositionIgnoreError, lcObj.m_startPositionIgnoreError );
        eb.append( m_validateTailerStats, lcObj.m_validateTailerStats );
        eb.append( m_tailerBackupReadLog, lcObj.m_tailerBackupReadLog );
        eb.append( m_tailerBackupReadPriorLog, lcObj.m_tailerBackupReadPriorLog );
        eb.append( m_stopOnEOF, lcObj.m_stopOnEOF );
        eb.append( m_readOnlyFileMode, lcObj.m_readOnlyFileMode );
        eb.append( m_lockFilePath, lcObj.m_lockFilePath );
        eb.append( m_logPath, lcObj.m_logPath );
        eb.append( m_storeLogPath, lcObj.m_storeLogPath );
        eb.append( m_statusFilePath, lcObj.m_statusFilePath );
        eb.append( m_stateFilePath, lcObj.m_stateFilePath );
        eb.append( m_errorFilePath, lcObj.m_errorFilePath );
        eb.append( m_configFilePath, lcObj.m_configFilePath );
        eb.append( m_holdingDirPath, lcObj.m_holdingDirPath );
        eb.append( m_deDupeDirPath, lcObj.m_deDupeDirPath );
        eb.append( m_tailerLogBackupDir, lcObj.m_tailerLogBackupDir );
        eb.append( m_elasticsearchURL, lcObj.m_elasticsearchURL );
        eb.append( m_logCutoffDate, lcObj.m_logCutoffDate );
        eb.append( m_logCutoffDuration, lcObj.m_logCutoffDuration );
        eb.append( m_logDeduplicationDuration, lcObj.m_logDeduplicationDuration );
        eb.append( m_elasticsearchIndexNameFormat, lcObj.m_elasticsearchIndexNameFormat );
        eb.append( m_logEntryBuilders, lcObj.m_logEntryBuilders );
        eb.append( m_logEntryStores, lcObj.m_logEntryStores );
        eb.append( m_tailerBackupLogNameComps, lcObj.m_tailerBackupLogNameComps );
        eb.append( m_debugFlags, lcObj.m_debugFlags );
        eb.append( m_idBlockHashType, lcObj.m_idBlockHashType );
        eb.append( m_tailerBackupLogCompression, lcObj.m_tailerBackupLogCompression );

        if( (m_tailerBackupLogNameRegex != null) && (lcObj.m_tailerBackupLogNameRegex != null) )
        {
            eb.append( m_tailerBackupLogNameRegex.pattern(), lcObj.m_tailerBackupLogNameRegex.pattern() );
        }
        else if( (m_tailerBackupLogNameRegex != null) || (lcObj.m_tailerBackupLogNameRegex != null) )
        {
            return false;
        }

        res = eb.build();

        return res;
    }

    @Override
    public String toString()
    {
        String res = ToStringBuilder.reflectionToString(this,
                RecursiveToStringStyle.MULTI_LINE_STYLE);

        return res;
    }
}
