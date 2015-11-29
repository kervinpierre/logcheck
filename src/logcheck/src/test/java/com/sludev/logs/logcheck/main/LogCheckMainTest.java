/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 * 
 *  NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.main;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCheckMainTest
{
    private static final Logger log 
                   = LogManager.getLogger(LogCheckMainTest.class);

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
    
    public LogCheckMainTest()
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
     * Test of main method, of class LogCheckMain.
     */
    @Test
    @Ignore
    public void testMain()
    {
        System.out.println("main");
        String[] args = null;
        LogCheckMain.main(args);
    }

    /**
     * Test of commonStart method, of class LogCheckMain.
     */
    @Test
    public void testCommonStartVersion01()
    {
        log.info("commonStartVersion01");
        
        String[] args = {"--version"};
        
        LogCheckMain.commonStart(args);
    }

    /**
     * Test of destroy method, of class LogCheckMain.
     */
    @Test
    @Ignore
    public void testDestroy()
    {
        System.out.println("destroy");
        LogCheckMain instance = new LogCheckMain();
        instance.destroy();
    }

    /**
     * Test of init method, of class LogCheckMain.
     */
    @Test
    @Ignore
    public void testInit()
    {
        System.out.println("init");
        String[] args = null;
        LogCheckMain instance = new LogCheckMain();
        instance.init(args);
    }

    /**
     * Test of start method, of class LogCheckMain.
     */
    @Test
    @Ignore
    public void testStart()
    {
        System.out.println("start");
        LogCheckMain instance = new LogCheckMain();
        instance.start();
    }

    /**
     * Test of stop method, of class LogCheckMain.
     */
    @Test
    @Ignore
    public void testStop()
    {
        System.out.println("stop");
        LogCheckMain instance = new LogCheckMain();
        instance.stop();
    }

    /**
     * Test of windowsStop method, of class LogCheckMain.
     */
    @Test
    @Ignore
    public void testWindowsStop()
    {
        System.out.println("windowsStop");
        String[] args = null;
        LogCheckMain.windowsStop(args);
    }

    /**
     * Test of windowsStart method, of class LogCheckMain.
     */
    @Test
    @Ignore
    public void testWindowsStart()
    {
        System.out.println("windowsStart");
        String[] args = null;
        LogCheckMain.windowsStart(args);
    }
    
}
