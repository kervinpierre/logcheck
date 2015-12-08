/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.utils;

import java.io.IOException;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public class FSSConfigurationFile
{
    private static final Logger log 
                          = LogManager.getLogger(FSSConfigurationFile.class);

    public Document read(Path path) throws LogCheckException
    {
        log.info( String.format( "Configuration FilePath : %1$s\n", path ) );

        Document res = null;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder currDocBuilder;
        
        try
        {
            currDocBuilder = dbFactory.newDocumentBuilder();
            try
            {
                res = currDocBuilder.parse( path.toFile() );
            }
            catch (SAXException ex)
            {
                String errMsg = String.format("Error parsing XML Configuration file syntax.");
                
                log.error(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
            catch (IOException ex)
            {
                String errMsg = String.format("Error reading the XML Configuration file on disk.");
                
                log.error(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
        catch (ParserConfigurationException ex)
        {
            String errMsg = String.format("Error parsing XML Configuration file");
                
                log.error(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
        }
 
        res.getDocumentElement().normalize();

        return res;
    }    
}
