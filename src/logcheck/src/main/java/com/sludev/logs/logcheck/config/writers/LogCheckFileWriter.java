package com.sludev.logs.logcheck.config.writers;

import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by kervin on 2015-10-30.
 */
public final class LogCheckFileWriter
{
    private static final Logger log = LogManager.getLogger(LogCheckFileWriter.class);

    public static Element toElement(Document doc, String elementName, LogFileState lcf) throws LogCheckException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement(elementName);

        // path
        Path currPath = lcf.getFile();
        if( currPath == null )
        {
            throw new LogCheckException("Missing Log File Path");
        }
        currElem = doc.createElement("filePath");
        currElem.appendChild(doc.createTextNode( currPath.toString() ));
        res.appendChild(currElem);

        // start
        Instant currInst = lcf.getLastProcessedTimeStart();
        if( currInst == null )
        {
            throw new LogCheckException("Missing Start");
        }
        currElem = doc.createElement("lastProcessedStart");
        currElem.appendChild(doc.createTextNode( currInst.toString() ));
        res.appendChild(currElem);

        // end
        currInst = lcf.getLastProcessedTimeEnd();
        if( currInst == null )
        {
            log.debug("Missing <end /> tag."); // throw new LogCheckException("Missing End");
        }
        else
        {
            currElem = doc.createElement("lastProcessedEnd");
            currElem.appendChild(doc.createTextNode(currInst.toString()));
            res.appendChild(currElem);
        }

        // line num
        Long currLong = lcf.getLastProcessedLineNumber();
        if( currLong == null )
        {
            throw new LogCheckException("Missing file line number");
        }
        currElem = doc.createElement("lastProcessedLineNumber");
        currElem.appendChild(doc.createTextNode( currLong.toString() ));
        res.appendChild(currElem);

        // char num
        currLong = lcf.getLastProcessedCharNumber();
        if( currLong == null )
        {
            throw new LogCheckException("Missing file line number");
        }
        currElem = doc.createElement("lastProcessedCharNumber");
        currElem.appendChild(doc.createTextNode( currLong.toString() ));
        res.appendChild(currElem);

        // position
        currLong = lcf.getLastProcessedPosition();
        if( currLong == null )
        {
            throw new LogCheckException("Missing file position");
        }
        currElem = doc.createElement("lastProcessedBytePosition");
        currElem.appendChild(doc.createTextNode( currLong.toString() ));
        res.appendChild(currElem);

        LogFileBlock currBlock = lcf.getFirstBlock();
        if( currBlock == null )
        {
            throw new LogCheckException("Missing first block");
        }
        currElem = LogFileBlockWriter.toElement(doc, "firstBlock", currBlock);
        if( currElem != null )
        {
            res.appendChild(currElem);
        }

        currBlock = lcf.getLastProcessedBlock();
        if( currBlock == null )
        {
            throw new LogCheckException("Missing last processed block");
        }
        currElem = LogFileBlockWriter.toElement(doc, "lastProcessedBlock", currBlock);
        if( currElem != null )
        {
            res.appendChild(currElem);
        }

        return res;
    }
}
