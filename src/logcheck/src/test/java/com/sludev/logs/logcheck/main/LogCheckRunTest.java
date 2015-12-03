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
import com.sludev.logs.logcheck.config.entities.LogCheckState;
import com.sludev.logs.logcheck.config.parsers.LogCheckStateParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.LCFileFormats;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.utils.FSSArgFile;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppInitialize;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppMainRun;
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public void A002_testCallGenericWithLogs60s() throws Exception
    {
        Path testDir = Paths.get("/tmp/A002_testCallGenericWithLogs60s");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        Path stateFile = testDir.resolve("current-state.xml");

        String[] args;
        List<String> argsList = new ArrayList<>();

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");

        argsList.add(String.format("--output-file %s", logFile));
        argsList.add("--delete-logs");
        argsList.add("--output-frequency 25ms");
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

        Path storeLogFile = testDir.resolve("store-log.txt");

        Path dedupeDir = testDir.resolve("dedupe");
        Files.createDirectory(dedupeDir);

        argsList.add("--stop-after=1M");
        argsList.add(String.format("--log-file %s", logFile));
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add(String.format("--store-log-file %s", storeLogFile ));
        argsList.add("--save-state");
        argsList.add(String.format("--state-file %s", stateFile));
        argsList.add("--set-name=\"test app\"");
        argsList.add(String.format("--dedupe-dir-path %s", dedupeDir));
        argsList.add("--dedupe-max-before-write=5");
        argsList.add("--dedupe-log-per-file 10");
        argsList.add("--dedupe-max-log-files=5");
        argsList.add("--file-from-start");
        argsList.add("--read-reopen-log-file");
        argsList.add("--poll-interval=1");

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

        long logFileCount = Files.lines(logFile).count();
        long storeFileCount = Files.lines(storeLogFile).count();

        log.debug(String.format("\nLog File Count : %d\nStore File Count: %d\n",
                logFileCount, storeFileCount));

        Assert.assertTrue(logFileCount == 1024);
        Assert.assertTrue(storeFileCount == 1024);

        LogCheckState currState
                = LogCheckStateParser.readConfig(
                        ParserUtil.readConfig(stateFile,
                                        LCFileFormats.LCSTATE));

        long logSize = Files.size(logFile);
        long currPos = currState.getLogFile().getLastProcessedPosition();

        Assert.assertTrue(logSize==currPos);

        int lineI = 1;
        Pattern p = Pattern.compile("^.*?:\\s+\\[(\\d+)\\]\\s+.*$");
        String[] lines = Files.lines(storeLogFile).toArray(String[]::new);
        for( String line : lines )
        {
            int lineNo = 0;

            Matcher pm = p.matcher(line);
            if( pm.matches() )
            {
                lineNo = Integer.parseInt(pm.group(1));
            }
            else
            {
                Assert.fail("Log Line did not match regex check '%s'");
            }

            Assert.assertTrue(lineNo == lineI++ );
        }

        ByteBuffer bb = ByteBuffer.allocate(1000);
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            logFC.read(bb);
        }

        MessageDigest md =MessageDigest.getInstance("SHA-256");

        bb.flip();
        md.update(bb);

        byte[] currDigest = md.digest();
        byte[] firstDigest = currState.getLogFile().getFirstBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, firstDigest);

        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            logFC.position(logFC.size()-1000);
            logFC.read(bb);
        }

        bb.flip();
        md.update(bb);

        currDigest = md.digest();
        byte[] lastDigest = currState.getLogFile().getLastProcessedBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, lastDigest);
    }
}