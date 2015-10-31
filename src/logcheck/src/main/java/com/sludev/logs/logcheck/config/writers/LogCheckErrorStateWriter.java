package com.sludev.logs.logcheck.config.writers;

import com.sludev.logs.logcheck.config.entities.LogCheckError;
import com.sludev.logs.logcheck.enums.LCLogLevel;
import com.sludev.logs.logcheck.utils.LogCheckException;
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
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Created by kervin on 2015-10-30.
 */
public final class LogCheckErrorStateWriter
{
    private static final Logger log = LogManager.getLogger(LogCheckErrorStateWriter.class);


    public static void write( List<LogCheckError> errs, Path file ) throws LogCheckException
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
        Element rootElement = doc.createElement("errors");
        doc.appendChild(rootElement);

        for( LogCheckError e : errs )
        {
            Element currElem = LogCheckErrorStateWriter.toElement(doc, e);
            rootElement.appendChild(currElem);
        }

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
        StreamResult result = new StreamResult(file.toFile());

        try
        {
            transformer.transform(source, result);
        }
        catch( TransformerException | NullPointerException ex )
        {
            String errMsg = "Error writing '%s'";

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

    }


    public static Element toElement( Document doc, LogCheckError lce ) throws LogCheckException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement("error");

        // Log State ID
        UUID currId = lce.getLogStateID();
        if( currId == null )
        {
            throw new LogCheckException("Missing Log State Id");
        }
        currElem = doc.createElement("logStateId");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // Error ID
        currId = lce.getErrorID();
        if( currId == null )
        {
            throw new LogCheckException("Missing Error Id");
        }
        currElem = doc.createElement("errorId");
        currElem.appendChild(doc.createTextNode( currId.toString() ));
        res.appendChild(currElem);

        // log file path
        Path currPath = lce.getLogFilePath();
        if( currPath == null )
        {
            ; //throw new LogCheckException("Missing Archive Path");
        }
        else
        {
            currElem = doc.createElement("logFilePath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // Disposition
        LCLogLevel currDisp = lce.getDisposition();
        if( currDisp == null )
        {
            throw new LogCheckException("Missing Disposition");
        }
        currElem = doc.createElement("disposition");
        currElem.appendChild(doc.createTextNode( currDisp.toString() ));
        res.appendChild(currElem);

        // Error Code
        Long currInt = lce.getErrorCode();
        if( currInt == null )
        {
            ;//throw new LogCheckException("Missing Error Code");
        }
        else
        {
            currElem = doc.createElement("errorCode");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // Error Code Type
        String currType = lce.getErrorCodeType();
        if( currType == null )
        {
            ; // throw new LogCheckException("Missing Error Code Type");
        }
        else
        {
            currElem = doc.createElement("errorCodeType");
            currElem.appendChild(doc.createTextNode(currType.toString()));
            res.appendChild(currElem);
        }

        // Text
        String currStr = lce.getText();
        if( currStr == null )
        {
            throw new LogCheckException("Missing Text");
        }
        currElem = doc.createElement("text");
        currElem.appendChild(doc.createCDATASection( currStr ));
        res.appendChild(currElem);

        // Summary
        currStr = lce.getSummary();
        if( currStr == null )
        {
            ; //throw new LogCheckException("Missing Summary");
        }
        else
        {
            currElem = doc.createElement("summary");
            currElem.appendChild(doc.createCDATASection(currStr));
            res.appendChild(currElem);
        }

        // timestamp
        Instant currInst = lce.getTimestamp();
        if( currInst == null )
        {
            ; //throw new LogCheckException("Missing Timestamp");
        }
        else
        {
            currElem = doc.createElement("timestamp");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // Exception
        Exception currEx = lce.getException();
        if( currEx == null )
        {
            ; //throw new LogCheckException("Missing Exception");
        }
        else
        {
            currElem = doc.createElement("exception");
            currElem.appendChild(doc.createCDATASection(currEx.toString()));
            res.appendChild(currElem);
        }

        return res;
    }
}
