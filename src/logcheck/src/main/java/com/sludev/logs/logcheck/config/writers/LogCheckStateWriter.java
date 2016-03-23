package com.sludev.logs.logcheck.config.writers;

import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.entities.LogFileStatus;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Created by kervin on 2015-10-30.
 */
public final class LogCheckStateWriter
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckStateWriter.class);


    public static void write( LogCheckState js, Path stateFile, Path errFile ) throws LogCheckException
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

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        Document doc = docBuilder.newDocument();

        Element currElem = LogCheckStateWriter.toElement(doc, "logCheckState", js);
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

            LOGGER.debug(errMsg, ex);
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
            String errMsg = "Error writing '%s'";

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        if( LOGGER.isDebugEnabled() )
        {
            try
            {
                LOGGER.debug(String.format("write() just wrote :\n%s\n",
                                    new String(Files.readAllBytes(stateFile))));
            }
            catch( IOException ex )
            {
                LOGGER.debug("Error dumping State File", ex);
            }
        }

        if( (js.getErrors() != null) && (js.getErrors().size() > 0) )
        {
            // We have errors, so we need to write them to an errors file

            LogCheckErrorStateWriter.write(js.getErrors(), errFile);
        }
    }

    public static Pair<Path, Path> write(LogCheckState js) throws LogCheckException
    {
        Path resStateFile = null;
        Path resErrFile = null;

        try
        {
            resStateFile = Files.createTempFile("LogCheckState", ".xml.tmp");
            resErrFile = Files.createTempFile("LogCheckErrorState", ".xml.tmp");
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error creating temp file JobStateWriter");

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        write(js, resStateFile, resErrFile);

        return Pair.of(resStateFile, resErrFile);
    }

    public static Element toElement( Document doc, String elementName, LogCheckState lcs ) throws LogCheckException
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

        // set name
        String currStr = lcs.getSetName();
        if( currStr == null )
        {
            throw new LogCheckException("Missing set name");
        }
        currElem = doc.createElement("setName");
        currElem.appendChild(doc.createTextNode( currStr ));
        res.appendChild(currElem);

        // save date
        Instant currInst = lcs.getSaveDate();
        if( currInst == null )
        {
            throw new LogCheckException("Missing save date");
        }
        currElem = doc.createElement("saveDate");
        currElem.appendChild(doc.createTextNode( currInst.toString() ));
        res.appendChild(currElem);

        LogFileState currLog = lcs.getLogFile();
        if( currLog != null )
        {
            currElem = LogCheckFileWriter.toElement(doc, "logFile", currLog);
            res.appendChild(currElem);
        }

        // completed log files
        Deque<LogFileStatus> currStatuses = lcs.getCompletedLogFiles();
        if( currStatuses != null )
        {
            Element statuses = doc.createElement("fileStatuses");
            for( LogFileStatus status : currStatuses )
            {
                currElem = toElement(doc, "fileStatus", status);
                statuses.appendChild(currElem);
            }
            res.appendChild(statuses);
        }

        return res;
    }

    public static Element toElement( Document doc, String elementName, LogFileStatus lfs ) throws LogCheckException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement(elementName);

        // processed timestamp
        Instant currInst = lfs.getProcessedStamp();
        if( currInst == null )
        {
            ; // throw new LogCheckException("Missing processed time stamp");
        }
        else
        {
            currElem = doc.createElement("processedStamp");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // processed flag
        Boolean currBool = lfs.isProcessed();
        if( currBool == null )
        {
            ; // throw new LogCheckException("Missing processed flag");
        }
        else
        {
            currElem = doc.createElement("processed");
            currElem.appendChild(doc.createTextNode(currBool.toString()));
            res.appendChild(currElem);
        }

        // path
        Path currPath = lfs.getPath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing Path");
        }
        else
        {
            currElem = doc.createElement("path");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // file block
        LogFileBlock currBlock = lfs.getFullFileBlock();
        if( currBlock == null )
        {
            ; //LOGGER.info("Missing first block. <fullFileBlock/>"); // throw new LogCheckException("Missing full block");
        }
        else
        {
            currElem = LogFileBlockWriter.toElement(doc, "firstBlock", currBlock);
        }
        if( currElem != null )
        {
            res.appendChild(currElem);
        }

        return res;
    }
}
