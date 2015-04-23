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
package com.sludev.logs.logcheck.store;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.enums.LogCheckLogLevel;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import java.time.LocalDateTime;
import java.util.Properties;
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
public class LogEntryElasticSearchTest
{
    private static final Logger log 
                   = LogManager.getLogger(LogEntryElasticSearchTest.class);

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
    
    public LogEntryElasticSearchTest()
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
     * Test of put method, of class LogEntryElasticSearch.
     */
    @Test
    @Ignore
    public void testPut()
    {
        log.debug("testing put()");
        
        String elasticsearchURL = testProperties.getProperty("logcheck.test0001.elasticsearchurl");
        
        LogEntry le = new LogEntry();
        le.setHost("test-host");
        le.setTimeStamp(LocalDateTime.now());
        le.setLevel(LogCheckLogLevel.DEBUG);
        le.setMessage(String.format("Message Message\nMessage Message Message\nMessage Message"));
        le.setException(String.format("Exception Exception\nException Exception Exception\nException Exception"));
        le.setLogger("com.example.test");
        
        LogEntryElasticSearch instance = new LogEntryElasticSearch();
        instance.setElasticsearchURL(elasticsearchURL);
        instance.init();
        
        LogCheckResult result = instance.put(le);
    }
    
}
