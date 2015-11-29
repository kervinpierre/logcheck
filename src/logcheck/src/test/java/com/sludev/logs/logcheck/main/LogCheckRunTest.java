/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.main;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.utils.FSSArgFile;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppInitialize;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppMainRun;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.junit.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by kervin on 2015-11-28.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCheckRunTest
{
    private static final Logger log
            = LogManager.getLogger(LogCheckRunTest.class);

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

    @AfterClass
    public static void tearDownClass()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Generic call with some of the safer parameters.
     *
     * Runs then stops after 20s.
     *
     * @throws Exception
     */
    @Test
    public void A001_testCallGeneric20s() throws Exception
    {
        String[] args;
        List<String> argsList = new ArrayList<>();

        argsList.add("--stop-after=20");
        argsList.add("--log-file /tmp/logcheck-sample-app-output.txt");
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add("--store-log-file /tmp/store-log.txt");
        argsList.add("--save-state");
        argsList.add("--state-file /tmp/current-state.xml");
        argsList.add("--set-name=\"test app\"");
        argsList.add("--dedupe-dir-path /tmp/dedupe");
        argsList.add("--dedupe-max-before-write=5");
        argsList.add("--dedupe-log-per-file 10");
        argsList.add("--dedupe-max-log-files=5");
        argsList.add("--continue");
        argsList.add("--read-reopen-log-file");

        args = FSSArgFile.getArgArray(argsList);

        LogCheckConfig config = LogCheckInitialize.initialize(args);

        LogCheckRun currRun = new LogCheckRun(config);

        LogCheckResult res = currRun.call();

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatus() == LCResultStatus.SUCCESS);
    }

    /**
     * Generic call with some of the safer parameters.
     *
     * Runs then stops after 30s.  Relies on the log sample application.
     *
     * @throws Exception
     */
    @Test
    public void A002_testCallGenericWithLogs30s() throws Exception
    {
        String stateFile = "/tmp/current-state.xml";

        String[] args;
        List<String> argsList = new ArrayList<>();

        argsList.add("--output-file /tmp/logcheck-sample-app-output.txt");
        argsList.add("--delete-logs");
        argsList.add("--output-frequency 50ms");
        argsList.add("--stop-after-count 1K");

        args = FSSArgFile.getArgArray(argsList);
        LogCheckAppConfig appConfig = LogCheckAppInitialize.initialize(args);;

        LogCheckAppMainRun currAppRun = new LogCheckAppMainRun(appConfig);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("app-run-thread-%d")
                .build();
        ExecutorService appThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<LCSAResult> lcAppFuture = appThreadExe.submit(currAppRun);
        appThreadExe.shutdown();

        argsList.clear();

        argsList.add("--stop-after=30");
        argsList.add("--log-file /tmp/logcheck-sample-app-output.txt");
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add("--store-log-file /tmp/store-log.txt");
        argsList.add("--save-state");
        argsList.add("--start-position-ignore-error");
        argsList.add(String.format("--state-file %s", stateFile));
        argsList.add("--set-name=\"test app\"");
        argsList.add("--dedupe-dir-path /tmp/dedupe");
        argsList.add("--dedupe-max-before-write=5");
        argsList.add("--dedupe-log-per-file 10");
        argsList.add("--dedupe-max-log-files=5");
        argsList.add("--continue");
        argsList.add("--read-reopen-log-file");

        args = FSSArgFile.getArgArray(argsList);
        LogCheckConfig config = LogCheckInitialize.initialize(args);
        LogCheckRun currRun = new LogCheckRun(config);

        thFactory = new BasicThreadFactory.Builder()
                .namingPattern("logcheck-run-thread-%d")
                .build();
        ExecutorService lcThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<LogCheckResult> lcFuture = lcThreadExe.submit(currRun);
        lcThreadExe.shutdown();

        LogCheckResult lcResult;

        try
        {
            // Wait for the logging to be completed
            lcResult = lcFuture.get();
        }
        finally
        {
            // If Tailer thread is done, then cancel/interrupt the store thread
            // E.g. useful for implementing the --stop-after feature
            if( lcAppFuture.isDone() == false )
            {
                lcAppFuture.cancel(true);
            }
        }

       Assert.assertNotNull(lcResult);
       Assert.assertTrue(lcResult.getStatus() == LCResultStatus.SUCCESS);
    }
}