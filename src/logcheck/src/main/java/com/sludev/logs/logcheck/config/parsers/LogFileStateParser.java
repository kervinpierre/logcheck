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

import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by kervin on 2015-11-21.
 */
public final class LogFileStateParser
{
    private static final Logger log = LogManager.getLogger(LogFileStateParser.class);

    public static LogFileState readConfig(Element rootElem)
            throws LogCheckException
    {
        LogFileState res = null;
        Element currEl = rootElem;

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        String filePathStr = null;
        String lplineNumStr = null;
        String lpCharNumStr = null;
        String lpBytePosStr = null;
        String lpStartStr = null;
        String lpEndStr = null;
        LogFileBlock lastProcessedBlock = null;
        LogFileBlock firstBlock = null;

        try
        {
            filePathStr = currXPath.compile("./filePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Configuration parsing error. <filePath> tag.";

            log.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            lplineNumStr = currXPath.compile("./lastProcessedLineNumber").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Configuration parsing error. <lastProcessedLineNumber> tag.";

            log.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            lpCharNumStr = currXPath.compile("./lastProcessedCharNumber").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Configuration parsing error. <lastProcessedCharNumber> tag.";

            log.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            lpBytePosStr = currXPath.compile("./lastProcessedBytePosition").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Configuration parsing error. <lastProcessedBytePosition> tag.";

            log.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            lpStartStr = currXPath.compile("./lastProcessedStart").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Configuration parsing error. <lastProcessedStart> tag.";

            log.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            lpEndStr = currXPath.compile("./lastProcessedEnd").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            Element currNode = (Element)currXPath.compile("./lastProcessedBlock").evaluate(
                    currEl, XPathConstants.NODE);

           lastProcessedBlock = LogFileBlockParser.readConfig(currNode);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error retrieving <lastProcessedBlock>");
        }

        try
        {
            Element currNode = (Element)currXPath.compile("./firstBlock").evaluate(
                    currEl, XPathConstants.NODE);

            firstBlock = LogFileBlockParser.readConfig(currNode);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        res = LogFileState.from(filePathStr,
                lpStartStr,
                lpEndStr,
                lpBytePosStr,
                lplineNumStr,
                lpCharNumStr,
                lastProcessedBlock,
                firstBlock);

        return res;
    }
}
