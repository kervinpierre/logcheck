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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ls.LSInput;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
class LogCheckLSInput implements LSInput
{
    private static final Logger log 
                          = LogManager.getLogger(LogCheckLSInput.class);
    
    private String publicId;
    private String systemId;
    private BufferedInputStream inputStream;

    public LogCheckLSInput(String publicId, String sysId, InputStream input)
    {      
        this.publicId = publicId;
        this.systemId = sysId;
        this.inputStream = new BufferedInputStream(input);
    }
    
    @Override
    public String getPublicId()
    {
        return publicId;
    }

    @Override
    public void setPublicId(String publicId)
    {
        this.publicId = publicId;
    }

    @Override
    public String getBaseURI()
    {
        return null;
    }

    @Override
    public InputStream getByteStream()
    {
        return null;
    }

    @Override
    public boolean getCertifiedText()
    {
        return false;
    }

    @Override
    public Reader getCharacterStream()
    {
        return null;
    }

    @Override
    public String getEncoding()
    {
        return null;
    }

    @Override
    public String getStringData()
    {
        byte[] input = null;
        String contents;
        
        synchronized (inputStream)
        {
            try
            {
                input = new byte[inputStream.available()];
                inputStream.read(input);
            }
            catch (IOException ex)
            {
                log.error("getStringData() error", ex);
                return null;
            }
        }
        
        contents = new String(input);
        
        return contents;
    }

    @Override
    public void setBaseURI(String baseURI)
    {
    }

    @Override
    public void setByteStream(InputStream byteStream)
    {
    }

    @Override
    public void setCertifiedText(boolean certifiedText)
    {
    }

    @Override
    public void setCharacterStream(Reader characterStream)
    {
    }

    @Override
    public void setEncoding(String encoding)
    {
    }

    @Override
    public void setStringData(String stringData)
    {
    }

    @Override
    public String getSystemId()
    {
        return systemId;
    }

    @Override
    public void setSystemId(String systemId)
    {
        this.systemId = systemId;
    }

    public BufferedInputStream getInputStream()
    {
        return inputStream;
    }

    public void setInputStream(BufferedInputStream inputStream)
    {
        this.inputStream = inputStream;
    }

}
