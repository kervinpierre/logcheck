package com.sludev.logs.logcheck.config.builders;

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.enums.LCCompressionType;
import com.sludev.logs.logcheck.enums.LCDeDupeAction;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCIndexNameFormat;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.enums.LCLogEntryStoreType;
import com.sludev.logs.logcheck.enums.LCLogSourceType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Value-Object corresponding to the LogCheckConfig class.
 * 
 * Created by Kervin on 8/9/2016.
 */
public final class LogCheckConfigBuilder
{
    private static final org.apache.logging.log4j.Logger LOGGER
            = LogManager.getLogger(LogCheckConfigBuilder.class);

    private Integer m_id;

    private Long m_pollIntervalSeconds;
    private Long m_stopAfter;
    private Long m_deDupeIgnoreCount;
    private Long m_deDupeSkipCount;
    private Integer m_idBlockSize;
    private Integer m_deDupeMaxLogsPerFile;
    private Integer m_deDupeMaxLogsBeforeWrite;
    private Integer m_deDupeMaxLogFiles;
    private Integer m_deDupeIgnorePercent;
    private Integer m_deDupeSkipPercent;
    private Integer m_readLogFileCount;
    private Integer m_readMaxDeDupeEntries;
    private String m_emailOnError;
    private String m_smtpServer;
    private String m_smtpPort;
    private String m_smtpPass;
    private String m_smtpUser;
    private String m_smtpProto;
    private String m_setName;
    private String m_elasticsearchIndexName;
    private String m_elasticsearchIndexPrefix;
    private String m_elasticsearchLogType;
    private String m_windowsEventConnection;
    private Boolean m_service;
    private Boolean m_dryRun;
    private Boolean m_showVersion;
    private Boolean m_tailFromEnd;
    private Boolean m_printLog;
    private Boolean m_saveState;
    private Boolean m_collectState;
    private Boolean m_continueState;
    private Boolean m_readReOpenLogFile;
    private Boolean m_storeReOpenLogFile;
    private Boolean m_startPositionIgnoreError;
    private Boolean m_validateTailerStats;
    private Boolean m_tailerBackupReadLog;
    private Boolean m_tailerBackupReadLogReverse;
    private Boolean m_tailerBackupReadPriorLog;
    private Boolean m_stopOnEOF;
    private Boolean m_readOnlyFileMode;
    private Boolean m_createMissingDirs;
    private Path m_lockFilePath;
    private Path m_logPath;
    private Path m_storeLogPath;
    private Path m_statusFilePath;
    private Path m_stateFilePath;
    private Path m_stateProcessedLogsFilePath;
    private Path m_errorFilePath;
    private Path m_configFilePath;
    private Path m_holdingDirPath;
    private Path m_deDupeDirPath;
    private Path m_tailerLogBackupDir;
    private Path m_preferredDir;
    private Path m_stdOutFile;
    private URL m_elasticsearchURL;
    private URL m_monitorURL;
    private LocalTime m_logCutoffDate;
    private Duration m_logCutoffDuration;
    private Duration m_logDeduplicationDuration;
    private LCDeDupeAction m_deDupeDefaultAction;
    private LCIndexNameFormat m_elasticsearchIndexNameFormat;
    private List<LCLogEntryBuilderType> m_logEntryBuilders;
    private List<LCLogEntryStoreType> m_logEntryStores;
    private List<LCFileRegexComponent> m_tailerBackupLogNameComps;
    private Set<LCDebugFlag> m_debugFlags;
    private LCHashType m_idBlockHashType;
    private LCCompressionType m_tailerBackupLogCompression;
    private Pattern m_tailerBackupLogNameRegex;
    private LCLogSourceType m_logSourceType;
    private FSSVerbosityEnum m_verbosity;

    public Integer getId()
    {
        return m_id;
    }

    public void setId( Integer m_id )
    {
        this.m_id = m_id;
    }

    public void setId( String idStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(idStr))
        {
            try
            {
                m_id = Integer.parseInt(idStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", idStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Long getPollIntervalSeconds()
    {
        return m_pollIntervalSeconds;
    }

    public void setPollIntervalSeconds( Long m_pollIntervalSeconds )
    {
        this.m_pollIntervalSeconds = m_pollIntervalSeconds;
    }

    public void setPollIntervalSeconds( String pollIntervalSecondsStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(pollIntervalSecondsStr))
        {
            try
            {
                m_pollIntervalSeconds = Long.parseLong(pollIntervalSecondsStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing long '%s'", pollIntervalSecondsStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Long getStopAfter()
    {
        return m_stopAfter;
    }

    public void setStopAfter( Long m_stopAfter )
    {
        this.m_stopAfter = m_stopAfter;
    }

    public void setStopAfter( String stopAfterStr )
            throws LogCheckException
    {

        if(StringUtils.isNoneBlank(stopAfterStr))
        {
            try
            {
                Pair<Long,TimeUnit> stop = ParseNumberWithSuffix.parseIntWithTimeUnits(stopAfterStr);
                if( stop != null )
                {
                    m_stopAfter = stop.getRight().toSeconds(stop.getLeft());
                }
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", stopAfterStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

    }

    public Long getDeDupeIgnoreCount()
    {
        return m_deDupeIgnoreCount;
    }

    public void setDeDupeIgnoreCount( Long m_deDupeIgnoreCount )
    {
        this.m_deDupeIgnoreCount = m_deDupeIgnoreCount;
    }

    public void setDeDupeIgnoreCount( String deDupeIgnoreCountStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(deDupeIgnoreCountStr))
        {
            try
            {
                m_deDupeIgnoreCount = Long.parseLong(deDupeIgnoreCountStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing long '%s'", m_deDupeIgnoreCount);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Long getDeDupeSkipCount()
    {
        return m_deDupeSkipCount;
    }

    public void setDeDupeSkipCount( Long m_deDupeSkipCount )
    {
        this.m_deDupeSkipCount = m_deDupeSkipCount;
    }

    public void setDeDupeSkipCount( String deDupeSkipCountStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(deDupeSkipCountStr))
        {
            try
            {
                m_deDupeSkipCount = Long.parseLong(deDupeSkipCountStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing long '%s'", m_deDupeSkipCount);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Integer getIdBlockSize()
    {
        return m_idBlockSize;
    }

    public void setIdBlockSize( Integer m_idBlockSize )
    {
        this.m_idBlockSize = m_idBlockSize;
    }

    public void setIdBlockSize( String idBlockSizeStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(idBlockSizeStr))
        {
            try
            {
                m_idBlockSize = Integer.parseInt(idBlockSizeStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", idBlockSizeStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

    }

    public Integer getDeDupeMaxLogsPerFile()
    {
        return m_deDupeMaxLogsPerFile;
    }

    public void setDeDupeMaxLogsPerFile( Integer m_deDupeMaxLogsPerFile )
    {
        this.m_deDupeMaxLogsPerFile = m_deDupeMaxLogsPerFile;
    }

    public void setDeDupeMaxLogsPerFile( String deDupeMaxLogsPerFileStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(deDupeMaxLogsPerFileStr))
        {
            try
            {
                m_deDupeMaxLogsPerFile = Integer.parseInt(deDupeMaxLogsPerFileStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeMaxLogsPerFileStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Integer getDeDupeMaxLogsBeforeWrite()
    {
        return m_deDupeMaxLogsBeforeWrite;
    }

    public void setDeDupeMaxLogsBeforeWrite( Integer m_deDupeMaxLogsBeforeWrite )
    {
        this.m_deDupeMaxLogsBeforeWrite = m_deDupeMaxLogsBeforeWrite;
    }

    public void setDeDupeMaxLogsBeforeWrite( String deDupeMaxLogsBeforeWriteStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(deDupeMaxLogsBeforeWriteStr))
        {
            try
            {
                m_deDupeMaxLogsBeforeWrite = Integer.parseInt(deDupeMaxLogsBeforeWriteStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeMaxLogsBeforeWriteStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Integer getDeDupeMaxLogFiles()
    {
        return m_deDupeMaxLogFiles;
    }

    public void setDeDupeMaxLogFiles( Integer m_deDupeMaxLogFiles )
    {
        this.m_deDupeMaxLogFiles = m_deDupeMaxLogFiles;
    }

    public void setDeDupeMaxLogFiles( String deDupeMaxLogFilesStr )
            throws LogCheckException
    {

        if(StringUtils.isNoneBlank(deDupeMaxLogFilesStr))
        {
            try
            {
                m_deDupeMaxLogFiles = Integer.parseInt(deDupeMaxLogFilesStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", deDupeMaxLogFilesStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

    }

    public Integer getDeDupeIgnorePercent()
    {
        return m_deDupeIgnorePercent;
    }

    public void setDeDupeIgnorePercent( Integer m_deDupeIgnorePercent )
    {
        this.m_deDupeIgnorePercent = m_deDupeIgnorePercent;
    }

    public void setDeDupeIgnorePercent( String deDupeIgnorePercentStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(deDupeIgnorePercentStr))
        {
            try
            {
                m_deDupeIgnorePercent = Integer.parseInt(deDupeIgnorePercentStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", m_deDupeIgnorePercent);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

    }

    public Integer getDeDupeSkipPercent()
    {
        return m_deDupeSkipPercent;
    }

    public void setdeDupeSkipPercent( Integer m_deDupeSkipPercent )
    {
        this.m_deDupeSkipPercent = m_deDupeSkipPercent;
    }

    public void setdeDupeSkipPercent( String deDupeSkipPercentStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(deDupeSkipPercentStr))
        {
            try
            {
                m_deDupeSkipPercent = Integer.parseInt(deDupeSkipPercentStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", m_deDupeSkipPercent);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Integer getReadLogFileCount()
    {
        return m_readLogFileCount;
    }

    public void setReadLogFileCount( Integer m_readLogFileCount )
    {
        this.m_readLogFileCount = m_readLogFileCount;
    }

    public void setReadLogFileCount( String readLogFileCountStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(readLogFileCountStr))
        {
            try
            {
                m_readLogFileCount = Integer.parseInt(readLogFileCountStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", readLogFileCountStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public Integer getReadMaxDeDupeEntries()
    {
        return m_readMaxDeDupeEntries;
    }

    public void setReadMaxDeDupeEntries( Integer m_readMaxDeDupeEntries )
    {
        this.m_readMaxDeDupeEntries = m_readMaxDeDupeEntries;
    }

    public void setReadMaxDeDupeEntries( String readMaxDeDupeEntriesStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(readMaxDeDupeEntriesStr))
        {
            try
            {
                m_readMaxDeDupeEntries = Integer.parseInt(readMaxDeDupeEntriesStr);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Error parsing integer '%s'", readMaxDeDupeEntriesStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public String getEmailOnError()
    {
        return m_emailOnError;
    }

    public void setEmailOnError( String m_emailOnError )
    {
        this.m_emailOnError = m_emailOnError;
    }

    public String getSmtpServer()
    {
        return m_smtpServer;
    }

    public void setSmtpServer( String m_smtpServer )
    {
        this.m_smtpServer = m_smtpServer;
    }

    public String getSmtpPort()
    {
        return m_smtpPort;
    }

    public void setSmtpPort( String m_smtpPort )
    {
        this.m_smtpPort = m_smtpPort;
    }

    public String getSmtpPass()
    {
        return m_smtpPass;
    }

    public void setSmtpPass( String m_smtpPass )
    {
        this.m_smtpPass = m_smtpPass;
    }

    public String getSmtpUser()
    {
        return m_smtpUser;
    }

    public void setSmtpUser( String m_smtpUser )
    {
        this.m_smtpUser = m_smtpUser;
    }

    public String getSmtpProto()
    {
        return m_smtpProto;
    }

    public void setSmtpProto( String m_smtpProto )
    {
        this.m_smtpProto = m_smtpProto;
    }

    public String getSetName()
    {
        return m_setName;
    }

    public void setSetName( String m_setName )
    {
        this.m_setName = m_setName;
    }

    public String getElasticsearchIndexName()
    {
        return m_elasticsearchIndexName;
    }

    public void setElasticsearchIndexName( String m_elasticsearchIndexName )
    {
        this.m_elasticsearchIndexName = m_elasticsearchIndexName;
    }

    public String getElasticsearchIndexPrefix()
    {
        return m_elasticsearchIndexPrefix;
    }

    public void setElasticsearchIndexPrefix( String m_elasticsearchIndexPrefix )
    {
        this.m_elasticsearchIndexPrefix = m_elasticsearchIndexPrefix;
    }

    public String getElasticsearchLogType()
    {
        return m_elasticsearchLogType;
    }

    public void setElasticsearchLogType( String m_elasticsearchLogType )
    {
        this.m_elasticsearchLogType = m_elasticsearchLogType;
    }

    public String getWindowsEventConnection()
    {
        return m_windowsEventConnection;
    }

    public void setWindowsEventConnection( String m_windowsEventConnection )
    {
        this.m_windowsEventConnection = m_windowsEventConnection;
    }

    public Boolean getService()
    {
        return m_service;
    }

    public void setService( Boolean m_service )
    {
        this.m_service = m_service;
    }

    public Boolean getDryRun()
    {
        return m_dryRun;
    }

    public void setDryRun( Boolean m_dryRun )
    {
        this.m_dryRun = m_dryRun;
    }

    public Boolean getShowVersion()
    {
        return m_showVersion;
    }

    public void setShowVersion( Boolean m_showVersion )
    {
        this.m_showVersion = m_showVersion;
    }

    public Boolean getTailFromEnd()
    {
        return m_tailFromEnd;
    }

    public void setTailFromEnd( Boolean m_tailFromEnd )
    {
        this.m_tailFromEnd = m_tailFromEnd;
    }

    public Boolean getPrintLog()
    {
        return m_printLog;
    }

    public void setPrintLog( Boolean m_printLog )
    {
        this.m_printLog = m_printLog;
    }

    public Boolean getSaveState()
    {
        return m_saveState;
    }

    public void setSaveState( Boolean m_saveState )
    {
        this.m_saveState = m_saveState;
    }

    public Boolean getCollectState()
    {
        return m_collectState;
    }

    public void setCollectState( Boolean m_collectState )
    {
        this.m_collectState = m_collectState;
    }

    public Boolean getContinueState()
    {
        return m_continueState;
    }

    public void setContinueState( Boolean m_continueState )
    {
        this.m_continueState = m_continueState;
    }

    public Boolean getReadReOpenLogFile()
    {
        return m_readReOpenLogFile;
    }

    public void setReadReOpenLogFile( Boolean m_readReOpenLogFile )
    {
        this.m_readReOpenLogFile = m_readReOpenLogFile;
    }

    public Boolean getStoreReOpenLogFile()
    {
        return m_storeReOpenLogFile;
    }

    public void setStoreReOpenLogFile( Boolean m_storeReOpenLogFile )
    {
        this.m_storeReOpenLogFile = m_storeReOpenLogFile;
    }

    public Boolean getStartPositionIgnoreError()
    {
        return m_startPositionIgnoreError;
    }

    public void setStartPositionIgnoreError( Boolean m_startPositionIgnoreError )
    {
        this.m_startPositionIgnoreError = m_startPositionIgnoreError;
    }

    public Boolean getValidateTailerStats()
    {
        return m_validateTailerStats;
    }

    public void setValidateTailerStats( Boolean m_validateTailerStats )
    {
        this.m_validateTailerStats = m_validateTailerStats;
    }

    public Boolean getTailerBackupReadLog()
    {
        return m_tailerBackupReadLog;
    }

    public void setTailerBackupReadLog( Boolean m_tailerBackupReadLog )
    {
        this.m_tailerBackupReadLog = m_tailerBackupReadLog;
    }

    public Boolean getTailerBackupReadLogReverse()
    {
        return m_tailerBackupReadLogReverse;
    }

    public void setTailerBackupReadLogReverse( Boolean m_tailerBackupReadLogReverse )
    {
        this.m_tailerBackupReadLogReverse = m_tailerBackupReadLogReverse;
    }

    public Boolean getTailerBackupReadPriorLog()
    {
        return m_tailerBackupReadPriorLog;
    }

    public void setTailerBackupReadPriorLog( Boolean m_tailerBackupReadPriorLog )
    {
        this.m_tailerBackupReadPriorLog = m_tailerBackupReadPriorLog;
    }

    public Boolean getStopOnEOF()
    {
        return m_stopOnEOF;
    }

    public void setStopOnEOF( Boolean m_stopOnEOF )
    {
        this.m_stopOnEOF = m_stopOnEOF;
    }

    public Boolean getReadOnlyFileMode()
    {
        return m_readOnlyFileMode;
    }

    public void setReadOnlyFileMode( Boolean m_readOnlyFileMode )
    {
        this.m_readOnlyFileMode = m_readOnlyFileMode;
    }

    public Boolean getCreateMissingDirs()
    {
        return m_createMissingDirs;
    }

    public void setCreateMissingDirs( Boolean m_createMissingDirs )
    {
        this.m_createMissingDirs = m_createMissingDirs;
    }

    public Path getLockFilePath()
    {
        return m_lockFilePath;
    }

    public void setLockFilePath( Path m_lockFilePath )
    {
        this.m_lockFilePath = m_lockFilePath;
    }

    public void setLockFilePath( String lockFilePathStr )
    {
        if(StringUtils.isNoneBlank(lockFilePathStr))
        {
            m_lockFilePath = Paths.get(lockFilePathStr);
        }
    }

    public Path getLogPath()
    {
        return m_logPath;
    }

    public void setLogPath( Path m_logPath )
    {
        this.m_logPath = m_logPath;
    }

    public void setLogPath( String logPathStr )
    {
        if(StringUtils.isNoneBlank(logPathStr))
        {
            m_logPath = Paths.get(logPathStr);
        }
    }

    public Path getStoreLogPath()
    {
        return m_storeLogPath;
    }

    public void setStoreLogPath( Path m_storeLogPath )
    {
        this.m_storeLogPath = m_storeLogPath;
    }

    public void setStoreLogPath( String storeLogPathStr )
    {
        if(StringUtils.isNoneBlank(storeLogPathStr))
        {
            m_storeLogPath = Paths.get(storeLogPathStr);
        }
    }

    public Path getStatusFilePath()
    {
        return m_statusFilePath;
    }

    public void setStatusFilePath( Path m_statusFilePath )
    {
        this.m_statusFilePath = m_statusFilePath;
    }

    public void setStatusFilePath( String statusFilePathStr )
    {
        if(StringUtils.isNoneBlank(statusFilePathStr))
        {
            m_statusFilePath = Paths.get(statusFilePathStr);
        }
    }

    public Path getStateFilePath()
    {
        return m_stateFilePath;
    }

    public void setStateFilePath( Path m_stateFilePath )
    {
        this.m_stateFilePath = m_stateFilePath;
    }

    public void setStateFilePath( String stateFilePathStr )
    {
        if(StringUtils.isNoneBlank(stateFilePathStr))
        {
            m_stateFilePath = Paths.get(stateFilePathStr);
        }
    }

    public Path getStateProcessedLogsFilePath()
    {
        return m_stateProcessedLogsFilePath;
    }

    public void setStateProcessedLogsFilePath( Path m_stateProcessedLogsFilePath )
    {
        this.m_stateProcessedLogsFilePath = m_stateProcessedLogsFilePath;
    }

    public void setStateProcessedLogsFilePath( String stateProcessedLogsFilePathStr )
    {
        if(StringUtils.isNoneBlank(stateProcessedLogsFilePathStr))
        {
            m_stateProcessedLogsFilePath = Paths.get(stateProcessedLogsFilePathStr);
        }
    }

    public Path getErrorFilePath()
    {
        return m_errorFilePath;
    }

    public void setErrorFilePath( Path m_errorFilePath )
    {
        this.m_errorFilePath = m_errorFilePath;
    }

    public void setErrorFilePath( String errorFilePathStr )
    {
        if(StringUtils.isNoneBlank(errorFilePathStr))
        {
            m_errorFilePath = Paths.get(errorFilePathStr);
        }
    }

    public Path getConfigFilePath()
    {
        return m_configFilePath;
    }

    public void setConfigFilePath( Path m_configFilePath )
    {
        this.m_configFilePath = m_configFilePath;
    }

    public void setConfigFilePath( String configFilePathStr )
    {
        if(StringUtils.isNoneBlank(configFilePathStr))
        {
            m_configFilePath = Paths.get(configFilePathStr);
        }
    }

    public Path getHoldingDirPath()
    {
        return m_holdingDirPath;
    }

    public void setHoldingDirPath( Path m_holdingDirPath )
    {
        this.m_holdingDirPath = m_holdingDirPath;
    }

    public void setHoldingDirPath( String holdingDirPathStr )
    {
        if(StringUtils.isNoneBlank(holdingDirPathStr))
        {
            m_holdingDirPath = Paths.get(holdingDirPathStr);
        }
    }

    public Path getDeDupeDirPath()
    {
        return m_deDupeDirPath;
    }

    public void setDeDupeDirPath( Path m_deDupeDirPath )
    {
        this.m_deDupeDirPath = m_deDupeDirPath;
    }

    public void setDeDupeDirPath( String deDupeDirPathStr )
    {
        if(StringUtils.isNoneBlank(deDupeDirPathStr))
        {
            m_deDupeDirPath = Paths.get(deDupeDirPathStr);
        }
    }

    public Path getTailerLogBackupDir()
    {
        return m_tailerLogBackupDir;
    }

    public void setTailerLogBackupDir( Path m_tailerLogBackupDir )
    {
        this.m_tailerLogBackupDir = m_tailerLogBackupDir;
    }

    public void setTailerLogBackupDir( String tailerLogBackupDirStr )
    {
        if(StringUtils.isNoneBlank(tailerLogBackupDirStr))
        {
            m_tailerLogBackupDir = Paths.get(tailerLogBackupDirStr);
        }
    }

    public Path getPreferredDir()
    {
        return m_preferredDir;
    }

    public void setPreferredDir( Path m_preferredDir )
    {
        this.m_preferredDir = m_preferredDir;
    }

    public void setPreferredDir( String preferredDirStr )
    {
        if(StringUtils.isNoneBlank(preferredDirStr))
        {
            m_preferredDir = Paths.get(preferredDirStr);
        }
    }

    public Path getStdOutFile()
    {
        return m_stdOutFile;
    }

    public void setStdOutFile( Path m_stdOutFile )
    {
        this.m_stdOutFile = m_stdOutFile;
    }

    public void setStdOutFile( String stdOutFileStr )
    {
        if(StringUtils.isNoneBlank(stdOutFileStr))
        {
            m_stdOutFile = Paths.get(stdOutFileStr);
        }
    }

    public URL getElasticsearchURL()
    {
        return m_elasticsearchURL;
    }

    public void setElasticsearchURL( URL m_elasticsearchURL )
    {
        this.m_elasticsearchURL = m_elasticsearchURL;
    }

    public void setElasticsearchURL( String elasticsearchURLStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(elasticsearchURLStr))
        {
            try
            {
                m_elasticsearchURL = new URL(elasticsearchURLStr);
            }
            catch(MalformedURLException ex)
            {
                String errMsg = String.format("Invalid URL string '%s'", elasticsearchURLStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public URL getMonitorURL()
    {
        return m_monitorURL;
    }

    public void setMonitorURL( URL m_monitorURL )
    {
        this.m_monitorURL = m_monitorURL;
    }

    public void setMonitorURL( String monitorURLStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(monitorURLStr))
        {
            try
            {
                m_monitorURL = new URL(monitorURLStr);
            }
            catch(MalformedURLException ex)
            {
                String errMsg = String.format("Invalid URL string '%s'", monitorURLStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public LocalTime getLogCutoffDate()
    {
        return m_logCutoffDate;
    }

    public void setLogCutoffDate( LocalTime m_logCutoffDate )
    {
        this.m_logCutoffDate = m_logCutoffDate;
    }

    public void setLogCutoffDate( String logCutoffDateStr )
    {
        if(StringUtils.isNoneBlank(logCutoffDateStr))
        {
            m_logCutoffDate = LocalTime.parse(logCutoffDateStr);
        }
    }

    public Duration getLogCutoffDuration()
    {
        return m_logCutoffDuration;
    }

    public void setLogCutoffDuration( Duration m_logCutoffDuration )
    {
        this.m_logCutoffDuration = m_logCutoffDuration;
    }

    public void setLogCutoffDuration( String logCutoffDurationStr )
    {
        if(StringUtils.isNoneBlank(logCutoffDurationStr))
        {
            m_logCutoffDuration = Duration.parse(logCutoffDurationStr);
        }
    }

    public Duration getLogDeduplicationDuration()
    {
        return m_logDeduplicationDuration;
    }

    public void setLogDeduplicationDuration( Duration m_logDeduplicationDuration )
    {
        this.m_logDeduplicationDuration = m_logDeduplicationDuration;
    }

    public void setLogDeduplicationDuration( String logDeduplicationDurationStr )
    {
        if(StringUtils.isNoneBlank(logDeduplicationDurationStr))
        {
            m_logDeduplicationDuration
                    = Duration.parse(logDeduplicationDurationStr);
        }
    }

    public LCDeDupeAction getDeDupeDefaultAction()
    {
        return m_deDupeDefaultAction;
    }

    public void setDeDupeDefaultAction( LCDeDupeAction m_deDupeDefaultAction )
    {
        this.m_deDupeDefaultAction = m_deDupeDefaultAction;
    }

    public void setDeDupeDefaultAction( String deDupeDefaultActionStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(deDupeDefaultActionStr))
        {
            try
            {
                m_deDupeDefaultAction = LCDeDupeAction.from(deDupeDefaultActionStr);
            }
            catch( LogCheckException ex )
            {
                String errMsg = String.format("Error parsing Deduplication Default Action '%s'", deDupeDefaultActionStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public LCIndexNameFormat getElasticsearchIndexNameFormat()
    {
        return m_elasticsearchIndexNameFormat;
    }

    public void setElasticsearchIndexNameFormat( LCIndexNameFormat m_elasticsearchIndexNameFormat )
    {
        this.m_elasticsearchIndexNameFormat = m_elasticsearchIndexNameFormat;
    }

    public void setElasticsearchIndexNameFormat( String elasticsearchIndexNameFormatStr )
    {
        if(StringUtils.isNoneBlank(elasticsearchIndexNameFormatStr))
        {
            m_elasticsearchIndexNameFormat
                    = LCIndexNameFormat.from(elasticsearchIndexNameFormatStr);
        }
    }

    public List<LCLogEntryBuilderType> getLogEntryBuilders()
    {
        return m_logEntryBuilders;
    }

    public void setLogEntryBuilders( List<LCLogEntryBuilderType> m_logEntryBuilders )
    {
        this.m_logEntryBuilders = m_logEntryBuilders;
    }

    public void setLogEntryBuilderStrs( String[] logEntryBuilderStrs )
    {
        if(logEntryBuilderStrs != null )
        {
            m_logEntryBuilders = new ArrayList<>(10);

            for( String builder : logEntryBuilderStrs )
            {
                if( StringUtils.isNoneBlank(builder) )
                {
                    m_logEntryBuilders.add(LCLogEntryBuilderType.from(builder));
                }
            }
        }
    }

    public List<LCLogEntryStoreType> getLogEntryStores()
    {
        return m_logEntryStores;
    }

    public void setLogEntryStores( List<LCLogEntryStoreType> m_logEntryStores )
    {
        this.m_logEntryStores = m_logEntryStores;
    }

    public void setLogEntryStoreStrs( String[] logEntryStoreStrs )
    {
        if(logEntryStoreStrs != null )
        {
            m_logEntryStores = new ArrayList<>(10);

            for( String store : logEntryStoreStrs )
            {
                if( StringUtils.isNoneBlank(store) )
                {
                    m_logEntryStores.add(LCLogEntryStoreType.from(store));
                }
            }
        }
    }

    public List<LCFileRegexComponent> getTailerBackupLogNameComps()
    {
        return m_tailerBackupLogNameComps;
    }

    public void setTailerBackupLogNameComps( List<LCFileRegexComponent> m_tailerBackupLogNameComps )
    {
        this.m_tailerBackupLogNameComps = m_tailerBackupLogNameComps;
    }

    public void setTailerBackupLogNameCompStrs( String[] tailerBackupLogNameCompStrs )
    {
        m_tailerBackupLogNameComps
                = LCFileRegexComponent.from(tailerBackupLogNameCompStrs);
    }

    public Set<LCDebugFlag> getDebugFlags()
    {
        return m_debugFlags;
    }

    public void setDebugFlags( Set<LCDebugFlag> m_debugFlags )
    {
        this.m_debugFlags = m_debugFlags;
    }

    public void setDebugFlagStrs( String[] debugFlagStrs )
    {
        m_debugFlags = LCDebugFlag.from(debugFlagStrs);
    }

    public LCHashType getIdBlockHashType()
    {
        return m_idBlockHashType;
    }

    public void setIdBlockHashType( LCHashType m_idBlockHashType )
    {
        this.m_idBlockHashType = m_idBlockHashType;
    }

    public void setIdBlockHashType( String idBlockHashTypeStr )
            throws LogCheckException
    {

        if(StringUtils.isNoneBlank(idBlockHashTypeStr))
        {
            try
            {
                m_idBlockHashType = LCHashType.from(idBlockHashTypeStr);
            }
            catch( LogCheckException ex )
            {
                String errMsg = String.format("Error parsing ID Block Hash '%s'", idBlockHashTypeStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    public LCCompressionType getTailerBackupLogCompression()
    {
        return m_tailerBackupLogCompression;
    }

    public void setTailerBackupLogCompression( LCCompressionType m_tailerBackupLogCompression )
    {
        this.m_tailerBackupLogCompression = m_tailerBackupLogCompression;
    }

    public void setTailerBackupLogCompression( String tailBackupLogCompressionStr )
    {
        if( StringUtils.isNoneBlank(tailBackupLogCompressionStr))
        {
            m_tailerBackupLogCompression = LCCompressionType.from(tailBackupLogCompressionStr);
        }
    }


    public Pattern getTailerBackupLogNameRegex()
    {
        return m_tailerBackupLogNameRegex;
    }

    public void setTailerBackupLogNameRegex( Pattern m_tailerBackupLogNameRegex )
    {
        this.m_tailerBackupLogNameRegex = m_tailerBackupLogNameRegex;
    }

    public void setTailerBackupLogNameRegex( String tailBackupLogNameRegexStr )
    {
        if(StringUtils.isNoneBlank(tailBackupLogNameRegexStr))
        {
            m_tailerBackupLogNameRegex = Pattern.compile(tailBackupLogNameRegexStr);
        }
    }

    public LCLogSourceType getLogSourceType()
    {
        return m_logSourceType;
    }

    public void setLogSourceType( LCLogSourceType m_logSourceType )
    {
        this.m_logSourceType = m_logSourceType;
    }

    public void setLogSourceType( String logSourceTypeStr )
            throws LogCheckException
    {

        if(StringUtils.isNoneBlank(logSourceTypeStr))
        {
            try
            {
                m_logSourceType = LCLogSourceType.from(logSourceTypeStr);
            }
            catch( Exception ex )
            {
                String errMsg = String.format("Error parsing log source type '%s'", logSourceTypeStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }

    }

    public FSSVerbosityEnum getVerbosity()
    {
        return m_verbosity;
    }

    public void setVerbosity( FSSVerbosityEnum m_verbosity )
    {
        this.m_verbosity = m_verbosity;
    }

    public void setVerbosity( String verbosityStr )
            throws LogCheckException
    {
        if(StringUtils.isNoneBlank(verbosityStr))
        {
            try
            {
                m_verbosity = FSSVerbosityEnum.from(verbosityStr);
            }
            catch( Exception ex )
            {
                String errMsg = String.format("Error parsing verbosity '%s'", verbosityStr);

                LOGGER.debug(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    private LogCheckConfigBuilder()
    {
        ;
    }

    public static LogCheckConfigBuilder from()
    {
        return from(null);
    }

    public static LogCheckConfigBuilder from(final Integer id)
    {
        LogCheckConfigBuilder res = new LogCheckConfigBuilder();

        res.setId(id);

        return res;
    }
    public LogCheckConfig toConfig(final LogCheckConfig orig)
            throws LogCheckException
    {
        LogCheckConfig res = LogCheckConfig.from(orig,
                getId(),
                getService(),
                getEmailOnError(),
                getSmtpServer(),
                getSmtpPort(),
                getSmtpPass(),
                getSmtpUser(),
                getSmtpProto(),
                getSetName(),
                getDryRun(),
                getShowVersion(),
                getPrintLog(),
                getTailFromEnd(),
                getReadReOpenLogFile(),
                getStoreReOpenLogFile(),
                getSaveState(),
                getCollectState(),
                getContinueState(),
                getStartPositionIgnoreError(),
                getValidateTailerStats(),
                getTailerBackupReadLog(),
                getTailerBackupReadLogReverse(),
                getTailerBackupReadPriorLog(),
                getStopOnEOF(),
                getReadOnlyFileMode(),
                getCreateMissingDirs(),
                getLockFilePath(),
                getLogPath(),
                getStoreLogPath(),getStatusFilePath(),
                getStateFilePath(),
                getStateProcessedLogsFilePath(),
                getErrorFilePath(),
                getConfigFilePath(),
                getHoldingDirPath(),
                getDeDupeDirPath(),
                getTailerLogBackupDir(),
                getPreferredDir(),
                getStdOutFile(),
                getElasticsearchURL(),
                getMonitorURL(),
                getElasticsearchIndexName(),
                getElasticsearchIndexPrefix(),
                getElasticsearchLogType(),
                getWindowsEventConnection(),
                getElasticsearchIndexNameFormat(),
                getLogCutoffDate(),
                getLogCutoffDuration(),
                getLogDeduplicationDuration(),
                getPollIntervalSeconds(),
                getStopAfter(),
                getDeDupeIgnoreCount(),
                getDeDupeSkipCount(),
                getReadLogFileCount(),
                getReadMaxDeDupeEntries(),
                getIdBlockSize(),
                getDeDupeMaxLogsBeforeWrite(),
                getDeDupeMaxLogsPerFile(),
                getDeDupeMaxLogFiles(),
                getDeDupeIgnorePercent(),
                getDeDupeSkipPercent(),
                getVerbosity(),
                getDeDupeDefaultAction(),
                getLogEntryBuilders(),
                getLogEntryStores(),
                getTailerBackupLogNameComps(),
                getIdBlockHashType(),
                getTailerBackupLogCompression(),
                getTailerBackupLogNameRegex(),
                getLogSourceType(),
                getDebugFlags());

        return res;
    }
}
