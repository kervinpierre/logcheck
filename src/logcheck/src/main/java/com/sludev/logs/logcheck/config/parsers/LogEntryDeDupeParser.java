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
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by kervin on 2015-11-14.
 */
public final class LogEntryDeDupeParser
{
    private static final Logger log = LogManager.getLogger(LogEntryDeDupeParser.class);

    public static LogEntryDeDupe readConfig(Element rootElem)
            throws LogCheckException
    {
        LogEntryDeDupe res = null;

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        String idStr = null;
        String logHashCodeStr = null;
        String errorCodeStr = null;
        String errorCodeTypeStr = null;
        String errorTextStr = null;
        String timeStampStr = null;

        try
        {
            idStr = currXPath.compile("./id").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            logHashCodeStr = currXPath.compile("./logHashCode").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            errorCodeStr = currXPath.compile("./errorCode").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            errorCodeTypeStr = currXPath.compile("./errorCodeType").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            errorTextStr = currXPath.compile("./errorText").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            timeStampStr = currXPath.compile("./timestamp").evaluate(rootElem);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        res = LogEntryDeDupe.from(idStr,
                logHashCodeStr,
                errorCodeStr,
                errorCodeTypeStr,
                errorTextStr,
                timeStampStr);

        return res;
    }
}
