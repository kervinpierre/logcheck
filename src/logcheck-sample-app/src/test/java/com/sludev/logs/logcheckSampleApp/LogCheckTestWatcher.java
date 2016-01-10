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
package com.sludev.logs.logcheckSampleApp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 *
 * @author kervin
 */
public class LogCheckTestWatcher extends TestWatcher
{
    private static final Logger LOGGER
                                = LogManager.getLogger(LogCheckTestWatcher.class);
    
    @Override
    protected void failed(Throwable e, Description description) 
    {
        LOGGER.info(
                String.format("%s failed %s", 
                              description.getDisplayName(), e.getMessage()));

        super.failed(e, description);
    }

    @Override
    protected void succeeded(Description description) 
    {
        LOGGER.info(
                String.format("%s succeeded.", 
                              description.getDisplayName()));

        super.succeeded(description);
    }
}
