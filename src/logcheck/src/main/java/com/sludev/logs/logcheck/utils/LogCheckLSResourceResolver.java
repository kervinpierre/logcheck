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

import java.io.InputStream;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public class LogCheckLSResourceResolver implements LSResourceResolver
{

    /**
     *
     * @param type
     * @param namespaceURI
     * @param publicId
     * @param systemId
     * @param baseURI
     * @return
     */
    @Override
    public LSInput resolveResource(String type, String namespaceURI,
        String publicId, String systemId, String baseURI) 
    {
        // note: in this sample, the XSD's are expected to be in the root of the classpath
        InputStream resourceAsStream = this.getClass().getClassLoader()
               .getResourceAsStream(systemId);
        return new LogCheckLSInput(publicId, systemId, resourceAsStream);
    }
}
