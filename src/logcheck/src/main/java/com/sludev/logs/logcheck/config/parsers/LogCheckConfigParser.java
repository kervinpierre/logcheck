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

import com.sludev.logs.logcheck.config.builders.LogCheckConfigBuilder;
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
import java.util.LinkedHashMap;
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

    public static LinkedHashMap<Integer,LogCheckConfig> readConfig( Document doc) throws LogCheckException
    {
        LinkedHashMap<Integer,LogCheckConfig> res = new LinkedHashMap<>(10);

        Element currEl;

        currEl = doc.getDocumentElement();

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./logCheckConfig").evaluate(
                    currEl, XPathConstants.NODESET);

            if( (currElList != null) && (currElList.getLength() > 0) )
            {
                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element) currElList.item(i);

                    LogCheckConfig currConf = readConfig(tempEl);
                    res.put(currConf.getId(), currConf);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }


        return res;
    }

    public static LogCheckConfig readConfig( Element el ) throws LogCheckException
    {
        LogCheckConfig res;

        Element currEl;

        currEl = el;

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        LogCheckConfigBuilder currBuilder = LogCheckConfigBuilder.from();

        try
        {
            String tempStr = currXPath.compile("./@id").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currBuilder.setId(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./logSourceType").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currBuilder.setLogSourceType(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./monitorURL").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currBuilder.setMonitorURL(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./windowsEventConnection").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currBuilder.setWindowsEventConnection(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./holdingFolder").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currBuilder.setHoldingDirPath(tempStr);
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
                    String[] leBuilderType = new String[tempList.size()];
                    leBuilderType = tempList.toArray(leBuilderType);
                    currBuilder.setLogEntryBuilderStrs(leBuilderType);
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
                    String[] leStoreType = new String[tempList.size()];
                    leStoreType = tempList.toArray(leStoreType);
                    currBuilder.setLogEntryStoreStrs(leStoreType);
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
                    String[] tailerBackupLogNameComp = new String[tempList.size()];
                    tailerBackupLogNameComp = tempList.toArray(tailerBackupLogNameComp);
                    currBuilder.setTailerBackupLogNameCompStrs(tailerBackupLogNameComp);
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
                    String[] debugFlags = new String[tempList.size()];
                    debugFlags = tempList.toArray(debugFlags);
                    currBuilder.setDebugFlagStrs(debugFlags);
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
                currBuilder.setPollIntervalSeconds(tempStr);
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
                currBuilder.setSmtpServer(tempStr);
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
                currBuilder.setSmtpUser(tempStr);
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
                currBuilder.setSmtpPass(tempStr);
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
                currBuilder.setSmtpPort(tempStr);
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
                currBuilder.setSmtpProto(tempStr);
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
                currBuilder.setDryRun(Boolean.parseBoolean(tempStr));
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
                currBuilder.setSaveState(Boolean.parseBoolean(tempStr));
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
                currBuilder.setLockFilePath(tempStr);
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
                currBuilder.setLockFilePath(tempStr);
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
                currBuilder.setElasticsearchURL(tempStr);
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
                currBuilder.setStatusFilePath(tempStr);
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
                currBuilder.setStateFilePath(tempStr);
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
                currBuilder.setStateProcessedLogsFilePath(tempStr);
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
                currBuilder.setStoreLogPath(tempStr);
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
                currBuilder.setErrorFilePath(tempStr);
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
                currBuilder.setIdBlockHashType(tempStr);
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
                currBuilder.setIdBlockSize(tempStr);
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
                currBuilder.setSetName(tempStr);
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
                currBuilder.setTailerLogBackupDir(tempStr);
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
                currBuilder.setTailerBackupLogNameRegex(tempStr);
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
                currBuilder.setDeDupeDirPath(tempStr);
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
                currBuilder.setDeDupeMaxLogsPerFile(tempStr);
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
                currBuilder.setDeDupeMaxLogsBeforeWrite(tempStr);
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
                currBuilder.setDeDupeMaxLogFiles(tempStr);
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
                currBuilder.setDeDupeIgnoreCount(tempStr);
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
                currBuilder.setdeDupeSkipPercent(tempStr);
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
                currBuilder.setDeDupeIgnorePercent(tempStr);
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
                currBuilder.setDeDupeDefaultAction(tempStr);
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
                currBuilder.setDeDupeSkipCount(tempStr);
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
                currBuilder.setLogDeduplicationDuration(tempStr);
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
                currBuilder.setStopAfter(tempStr);
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
                currBuilder.setReadLogFileCount(tempStr);
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
                currBuilder.setReadMaxDeDupeEntries(tempStr);
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
                currBuilder.setVerbosity(tempStr);
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
                currBuilder.setEmailOnError(tempStr);
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
                currBuilder.setContinueState(Boolean.parseBoolean(tempStr));
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
                currBuilder.setTailFromEnd(Boolean.parseBoolean(tempStr));
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
                currBuilder.setStoreReOpenLogFile(Boolean.parseBoolean(tempStr));
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
                currBuilder.setTailerBackupReadPriorLog(Boolean.parseBoolean(tempStr));
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
                currBuilder.setValidateTailerStats(Boolean.parseBoolean(tempStr));
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
                currBuilder.setService(Boolean.parseBoolean(tempStr));
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
                currBuilder.setReadReOpenLogFile(Boolean.parseBoolean(tempStr));
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
                currBuilder.setPreferredDir(tempStr);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./stdOutFile").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currBuilder.setStdOutFile(tempStr);
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
                currBuilder.setStopOnEOF(Boolean.parseBoolean(tempStr));
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
                currBuilder.setStartPositionIgnoreError(Boolean.parseBoolean(tempStr));
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
                currBuilder.setCollectState(Boolean.parseBoolean(tempStr));
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
                currBuilder.setTailerBackupReadLog(Boolean.parseBoolean(tempStr));
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
                currBuilder.setTailerBackupReadLogReverse(Boolean.parseBoolean(tempStr));
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./createMissingDirs").evaluate(currEl);
            if( StringUtils.isNoneBlank(tempStr) )
            {
                currBuilder.setCreateMissingDirs(Boolean.parseBoolean(tempStr));
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        res = currBuilder.toConfig(null);

        return res;
    }


}
