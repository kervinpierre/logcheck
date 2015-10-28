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
package com.sludev.logs.logcheck.utils;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.store.LogEntryElasticSearchTest;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestWatcher;

/**
 *
 * @author kervin
 */
public class LogCheckTailTest
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
    
    public LogCheckTailTest()
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
     * Test of call method, of class LogCheckTail.
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    @Ignore
    public void testTailLog01() throws InterruptedException, ExecutionException, LogCheckException
    {
        log.debug("testing call()");
        
        String logFilePath = testProperties.getProperty("logcheck.test0001.logfile");
        
        //LogCheckConfigParser config = new LogCheckConfigParser(service, emailOnError, smtpServer, smtpPort, smtpPass, smtpUser, smtpProto, dryRun, showVersion, lockFilePath, logPath, statusFilePath, configFilePath, holdingFolderPath, elasticsearchURL, elasticsearchIndexName, logCutoffDate, logCutoffDuration, logDeduplicationDuration);
//        ILogEntrySink logEntrySink = new LogEntryQueueSink();
//
//        //config.setLogPath(logFilePath);
//
//        BlockingDeque<LogEntry> currQ = new LinkedBlockingDeque<>();
//        logEntrySink.setCompletedLogEntries(currQ);
//
//        MultiLineDelimitedBuilder currLogEntryBuilder = new MultiLineDelimitedBuilder(logRowStartPattern, logRowEndPattern, logColumnStartPattern, logColumnEndPattern, completionCallback);
//        currLogEntryBuilder.setCompletionCallback(logEntrySink);
//
//        LogCheckTail lct = new LogCheckTail(mainLogEntryBuilder);
//        //lct.setLogFile(config.getLogPath());
//        lct.setTailFromEnd(false);
//        lct.setMainLogEntryBuilder(currLogEntryBuilder);
//        lct.setStopAfter(60);
        
//        FutureTask<LogCheckResult> logFileTailTask = new FutureTask<>(lct);
//
//        BasicThreadFactory fileTailFactory = new BasicThreadFactory.Builder()
//            .namingPattern("logpollthread-%d")
//            .build();
//
//        ExecutorService fileTailExe = Executors.newSingleThreadExecutor(fileTailFactory);
//        Future fileTailExeRes = fileTailExe.submit(logFileTailTask);
//        fileTailExe.shutdown();
//
//        LogCheckResult fileTailRes;
//        fileTailRes = logFileTailTask.get();
//
//        assertTrue( currQ.size() > 0 );
    }

}
