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
import com.sludev.logs.logcheck.utils.LogCheckException;
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
import java.util.List;

/**
 *
 * @author Administrator
 */
public class LogCheckConfigParser
{
    private static final Logger log 
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
        Boolean dryRun = null;
        Boolean currSaveState = null;
        Boolean continueState = null;
        Boolean reOpenLogFile = null;
        String lockFileStr = null;
        String elasticsearchURLStr = null;
        String logFileStr = null;
        String statusFileStr = null;
        String stateFileStr = null;
        String errorFileStr = null;
        String idBlockSize = null;
        String idBlockHashType = null;
        String setName = null;
        String deDupeDir = null;
        String deDupeMaxLogsPerFile = null;
        String deDupeMaxLogsBeforeWrite = null;
        String deDupeMaxLogFiles = null;
        String stopAfterStr = null;
        String readLogFileCountStr = null;
        String readMaxDeDupeEntriesStr = null;
        String[] leBuilderType = null;
        String[] leStoreType = null;

        try
        {
            holdingDirStr = currXPath.compile("./holdingFolder").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./logEntryBuilders/builder").evaluate(
                    currEl, XPathConstants.NODESET);

            if( currElList != null && currElList.getLength() > 0)
            {
                List<String> tempList = new ArrayList<>();
                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element)currElList.item(i);
                    tempList.add( tempEl.getTextContent() );
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error <logEntryBuilders>.", ex);
        }

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./logEntryStores/store").evaluate(
                    currEl, XPathConstants.NODESET);

            if( currElList != null && currElList.getLength() > 0)
            {
                List<String> tempList = new ArrayList<>();
                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element)currElList.item(i);
                    tempList.add( tempEl.getTextContent() );
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error <logEntryStores>.", ex);
        }

        try
        {
            pollIntervalStr = currXPath.compile("./pollInterval").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpServerStr = currXPath.compile("./smtpServer").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpUserStr = currXPath.compile("./smtpUser").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpPassStr = currXPath.compile("./smtpPass").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpPortStr = currXPath.compile("./smtpPort").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpProtocolStr = currXPath.compile("./smtpProtocol").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./dryRun").evaluate(currEl);
            dryRun = Boolean.parseBoolean(tempStr);
        }
        catch(Exception ex)
        {
            log.debug("configuration parsing error 'dryRun'.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./saveState").evaluate(currEl);
            currSaveState = Boolean.parseBoolean(tempStr);
        }
        catch(Exception ex)
        {
            log.debug("configuration parsing error 'saveState'.", ex);
        }

        try
        {
            lockFileStr = currXPath.compile("./lockFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            logFileStr = currXPath.compile("./logFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            elasticsearchURLStr = currXPath.compile("./elasticsearchURL").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            statusFileStr = currXPath.compile("./statusFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            stateFileStr = currXPath.compile("./stateFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            errorFileStr = currXPath.compile("./errorFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            idBlockHashType = currXPath.compile("./idBlockHashType").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            idBlockSize = currXPath.compile("./idBlockSize").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            setName = currXPath.compile("./setName").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            deDupeDir = currXPath.compile("./deDuplicationLogDir").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            deDupeMaxLogsPerFile = currXPath.compile("./deDuplicationMaxLogsPerFile").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            deDupeMaxLogsBeforeWrite = currXPath.compile("./deDuplicationMaxLogsBeforeWrite").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            deDupeMaxLogFiles = currXPath.compile("./deDuplicationMaxLogFiles").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            stopAfterStr = currXPath.compile("./stopAfter").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            readLogFileCountStr = currXPath.compile("./readLogFileCount").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            readMaxDeDupeEntriesStr = currXPath.compile("./readMaxDeDupeEntries").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./continue").evaluate(currEl);
            continueState = Boolean.parseBoolean(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            String tempStr = currXPath.compile("./reOpenLogFile").evaluate(currEl);
            reOpenLogFile = Boolean.parseBoolean(tempStr);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        res = LogCheckConfig.from(null,
                null, // service,
                null, // emailOnError,
                smtpServerStr,
                smtpPortStr,
                smtpPassStr,
                smtpUserStr,
                smtpProtocolStr,
                setName,
                dryRun,
                null, // showVersion,
                null, // printLog,
                null, // tailFromEnd,
                reOpenLogFile, // reOpenLogFile
                null, // storeReOpenLogFile
                currSaveState, // saveState
                continueState, // continueState
                lockFileStr,
                logFileStr,
                null, // storeLogFile
                statusFileStr,
                stateFileStr,
                errorFileStr,
                null, // configFilePath,
                holdingDirStr,
                deDupeDir,
                elasticsearchURLStr,
                null, // elasticsearchIndexName,
                null, // elasticsearchIndexPrefix,
                null, // elasticsearchLogType,
                null, // elasticsearchIndexNameFormat,
                null, // logCutoffDate,
                null, // logCutoffDuration,
                null, // logDeduplicationDuration,
                pollIntervalStr,
                stopAfterStr, // stopAfter
                readLogFileCountStr, // readLogFileCount
                readMaxDeDupeEntriesStr, // readMaxDeDupeEntries
                idBlockSize,
                deDupeMaxLogsBeforeWrite,
                deDupeMaxLogsPerFile,
                deDupeMaxLogFiles,
                leBuilderType,
                leStoreType,
                idBlockHashType);

        return res;
    }
}
