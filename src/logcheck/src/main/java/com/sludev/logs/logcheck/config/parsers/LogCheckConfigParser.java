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
package com.sludev.logs.logcheck.config.parsers;

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class LogCheckConfigParser
{
    private static final Logger LOGGER
                          = LogManager.getLogger(LogCheckConfigParser.class);
    
    public static LogCheckConfig readConfig(Document doc) throws LogCheckException
    {
        LogCheckConfig res;

        Element currEl;

        currEl = doc.getDocumentElement();
        
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        String holdingDirStr = null;
        String pollIntervalStr = null;
        String smtpServerStr = null;
        String smtpUserStr = null;
        String smtpPassStr = null;
        String smtpPortStr = null;
        String smtpProtocolStr = null;
        String verbosity = null;
        Boolean dryRun = null;
        Boolean currSaveState = null;
        Boolean continueState = null;
        Boolean collectState = null;
        Boolean tailFromEnd = null;
        Boolean reOpenLogFile = null;
        Boolean tailerBackupReadPriorLog = null;
        Boolean validateTailerStats = null;
        Boolean tailerBackupReadLog = null;
        Boolean tailerBackupReadLogReverse = null;
        Boolean currService = null;
        Boolean readOnlyFileMode = null;
        Boolean stopOnEOF = null;
        Boolean startPositionIgnoreError = null;
        String emailOnError = null;
        String lockFileStr = null;
        String elasticsearchURLStr = null;
        String logFileStr = null;
        String storeLogPathStr = null;
        String statusFileStr = null;
        String stateFileStr = null;
        String stateProcessedLogFileStr = null;
        String errorFileStr = null;
        String idBlockSize = null;
        String idBlockHashType = null;
        String setName = null;
        String tailerLogBackupDir = null;
        String preferredDir = null;
        String tailerBackupLogNameRegexStr = null;
        String deDupeDir = null;
        String deDupeMaxLogsPerFile = null;
        String deDupeMaxLogsBeforeWrite = null;
        String deDupeMaxLogFiles = null;
        String stopAfterStr = null;
        String readLogFileCountStr = null;
        String readMaxDeDupeEntriesStr = null;
        String logDeduplicationDuration = null;
        String deDuplicationIgnoreUntilCount = null;
        String deDuplicationSkipUntilCount = null;
        String deDuplicationIgnoreUntilPercent = null;
        String deDuplicationSkipUntilPercent = null;
        String deDuplicationDefaultAction = null;
        String[] leBuilderType = null;
        String[] leStoreType = null;
        String[] tailerBackupLogNameComp = null;
        String[] debugFlags = null;

        try
        {
            String tempStr = currXPath.compile("./holdingFolder").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                holdingDirStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./logEntryBuilders/builder").evaluate(
                    currEl, XPathConstants.NODESET);

            if( (currElList != null) && (currElList.getLength() > 0) )
            {
                List<String> tempList = new ArrayList<>();
                Set<String> tempSet = new HashSet<>();

                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element)currElList.item(i);
                    String tempStr = tempEl.getTextContent();

                    if( StringUtils.isNoneBlank(tempStr) && (tempSet.contains(tempStr) == false) )
                    {
                        tempList.add(tempStr);
                        tempSet.add(tempStr);
                    }
                }

                if( tempList.size() > 0 )
                {
                    leBuilderType = new String[tempList.size()];
                    leBuilderType = tempList.toArray(leBuilderType);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error <logEntryBuilders>.", ex);
        }

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./logEntryStores/store").evaluate(
                    currEl, XPathConstants.NODESET);

            if( (currElList != null) && (currElList.getLength() > 0) )
            {
                List<String> tempList = new ArrayList<>();
                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element)currElList.item(i);
                    String tempStr = tempEl.getTextContent();

                    if( StringUtils.isNoneBlank(tempStr) )
                    {
                        tempList.add(tempStr);
                    }
                }

                if( tempList.size() > 0 )
                {
                    leStoreType = new String[tempList.size()];
                    leStoreType = tempList.toArray(leStoreType);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error <logEntryStores>.", ex);
        }

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./tailerBackupLogNameComps/nameComponent").evaluate(
                    currEl, XPathConstants.NODESET);

            if( (currElList != null) && (currElList.getLength() > 0) )
            {
                List<String> tempList = new ArrayList<>();
                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element)currElList.item(i);
                    String tempStr = tempEl.getTextContent();

                    if( StringUtils.isNoneBlank(tempStr) )
                    {
                        tempList.add(tempStr);
                    }
                }

                if( tempList.size() > 0 )
                {
                    tailerBackupLogNameComp = new String[tempList.size()];
                    tailerBackupLogNameComp = tempList.toArray(tailerBackupLogNameComp);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error <tailerBackupLogNameComps>.", ex);
        }

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./debugFlags/flag").evaluate(
                    currEl, XPathConstants.NODESET);

            if( (currElList != null) && (currElList.getLength() > 0) )
            {
                List<String> tempList = new ArrayList<>();
                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element)currElList.item(i);
                    String tempStr = tempEl.getTextContent();

                    if( StringUtils.isNoneBlank(tempStr) )
                    {
                        tempList.add(tempStr);
                    }
                }

                if( tempList.size() > 0 )
                {
                    debugFlags = new String[tempList.size()];
                    debugFlags = tempList.toArray(debugFlags);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error <tailerBackupLogNameComps>.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./pollInterval").evaluate(currEl);

            if( StringUtils.isNoneBlank(tempStr) )
            {
                pollIntervalStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./smtpServer").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                smtpServerStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./smtpUser").evaluate(currEl);

            if( StringUtils.isNoneBlank(tempStr) )
            {
                smtpUserStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./smtpPass").evaluate(currEl);

            if( StringUtils.isNoneBlank(tempStr) )
            {
                smtpPassStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./smtpPort").evaluate(currEl);

            if( StringUtils.isNoneBlank(tempStr) )
            {
                smtpPortStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./smtpProtocol").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                smtpProtocolStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./dryRun").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                dryRun = Boolean.parseBoolean(tempStr);
            }
        }
        catch(Exception ex)
        {
            LOGGER.debug("configuration parsing error 'dryRun'.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./saveState").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currSaveState = Boolean.parseBoolean(tempStr);
            }
        }
        catch(Exception ex)
        {
            LOGGER.debug("configuration parsing error 'saveState'.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./lockFilePath").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                lockFileStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./logFilePath").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                logFileStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./elasticsearchURL").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                elasticsearchURLStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./statusFilePath").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                statusFileStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr  = currXPath.compile("./stateFilePath").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                stateFileStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr  = currXPath.compile("./stateProcessedLogFilePath").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                stateProcessedLogFileStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr  = currXPath.compile("./storeLogPath").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                storeLogPathStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./errorFilePath").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                errorFileStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./idBlockHashType").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                idBlockHashType = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./idBlockSize").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                idBlockSize = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./setName").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                setName = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./tailerLogBackupDir").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                tailerLogBackupDir = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./tailerBackupLogNameRegex").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                tailerBackupLogNameRegexStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationLogDir").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDupeDir = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }
        try
        {
            String tempStr = currXPath.compile("./deDuplicationMaxLogsPerFile").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDupeMaxLogsPerFile = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationMaxLogsBeforeWrite").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDupeMaxLogsBeforeWrite = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationMaxLogFiles").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDupeMaxLogFiles = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationIgnoreUntilCount").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDuplicationIgnoreUntilCount = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationSkipUntilPercent").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDuplicationSkipUntilPercent = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationIgnoreUntilPercent").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDuplicationIgnoreUntilPercent = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationDefaultAction").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDuplicationDefaultAction = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationSkipUntilCount").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                deDuplicationSkipUntilCount = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./deDuplicationDuration").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                logDeduplicationDuration = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./stopAfter").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                stopAfterStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./readLogFileCount").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                readLogFileCountStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./readMaxDeDupeEntries").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                readMaxDeDupeEntriesStr = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./verbosity").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                verbosity = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./emailOnError").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                emailOnError = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./continue").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                continueState = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./tailFromEnd").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                tailFromEnd = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./reOpenLogFile").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                reOpenLogFile = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./tailerBackupReadPriorLog").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                tailerBackupReadPriorLog = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./validateTailerStats").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                validateTailerStats = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./service").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currService = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./readOnlyLogFile").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                readOnlyFileMode = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./preferredDir").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                preferredDir = tempStr;
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./stopOnEOF").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                stopOnEOF = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./ignoreStartPosError").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                startPositionIgnoreError = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./collectState").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                collectState = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./tailerBackupReadLog").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                tailerBackupReadLog = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./tailerBackupReadLogReverse").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                tailerBackupReadLogReverse = Boolean.parseBoolean(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        res = LogCheckConfig.from(null,
                currService,
                emailOnError,
                smtpServerStr,
                smtpPortStr,
                smtpPassStr,
                smtpUserStr,
                smtpProtocolStr,
                setName,
                dryRun,
                null, // showVersion,
                null, // printLog,
                tailFromEnd,
                reOpenLogFile,
                null, // storeReOpenLogFile
                currSaveState,
                collectState,
                continueState,
                startPositionIgnoreError,
                validateTailerStats,
                tailerBackupReadLog,
                tailerBackupReadLogReverse,
                tailerBackupReadPriorLog,
                stopOnEOF,
                readOnlyFileMode,
                null, // createMissingDirs
                lockFileStr,
                logFileStr,
                storeLogPathStr,
                statusFileStr,
                stateFileStr,
                stateProcessedLogFileStr,
                errorFileStr,
                null, // configFilePath,
                holdingDirStr,
                deDupeDir,
                tailerLogBackupDir,
                preferredDir,
                elasticsearchURLStr,
                null, // elasticsearchIndexName,
                null, // elasticsearchIndexPrefix,
                null, // elasticsearchLogType,
                null, // elasticsearchIndexNameFormat,
                null, // logCutoffDate,
                null, // logCutoffDuration,
                logDeduplicationDuration,
                pollIntervalStr,
                stopAfterStr,
                deDuplicationIgnoreUntilCount,
                deDuplicationSkipUntilCount,
                readLogFileCountStr,
                readMaxDeDupeEntriesStr,
                idBlockSize,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile,
                deDupeMaxLogFiles,
                deDuplicationIgnoreUntilPercent,
                deDuplicationSkipUntilPercent,
                verbosity,
                deDuplicationDefaultAction,
                leBuilderType,
                leStoreType,
                tailerBackupLogNameComp,
                idBlockHashType,
                null, // tailerBackupLogCompression
                tailerBackupLogNameRegexStr,
                debugFlags
                 );

        return res;
    }
}
