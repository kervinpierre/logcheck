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
package com.sludev.logs.logcheck.config;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.config.parsers.LogCheckConfigParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.exceptions.LogCheckException;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import com.sludev.logs.logcheck.utils.FSSLog4JConfiguration;
import org.junit.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestWatcher;

/**
 *
 * @author kervin
 */
public class LogCheckConfigFileTest
{
    private static final Logger log 
                   = LogManager.getLogger(LogCheckConfigFileTest.class);

    private Properties testProperties;

    @Rule
    public TestWatcher testWatcher = new LogCheckTestWatcher();
    
    @Before
    public void setUp() 
    {
        
        /**
         * Get the current test properties from a file so we don't hard-code
         * in our source code.
         */
        testProperties = LogCheckProperties.GetProperties();
    }
    
    public LogCheckConfigFileTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of read method, of class LogCheckConfigParser.
     * @throws LogCheckException
     */
    @Test
    @Ignore
    public void testRead() throws LogCheckException
    {
        FSSLog4JConfiguration.setVerbosity(FSSVerbosityEnum.ALL);

        log.info("read");

        String confPathString = testProperties.getProperty("logcheck.test0001.conffile");

        Map<Integer, LogCheckConfig> config = LogCheckConfigParser.readConfig(
                ParserUtil.readConfig(Paths.get(confPathString),
                        LCFileFormat.LCCONFIG));

        Assert.assertNotNull(config);
        Assert.assertTrue(config.size() == 1);
        Assert.assertTrue(config.containsKey(0));
    }
    
}
