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
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.tail.FileTailer;
import com.sludev.logs.logcheck.utils.FSSArgFile;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheck.utils.LogCheckTestFileUtils;
import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppInitialize;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppMainRun;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

/**
 * Created by kervin on 2015-11-28.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCheckRunTest
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckRunTest.class);

    private Properties m_testProperties;

    @Rule
    public TestWatcher m_testWatcher = new LogCheckTestWatcher();

    @Before
    public void setUp()
    {

        /**
         * Get the current test properties from a file so we don't hard-code
         * in our source code.
         */
        m_testProperties = LogCheckProperties.GetProperties();
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
     * Simple call with some of the safer parameters.
     *
     * After a brief pause, and after the tailer starts, a thread inserts a
     * constant number of rows of logs into the log file.  Very little synchronization
     * is thus needed
     *
     * Runs then stops after 20s.  No "--continue"
     *
     */
    @Test
    public void A001_constantLogsAfterPauseAndRun20s()
                        throws IOException, LogCheckException, InterruptedException, ExecutionException
    {
        Path testDir = Paths.get("/tmp/A001_constantLogsAfterPauseAndRun20s");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        FileTailer.DEBUG_LCAPP_LOG_SEQUENCE = 0;

        String[] args;
        List<String> argsList = new ArrayList<>(20);

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");
        Path storeLogFile = testDir.resolve("store-log.txt");
        Path stateFile = testDir.resolve("current-state.xml");
        Path dedupeDir = testDir.resolve("dedupe");

        Files.createDirectory(dedupeDir);

        // Stop the test after 20 seconds
        argsList.add("--stop-after=20");

        // Re-open the log file after pause.
        // Use the default poll interval
        argsList.add("--read-reopen-log-file");

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
        argsList.add("--tailer-validate-log-file");
        argsList.add("--debug-flags=LOG_SOURCE_LC_APP");
        argsList.add("--verbosity=all");

        args = FSSArgFile.getArgArray(argsList);

        LogCheckConfig config = LogCheckInitialize.initialize(args);

        LogCheckRun currRun = new LogCheckRun(config);

        String tempStr =
                "2015-12-03T16:30:09.562Z :  [1015][15] Random Line : 446a7379-5283-4483-af7d-a2f3c239caaa\n" +
                "2015-12-03T16:30:09.586Z :  [1016][16] Random Line : a1d5412c-ee36-4560-89e7-792c775a9c71\n" +
                "2015-12-03T16:30:09.612Z :  [1017][17] Random Line : ce097710-feeb-4643-ad1c-af86e2ac3492\n" +
                "2015-12-03T16:30:09.637Z :  [1018][18] Random Line : af835242-9d83-41f7-96c1-b7ac99c328d0\n" +
                "2015-12-03T16:30:09.662Z :  [1019][19] Random Line : c52cbcc7-db8c-4766-9b78-ea53d6d08d61\n" +
                "2015-12-03T16:30:09.687Z :  [1020][20] Random Line : 6d392b86-483a-41db-acbd-6b5d74d90b8e\n" +
                "2015-12-03T16:30:09.711Z :  [1021][21] Random Line : 8d7ff1f7-9cbd-4f02-9b73-6ac56a36afee\n" +
                "2015-12-03T16:30:09.736Z :  [1022][22] Random Line : e7c03d4a-3269-463f-ade8-b4f01b8629f3\n" +
                "2015-12-03T16:30:09.762Z :  [1023][23] Random Line : 520bc16b-74b6-418c-a591-d2754ed70124\n" +
                "2015-12-03T16:30:09.787Z :  [1024][24] Random Line : d2c025d6-8f54-4865-b788-239968f3c0d9\n";

        ByteBuffer bb = ByteBuffer.wrap(tempStr.getBytes());

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCRunThread-%d")
                .build();
        ExecutorService lcThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<LogCheckResult> lcFuture = lcThreadExe.submit(currRun);
        lcThreadExe.shutdown();

        LogCheckResult lcResult;

        try
        {
            // Sleep 7 seconds, then supply some logs
            Thread.sleep(7000);
            try( FileChannel logFC = FileChannel.open(logFile, StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE) )
            {
                // Read the first block from file
                logFC.write(bb);
            }

            // Wait for the logging to be completed
            lcResult = lcFuture.get();
        }
        finally
        {
            ;
        }

        Assert.assertNotNull(lcResult);
        Assert.assertTrue(lcResult.getStatus() == LCResultStatus.SUCCESS);

        long storeFileCount = Files.lines(storeLogFile).count();
        Assert.assertTrue(storeFileCount==10);
    }

    /**
     * Separate Logging and Tailing threads running concurrently.
     *
     * No log rotation.  Relies on the log sample application to generate the logs.
     *
     * The Tailer runs then stops after 60s.
     *
     * After running some validation is done on the stored logs and also on the serialized state.
     *
     */
    @Test
    public void A002_separateThreadsRun60s()
                        throws IOException, LogCheckException, InterruptedException,
                                ExecutionException, NoSuchAlgorithmException
    {
       // Path testDir = Paths.get("/tmp/",
       //         Thread.currentThread().getStackTrace()[1].getMethodName());

        Path testDir = Paths.get("/tmp/A002_separateThreadsRun60s");
        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        Path stateFile = testDir.resolve("current-state.xml");

        FileTailer.DEBUG_LCAPP_LOG_SEQUENCE = 0;

        String[] args;
        List<String> argsList = new ArrayList<>(20);

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");

        argsList.add(String.format("--output-file %s", logFile));
        argsList.add("--delete-logs");
        argsList.add("--output-frequency 25ms");
        argsList.add("--stop-after-count 1K");

        args = FSSArgFile.getArgArray(argsList);
        LogCheckAppConfig appConfig = LogCheckAppInitialize.initialize(args);;

        LogCheckAppMainRun currAppRun = new LogCheckAppMainRun(appConfig);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("lc-run-test-%d")
                .build();
        ExecutorService appThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<LCSAResult> lcAppFuture = appThreadExe.submit(currAppRun);
        appThreadExe.shutdown();

        argsList.clear();

        Path storeLogFile = testDir.resolve("store-log.txt");

        Path dedupeDir = testDir.resolve("dedupe");
        Files.createDirectory(dedupeDir);

        argsList.add("--stop-after=30S");
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
        argsList.add("--tailer-validate-log-file");
        argsList.add("--debug-flags=LOG_SOURCE_LC_APP");
        argsList.add("--verbosity=all");
        // argsList.add("--random-wait-max=1");

        args = FSSArgFile.getArgArray(argsList);
        LogCheckConfig config = LogCheckInitialize.initialize(args);
        LogCheckRun currRun = new LogCheckRun(config);

        thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCRunThread-%d")
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

        LOGGER.debug(String.format("\nLog File Count : %d\nStore File Count: %d\n",
                logFileCount, storeFileCount));

        Assert.assertTrue(logFileCount == 1024);
        Assert.assertTrue(storeFileCount == 1024);

        LogCheckState currState
                = LogCheckStateParser.readConfig(
                        ParserUtil.readConfig(stateFile,
                                        LCFileFormat.LCSTATE));

        long logSize = Files.size(logFile);
        long currPos = currState.getLogFile().getLastProcessedPosition();

        Assert.assertTrue(logSize==currPos);

        LogCheckTestFileUtils.checkAllLinesInFile(storeLogFile,
                Pattern.compile(".*?:\\s+\\[(\\d+)\\].*"));

        ByteBuffer currByteBuffer = ByteBuffer.allocate(1000);
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            // Read the first block from file
            logFC.read(currByteBuffer);
        }

        MessageDigest md =MessageDigest.getInstance("SHA-256");

        currByteBuffer.flip();
        md.update(currByteBuffer);

        byte[] currDigest = md.digest();
        byte[] firstDigest = currState.getLogFile().getFirstBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, firstDigest);

        currByteBuffer.clear();
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            // Read the last block from file.
            logFC.position(logFC.size()-1000);
            logFC.read(currByteBuffer);
        }

        currByteBuffer.flip();
        md.update(currByteBuffer);

        currDigest = md.digest();
        byte[] lastDigest = currState.getLogFile().getLastProcessedBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, lastDigest);
    }


    /**
     * Similar to the A002 test.  Except we also rotate the logs to test how the tailer
     * handles log rotation detection.
     *
     */
    @Test
    public void A003_logRotateThenTail() throws IOException, LogCheckException,
            InterruptedException, ExecutionException, NoSuchAlgorithmException
    {
        Path testDir = Paths.get("/tmp/A003_logRotateThenTail");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        Path stateFile = testDir.resolve("current-state.xml");
        Path stateFile2 = testDir.resolve("current-state2.xml");
        Path stdOutFile = testDir.resolve("std-out.txt");

        FileTailer.DEBUG_LCAPP_LOG_SEQUENCE = 0;

        String[] args;
        List<String> argsList = new ArrayList<>(20);

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");

        argsList.add(String.format("--output-file %s", logFile));
        argsList.add("--delete-logs");
        argsList.add("--stop-after-count 1K");

        //Output the logs to the screen as they are generated
        // argsList.add("--output-to-screen");

        // The following should cause a log rotate per second
        // This should cause an issue with the tailer also having
        // a poll interval of a second.
        //
        // ...and the handling of that issue is what this test is
        // about.
        argsList.add("--output-frequency 10ms");
        argsList.add("--rotate-after-count 40");

        args = FSSArgFile.getArgArray(argsList);
        LogCheckAppConfig appConfig = LogCheckAppInitialize.initialize(args);;

        LogCheckAppMainRun currAppRun = new LogCheckAppMainRun(appConfig);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCAppRunThread-%d")
                .build();
        ExecutorService appThreadExe = Executors.newSingleThreadExecutor(thFactory);

        argsList.clear();

        Path storeLogFile = testDir.resolve("store-log.txt");

        Path dedupeDir = testDir.resolve("dedupe");
        Files.createDirectory(dedupeDir);

        argsList.add("--stop-after=30s");
        argsList.add(String.format("--log-file %s", logFile));
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add(String.format("--store-log-file %s", storeLogFile ));
        argsList.add("--save-state");
        argsList.add(String.format("--state-file %s", stateFile));
        argsList.add(String.format("--processed-logs-state-file %s", stateFile2));
        argsList.add("--set-name=\"test app\"");
        argsList.add(String.format("--dedupe-dir-path %s", dedupeDir));
        argsList.add("--dedupe-max-before-write=5");
        argsList.add("--dedupe-log-per-file 10");
        argsList.add("--dedupe-max-log-files=5");
        argsList.add("--file-from-start");
        argsList.add("--read-reopen-log-file");
        argsList.add("--poll-interval=1");
        argsList.add("--tailer-validate-log-file");
        argsList.add("--tailer-read-backup-log");
        argsList.add("--tailer-read-prior-backup-log");
        argsList.add("--tailer-backup-log-file-name-regex=(.*?)\\\\.(\\\\d+)\\\\.bak");
        argsList.add("--tailer-backup-log-file-name-component=FILENAME_PREFIX");
        argsList.add("--tailer-backup-log-file-name-component=INTEGER_INC");
        argsList.add("--debug-flags=LOG_SOURCE_LC_APP");
        argsList.add("--verbosity=all");
        argsList.add(String.format("--tailer-backup-log-dir %s", testDir));
        argsList.add(String.format("--stdout-file %s", stdOutFile));

        args = FSSArgFile.getArgArray(argsList);
        LogCheckConfig config = LogCheckInitialize.initialize(args);

        // Start the sample app
        Future<LCSAResult> lcAppFuture = appThreadExe.submit(currAppRun);
        appThreadExe.shutdown();

        // Wait for the logger to finish before parsing.
        // This gives us a chance to test log backup processing
        LCSAResult appRes = lcAppFuture.get();
        Assert.assertTrue(appRes==LCSAResult.SUCCESS);

        // Now the tailer
        LogCheckRun currRun = new LogCheckRun(config);

        thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCRunThread-%d")
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

        LOGGER.debug(String.format("\nLog File Count : %d\nStore File Count: %d\n",
                logFileCount, storeFileCount));

        // Total lines is 1024, but across 26 files, last containing 24 lines
        Assert.assertTrue(logFileCount == 24);

        Assert.assertTrue(storeFileCount == 1024);

        LogCheckState currState
                = LogCheckStateParser.readConfig(
                ParserUtil.readConfig(stateFile,
                        LCFileFormat.LCSTATE));

        long logSize = Files.size(logFile);
        long currPos = currState.getLogFile().getLastProcessedPosition();

        Assert.assertTrue(logSize==currPos);

        LogCheckTestFileUtils.checkAllLinesInFile(storeLogFile,
                                    Pattern.compile(".*?:\\s+\\[(\\d+)\\].*"));

        ByteBuffer currByteBuffer = ByteBuffer.allocate(1000);
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            // Read the first block from file
            logFC.read(currByteBuffer);
        }

        MessageDigest md =MessageDigest.getInstance("SHA-256");

        currByteBuffer.flip();
        md.update(currByteBuffer);

        byte[] currDigest = md.digest();
        byte[] firstDigest = currState.getLogFile().getFirstBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, firstDigest);

        currByteBuffer.clear();
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            // Read the last block from file.
            logFC.position(logFC.size()-1000);
            logFC.read(currByteBuffer);
        }

        currByteBuffer.flip();
        md.update(currByteBuffer);

        currDigest = md.digest();
        byte[] lastDigest = currState.getLogFile().getLastProcessedBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, lastDigest);
    }

    /**
     * Similar to the A003 test.  Except the log rotation is now done in parallel.
     *
     * FIXME : Ignore until MVP2 completed.
     *
     */
    @Test
    @Ignore
    public void A004_logRotateAndTailParallel() throws IOException, LogCheckException,
            InterruptedException, ExecutionException, NoSuchAlgorithmException
    {
        Path testDir = Paths.get("/tmp/A004_logRotateAndTailParallel");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        Path stateFile = testDir.resolve("current-state.xml");

        FileTailer.DEBUG_LCAPP_LOG_SEQUENCE = 0;

        String[] args;
        List<String> argsList = new ArrayList<>(20);

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");

        argsList.add(String.format("--output-file %s", logFile));
        argsList.add("--delete-logs");
        argsList.add("--stop-after-count 1K");

        //Output the logs to the screen as they are generated
        // argsList.add("--output-to-screen");

        // The following should cause a log rotate per second
        // This should cause an issue with the tailer also having
        // a poll interval of a second.
        //
        // ...and the handling of that issue is what this test is
        // about.
        argsList.add("--output-frequency 25ms");
        argsList.add("--rotate-after-count 40");

        args = FSSArgFile.getArgArray(argsList);
        LogCheckAppConfig appConfig = LogCheckAppInitialize.initialize(args);;

        LogCheckAppMainRun currAppRun = new LogCheckAppMainRun(appConfig);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCAppRunThread-%d")
                .build();
        ExecutorService appThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<LCSAResult> lcAppFuture = appThreadExe.submit(currAppRun);
        appThreadExe.shutdown();

        argsList.clear();

        Path storeLogFile = testDir.resolve("store-log.txt");

        Path dedupeDir = testDir.resolve("dedupe");
        Files.createDirectory(dedupeDir);

        // Top tailing afer 1 minute
        argsList.add("--stop-after=1M");

        // Read the backup file from the
        argsList.add("--file-from-start");

        argsList.add(String.format("--log-file %s", logFile));
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add(String.format("--store-log-file %s", storeLogFile ));
        argsList.add("--save-state");
        argsList.add(String.format("--state-file %s", stateFile));
        argsList.add("--set-name=\"test app\"");
        argsList.add("--debug-flags=LOG_SOURCE_LC_APP");

        // Deduplication configuration
        argsList.add(String.format("--dedupe-dir-path %s", dedupeDir));
        argsList.add("--dedupe-max-before-write=5");
        argsList.add("--dedupe-log-per-file 10");
        argsList.add("--dedupe-max-log-files=5");

        argsList.add("--read-reopen-log-file");
        argsList.add("--tailer-read-prior-backup-log");

        // Aggressive poll interval to increase chances of
        // Race-conditions
        argsList.add("--poll-interval=1");

        // Tailer log configuration/parameters
        argsList.add("--tailer-validate-log-file");
        argsList.add("--tailer-read-backup-log");
        argsList.add("--tailer-backup-log-file-name-regex=(.*?)\\\\.(\\\\d+)\\\\.bak");
        argsList.add("--tailer-backup-log-file-name-component=FILENAME_PREFIX");
        argsList.add("--tailer-backup-log-file-name-component=INTEGER_INC");
        argsList.add(String.format("--tailer-backup-log-dir %s", testDir));

        args = FSSArgFile.getArgArray(argsList);
        LogCheckConfig config = LogCheckInitialize.initialize(args);
        LogCheckRun currRun = new LogCheckRun(config);

        thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCRunThread-%d")
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

        LOGGER.debug(String.format("\nLog File Count : %d\nStore File Count: %d\n",
                logFileCount, storeFileCount));

        Assert.assertTrue(logFileCount == 24);
        Assert.assertTrue(storeFileCount == 1024);

        LogCheckState currState
                = LogCheckStateParser.readConfig(
                ParserUtil.readConfig(stateFile,
                        LCFileFormat.LCSTATE));

        long logSize = Files.size(logFile);
        long currPos = currState.getLogFile().getLastProcessedPosition();

        Assert.assertTrue(logSize==currPos);

        LogCheckTestFileUtils.checkAllLinesInFile(storeLogFile,
                Pattern.compile(".*?:\\s+\\[(\\d+)\\].*"));

        ByteBuffer currByteBuffer = ByteBuffer.allocate(1000);
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            // Read the first block from file
            logFC.read(currByteBuffer);
        }

        MessageDigest md =MessageDigest.getInstance("SHA-256");

        currByteBuffer.flip();
        md.update(currByteBuffer);

        byte[] currDigest = md.digest();
        byte[] firstDigest = currState.getLogFile().getFirstBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, firstDigest);

        currByteBuffer.clear();
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            // Read the last block from file.
            logFC.position(logFC.size()-1000);
            logFC.read(currByteBuffer);
        }

        currByteBuffer.flip();
        md.update(currByteBuffer);

        currDigest = md.digest();
        byte[] lastDigest = currState.getLogFile().getLastProcessedBlock().getHashDigest();

        Assert.assertArrayEquals(currDigest, lastDigest);
    }
}