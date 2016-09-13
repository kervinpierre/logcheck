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

import com.sludev.logs.logcheck.config.entities.LogCheckDeDupeLog;
import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
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
 * Created by kervin on 2015-11-14.
 */
public final class LogCheckDeDupeLogParser
{
    private static final Logger log = LogManager.getLogger(LogCheckDeDupeLogParser.class);

    public static LogCheckDeDupeLog readConfig(Document doc) throws LogCheckException
    {
        LogCheckDeDupeLog res;

        Element currEl;
        Element tempEl;
        NodeList currElList;

        currEl = doc.getDocumentElement();

        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();

        String idStr = null;
        String startTimeStr = null;
        String endTimeStr = null;
        List<LogEntryDeDupe> currDupeLog = new ArrayList<>();

        try
        {
            idStr = currXPath.compile("./id").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            startTimeStr = currXPath.compile("./startTime").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            endTimeStr = currXPath.compile("./endTime").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            // Get the list of jobs
            currElList = (NodeList)currXPath.compile("./logs/entry").evaluate(
                    currEl, XPathConstants.NODESET);

            if( currElList != null && currElList.getLength() > 0)
            {
                for(int i=0; i<currElList.getLength(); i++)
                {
                    tempEl = (Element)currElList.item(i);
                    LogEntryDeDupe tempJS = LogEntryDeDupeParser.readConfig(tempEl);
                    currDupeLog.add(tempJS);
                }
            }
        }
        catch (XPathExpressionException ex)
        {
            String errMsg = "Failed processing jobs in Job Set State File.";

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        res = LogCheckDeDupeLog.from(idStr,
                startTimeStr,
                endTimeStr,
                currDupeLog);

        return res;
    }
}
