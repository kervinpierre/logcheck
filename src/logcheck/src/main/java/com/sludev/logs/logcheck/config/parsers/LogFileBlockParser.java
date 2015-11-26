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

import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by kervin on 2015-11-21.
 */
public final class LogFileBlockParser
{
    private static final Logger log = LogManager.getLogger(LogFileBlockParser.class);


    public static LogFileBlock readConfig(Element rootElem)
            throws LogCheckException
    {
        LogFileBlock res = null;
        Element currEl = rootElem;

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        String nameStr = null;
        String startPosStr = null;
        String sizeStr = null;
        String hashTypeStr = null;
        String hashDigestStr = null;

        try
        {
            nameStr = currXPath.compile("./name").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            startPosStr = currXPath.compile("./startPosition").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            sizeStr = currXPath.compile("./size").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            hashTypeStr = currXPath.compile("./hashType").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            hashDigestStr = currXPath.compile("./hashDigest").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        res = LogFileBlock.from(nameStr,
                startPosStr,
                sizeStr,
                hashTypeStr,
                hashDigestStr);

        return res;
    }
}
