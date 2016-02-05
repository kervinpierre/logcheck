package com.sludev.logs.logcheck.config.writers;

import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.enums.LCFileBlockType;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by kervin on 2015-10-30.
 */
public final class LogFileBlockWriter
{
    private static final Logger LOGGER = LogManager.getLogger(LogFileBlockWriter.class);

    public static Element toElement( Document doc, String elementName, LogFileBlock lfb ) throws LogCheckException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement(elementName);

        // name
        String currStr = lfb.getName();
        if( StringUtils.isNoneBlank(currStr) )
        {
            currElem = doc.createElement("name");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // start position
        Long currLong = lfb.getStartPosition();
        if( currLong == null )
        {
            throw new LogCheckException("Missing start position");
        }
        currElem = doc.createElement("startPosition");
        currElem.appendChild(doc.createTextNode(currLong.toString()));
        res.appendChild(currElem);

        // size
        Integer currInt = lfb.getSize();
        if( currInt == null )
        {
            throw new LogCheckException("Missing size");
        }
        currElem = doc.createElement("size");
        currElem.appendChild(doc.createTextNode(currInt.toString()));
        res.appendChild(currElem);

        // hash type
        LCHashType currHashType = lfb.getHashType();
        if( currHashType == null )
        {
            throw new LogCheckException("Missing hash type");
        }
        currElem = doc.createElement("hashType");
        currElem.appendChild(doc.createTextNode(currHashType.toString().toLowerCase()));
        res.appendChild(currElem);

        // hash
        byte[] currHashData = lfb.getHashDigest();
        if( currHashData == null )
        {
            throw new LogCheckException("Missing start position");
        }
        currElem = doc.createElement("hashDigest");
        currElem.appendChild(
                doc.createTextNode(Hex.encodeHexString(currHashData)));
        res.appendChild(currElem);

        // type
        LCFileBlockType currBlockType = lfb.getType();
        if( currBlockType == null )
        {
            LOGGER.debug("Missing block type"); // ; throw new LogCheckException("Missing block type");
        }
        else
        {
            currElem = doc.createElement("type");
            currElem.appendChild(doc.createTextNode(currBlockType.toString().toLowerCase()));
            res.appendChild(currElem);
        }

        // Sample
        currStr = lfb.getSample();
        if( StringUtils.isNoneBlank(currStr) )
        {
            currElem = doc.createElement("sample");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        return res;
    }

}
