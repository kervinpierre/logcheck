/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.config.parsers;

import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.entities.LogFileStatus;
import com.sludev.logs.logcheck.enums.LCResultStatus;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 *
 * Created by kervin on 2015-11-21.
 */
public final class LogCheckStateParser
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckStateParser.class);

    public static LogCheckState readConfig(Document doc) throws LogCheckException
    {
        LogCheckState res;

        Element currEl;

        currEl = doc.getDocumentElement();

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        String saveDateStr = null;
        String setNameStr = null;
        String idStr = null;
        LogFileState logFile = null;
        Deque<LogFileStatus> statuses = null;

        try
        {
            setNameStr = currXPath.compile("./setName").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            saveDateStr = currXPath.compile("./saveDate").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            idStr = currXPath.compile("./id").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            Element tempEl = (Element)currXPath.compile("./logFile").evaluate(
                    currEl, XPathConstants.NODE);

            if( (tempEl != null) && tempEl.hasChildNodes() )
            {
                logFile = LogFileStateParser.readConfig(tempEl);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        try
        {
            NodeList currElList = (NodeList)currXPath.compile("./fileStatuses/fileStatus").evaluate(
                    currEl, XPathConstants.NODESET);

            if( (currElList != null) && (currElList.getLength() > 0) )
            {
                statuses = new ArrayDeque<>();
                for(int i=0; i<currElList.getLength(); i++)
                {
                    Element tempEl = (Element)currElList.item(i);

                    LogFileStatus currStat = readConfigFileStatus(tempEl);
                    if( currStat != null )
                    {
                        statuses.add(currStat);
                    }
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error <fileStatuses>.", ex);
        }

        res = LogCheckState.from(logFile,
                saveDateStr,
                idStr,
                setNameStr,
                null,
                statuses);

        return res;
    }

    public static LogFileStatus readConfigFileStatus(Element rootElem)
            throws LogCheckException
    {
        LogFileStatus res = null;
        Element currEl = rootElem;

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        Path filePath = null;
        Instant processedStamp = null;
        Boolean processed = null;
        LogFileBlock fullFileBlock = null;

        try
        {
            String filePathStr = currXPath.compile("./path").evaluate(currEl);
            filePath = Paths.get(filePathStr);
        }
        catch( XPathExpressionException ex )
        {
            String errMsg = "Configuration parsing error. <path> tag.";

            LOGGER.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            String processedStampStr = currXPath.compile("./processedStamp").evaluate(currEl);
            if( StringUtils.isNoneBlank(processedStampStr) )
            {
                processedStamp = Instant.parse(processedStampStr);
            }
        }
        catch( XPathExpressionException ex )
        {
            String errMsg = "Configuration parsing error. <processedStamp> tag.";

            LOGGER.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            String processedStr = StringUtils.trim(currXPath.compile("./processed").evaluate(currEl));

            if( (StringUtils.equalsIgnoreCase(processedStr, "true") == false)
                    || (StringUtils.equalsIgnoreCase(processedStr, "false") == false) )
            {
                LOGGER.warn("Tag <processed> does not have a true/false value");
            }

            processed = Boolean.parseBoolean(processedStr);
        }
        catch( XPathExpressionException ex )
        {
            String errMsg = "Configuration parsing error. <processed> tag value '%s'.";

            LOGGER.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            Element currNode = (Element)currXPath.compile("./fullFileBlock").evaluate(
                    currEl, XPathConstants.NODE);

            if( (currNode != null) && currNode.hasChildNodes() )
            {
                fullFileBlock = LogFileBlockParser.readConfig(currNode);
            }
        }
        catch (XPathExpressionException ex)
        {
            LOGGER.debug("configuration parsing error.", ex);
        }

        res = LogFileStatus.from(filePath, fullFileBlock, processedStamp, processed);

        return res;
    }
}
