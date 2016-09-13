/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck;

import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public class LogCheckProperties
{
    private static final Logger log = LogManager.getLogger(LogCheckProperties.class);
            
    public static Properties GetProperties()
    {
        Properties testProperties = new Properties();
        try
        {
            testProperties.load(LogCheckProperties.class
                            .getClassLoader()
                            .getResourceAsStream("test001.properties"));
        }
        catch (IOException ex)
        {
            log.error("Error loading properties file", ex);
        }
        
        return testProperties;
    }
}
