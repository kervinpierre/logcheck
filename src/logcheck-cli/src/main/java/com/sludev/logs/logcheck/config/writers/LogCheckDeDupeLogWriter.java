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
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by kervin on 2015-11-14.
 */
public final class LogCheckDeDupeLogWriter
{
    private static final Logger log = LogManager.getLogger(LogCheckDeDupeLogWriter.class);

    public static void write(LogCheckDeDupeLog js, Path stateFile) throws LogCheckException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try
        {
            docBuilder = docFactory.newDocumentBuilder();
        }
        catch( ParserConfigurationException ex )
        {
            String errMsg = "Error creating document builder.";

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        Document doc = docBuilder.newDocument();

        Element currElem = LogCheckDeDupeLogWriter.toElement(doc, "logCheckDeDupe", js);
        doc.appendChild(currElem);

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try
        {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
        catch( TransformerConfigurationException ex )
        {
            String errMsg = "Error creating transformer";

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(stateFile.toFile());

        try
        {
            transformer.transform(source, result);
        }
        catch( TransformerException | NullPointerException ex )
        {
            String errMsg = String.format("Error writing '%s'", stateFile);

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }
    }

    public static Path write(LogCheckDeDupeLog js) throws LogCheckException
    {
        Path res = null;

        try
        {
            res = Files.createTempFile("logFileDeDupeLog", ".xml.tmp");
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error creating temp file LogCheckDeDupeLog");

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        write(js, res);

        return res;
    }

    public static Element toElement( Document doc, String elementName, LogCheckDeDupeLog lcs ) throws LogCheckException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement(elementName);

        // ID
        UUID currId = lcs.getId();
        if( currId == null )
        {
            throw new LogCheckException("Missing Id");
        }
        currElem = doc.createElement("id");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // start
        Instant currInst = lcs.getStartTime();
        if( currInst == null )
        {
            ; // throw new LogCheckException("Missing set name");
        }
        else
        {
            currElem = doc.createElement("startTime");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // end
        currInst = lcs.getEndTime();
        if( currInst == null )
        {
            ; // throw new LogCheckException("Missing set name");
        }
        else
        {
            currElem = doc.createElement("endTime");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        Element logs = doc.createElement("logs");

        for( LogEntryDeDupe currLog : lcs.getLogEntryDeDupes() )
        {
            currElem = LogEntryDeDupeWriter.toElement(doc, "entry", currLog);
            logs.appendChild(currElem);
        }

        res.appendChild(logs);

        return res;
    }
}
