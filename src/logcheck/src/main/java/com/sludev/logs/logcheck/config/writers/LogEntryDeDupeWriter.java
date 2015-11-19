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

package com.sludev.logs.logcheck.config.writers;

import com.sludev.logs.logcheck.config.entities.LogCheckDeDupeLog;
import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by kervin on 2015-11-14.
 */
public final class LogEntryDeDupeWriter
{
    private static final Logger log = LogManager.getLogger(LogEntryDeDupeWriter.class);


    public static Element toElement(Document doc, String elementName, LogEntryDeDupe lcs ) throws LogCheckException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement(elementName);

        // ID
        UUID currId = lcs.getId();
        if( currId == null )
        {
            ; // throw new LogCheckException("Missing Id");
        }
        else
        {
            currElem = doc.createElement("id");
            currElem.appendChild(doc.createTextNode(currId.toString()));
            res.appendChild(currElem);
        }

        // HashCode
        String currStr = lcs.getLogHashCode();
        if( StringUtils.isBlank(currStr) )
        {
            throw new LogCheckException("Missing Log Hash Code");
        }
        else
        {
            currElem = doc.createElement("logHashCode");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // Error code
        currStr = lcs.getErrorCode();
        if( StringUtils.isBlank(currStr) )
        {
            ; // throw new LogCheckException("Missing Id");
        }
        else
        {
            currElem = doc.createElement("errorCode");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // Error code type
        currStr = lcs.getErrorCodeType();
        if( StringUtils.isBlank(currStr) )
        {
            ; // throw new LogCheckException("Missing Id");
        }
        else
        {
            currElem = doc.createElement("errorCodeType");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // Error text
        currStr = lcs.getErrorText();
        if( StringUtils.isBlank(currStr) )
        {
            ; // throw new LogCheckException("Missing Id");
        }
        else
        {
            currElem = doc.createElement("errorText");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // Error code
        Instant currInst = lcs.getTimeStamp();
        if( currInst == null  )
        {
            ; // throw new LogCheckException("Missing Id");
        }
        else
        {
            currElem = doc.createElement("timestamp");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        return res;
    }
}
