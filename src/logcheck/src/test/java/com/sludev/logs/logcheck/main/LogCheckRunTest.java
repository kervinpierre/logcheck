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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sludev.logs.elasticsearchApp.elasticsearch.EADelete;
import com.sludev.logs.elasticsearchApp.elasticsearch.EAScroll;
import com.sludev.logs.elasticsearchApp.utils.ESAException;
import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.config.entities.impl.LogCheckState;
import com.sludev.logs.logcheck.config.entities.LogEntryVO;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.parsers.LogCheckStateParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.tail.impl.FileTailer;
import com.sludev.logs.logcheck.utils.FSSArgFile;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import com.sludev.logs.logcheck.utils.LogCheckTestFileUtils;
import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppInitialize;
import com.sludev.logs.logcheckSampleApp.main.LogCheckAppMainRun;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by kervin on 2015-11-28.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCheckRunTest
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckRunTest.class);

    private Properties m_testProperties;

    private Path topTestDir = null;
    private Path stdOutFile = null;

    @Rule
    public TestWatcher m_testWatcher = new LogCheckTestWatcher();

    @Before
    public void setUp()
            throws IOException
    {
        if( topTestDir != null )
        {
            return;
        }

        /**
         * Get the current test properties from a file so we don't hard-code
         * in our source code.
         */
        m_testProperties = LogCheckProperties.GetProperties();

        topTestDir = Files.createTempDirectory("logcheck-unit-tests");
        LOGGER.debug(String.format("Created Top Test Directory '%s'", topTestDir));

        try( Stream<Path> paths = Files.list(topTestDir.getParent()) )
        {
            List<Path> dirs = paths.filter(p -> p.getFileName().startsWith("logcheck-unit-tests")
                    && p.equals(topTestDir) == false)
                    .collect(Collectors.toList());

            for( Path p : dirs )
            {
                try
                {
                    FileUtils.deleteDirectory(p.toFile());
                }
                catch( Exception ex )
                {
                    ;
                }
            }
        }

        stdOutFile = topTestDir.resolve("stdOut.txt");
        Files.write(stdOutFile, new byte[0],
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE );
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
        Path testDir = topTestDir.resolve("A001_constantLogsAfterPauseAndRun20s");

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

        argsList.add(String.format("--log-file %s",
                logFile.toString().replace("\\", "\\\\")));
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add(String.format("--store-log-file %s",
                storeLogFile.toString().replace("\\", "\\\\")));
        argsList.add("--save-state");
        argsList.add(String.format("--state-file %s",
                stateFile.toString().replace("\\", "\\\\")));
        argsList.add("--set-name=\"test app\"");
        argsList.add(String.format("--dedupe-dir-path %s",
                dedupeDir.toString().replace("\\", "\\\\")));
        argsList.add("--dedupe-max-before-write=5");
        argsList.add("--dedupe-log-per-file 10");
        argsList.add("--dedupe-max-log-files=5");
        argsList.add("--tailer-validate-log-file");
        argsList.add("--debug-flags=LOG_SOURCE_LC_APP");
        argsList.add("--verbosity=all");

        args = FSSArgFile.getArgArray(argsList);

        LinkedHashMap<Integer, LogCheckConfig> config = LogCheckInitialize.initialize(args);

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
        Future<Map<Integer, LogCheckResult>> lcFuture = lcThreadExe.submit(currRun);
        lcThreadExe.shutdown();

        Map<Integer, LogCheckResult> lcResult;

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
        Assert.assertTrue(lcResult.get(0).getStatuses().contains(LCResultStatus.SUCCESS));

        try(Stream<String> strm = Files.lines(logFile))
        {
            long storeFileCount = strm.count();
            Assert.assertTrue(storeFileCount==10);
        }
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

        Path testDir = topTestDir.resolve("A002_separateThreadsRun60s");
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

        argsList.add(String.format("--output-file %s",
                logFile.toString().replace("\\", "\\\\")));
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
        argsList.add(String.format("--log-file %s",
                logFile.toString().replace("\\", "\\\\")));
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add(String.format("--store-log-file %s",
                storeLogFile.toString().replace("\\", "\\\\") ));
        argsList.add("--save-state");
        argsList.add(String.format("--state-file %s",
                stateFile.toString().replace("\\", "\\\\")));
        argsList.add("--set-name=\"test app\"");
        argsList.add(String.format("--dedupe-dir-path %s",
                dedupeDir.toString().replace("\\", "\\\\")));
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
        LinkedHashMap<Integer, LogCheckConfig> config = LogCheckInitialize.initialize(args);
        LogCheckRun currRun = new LogCheckRun(config);

        thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCRunThread-%d")
                .build();
        ExecutorService lcThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<Map<Integer, LogCheckResult>> lcFuture = lcThreadExe.submit(currRun);
        lcThreadExe.shutdown();

        Map<Integer, LogCheckResult> lcResult;

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
        Assert.assertTrue(lcResult.get(0).getStatuses().contains(LCResultStatus.SUCCESS));

        long storeFileCount;
        try(Stream<String> strm = Files.lines(storeLogFile))
        {
            storeFileCount = strm.count();
        }

        long logFileCount;
        try(Stream<String> strm = Files.lines(logFile))
        {
            logFileCount = strm.count();
        }

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
        Path testDir = topTestDir.resolve("A003_logRotateThenTail");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        FileTailer.DEBUG_LCAPP_LOG_SEQUENCE = 0;
        internal_logRotateThenTail(testDir, 0, 24, 1024, false);
    }

    /**
     * Internal method for testing rotating logs then tailing.
     *
     * This simple SEQUENTIAL rotate THEN tail helps test the simple case of an application
     * being run for a long time then having LogCheck process all the past logs and come up
     * to speed.
     *
     * LogCheck is then responsible for...
     *   1. Detecting unprocessed backup files are present.  Currently this is done using a STATE file.
     *   2. Processing backup files
     *   3. Continue with tailing the log file
     *
     * If we ran this method twice against the same directory/data.  LogCheck should then
     *   1. Detect the unprocessed backup files SINCE the last run
     *   2. Process all unprocessed backup files
     *   3. Continue with tailing the log file
     *
     * Overall objectives include
     *   1. No logs should be skipped
     *   2. No logs should be processed twice
     *   3. No logs should be processed out-of-order
     *   4. Logs should be processed completely ( identical to the log source's value )
     *
     * @param testDir The directory where all test data is stored
     * @param lineCountStart The line number of the first log file.  Useful when continuing with previous data '--continue'
     * @param logFileCount The number of log lines remaining in the final log file after all rotation is completed
     * @param storeFileCount The total number of log lines in the store file '--store-log-file'
     * @throws IOException
     * @throws LogCheckException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NoSuchAlgorithmException
     */
    private void internal_logRotateThenTail(final Path testDir,
                                            final int lineCountStart,
                                            final long logFileCount,
                                            final long storeFileCount,
                                            final boolean ignoreStartPosErr )
            throws IOException, LogCheckException, InterruptedException,
                    ExecutionException, NoSuchAlgorithmException
    {
        Path stateFile = testDir.resolve("current-state.xml");
        Path stateFile2 = testDir.resolve("current-state2.xml");
        Path stdOutFile = testDir.resolve("std-out.txt");

        String[] args;
        List<String> argsList = new ArrayList<>(20);

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");

        argsList.add(String.format("--output-file %s",
                logFile.toString().replace("\\", "\\\\")));
        // argsList.add("--delete-logs");
        argsList.add("--stop-after-count 1K");
        argsList.add("--confirm-deletes");

        argsList.add(String.format("--start-line-number %d", lineCountStart));

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

        if( Files.notExists(dedupeDir) )
        {
            Files.createDirectory(dedupeDir);
        }

        argsList.add("--stop-after=1m");
        argsList.add("--continue");
        argsList.add(String.format("--log-file %s",
                logFile.toString().replace("\\", "\\\\")));
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add(String.format("--store-log-file %s",
                storeLogFile.toString().replace("\\", "\\\\") ));
        argsList.add("--save-state");
        argsList.add(String.format("--state-file %s",
                stateFile.toString().replace("\\", "\\\\")));
        argsList.add(String.format("--processed-logs-state-file %s",
                stateFile2.toString().replace("\\", "\\\\")));
        argsList.add("--set-name=\"test app\"");
        argsList.add(String.format("--dedupe-dir-path %s",
                dedupeDir.toString().replace("\\", "\\\\")));
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
        argsList.add(String.format("--tailer-backup-log-dir %s",
                testDir.toString().replace("\\", "\\\\")));
        argsList.add(String.format("--stdout-file %s",
                stdOutFile.toString().replace("\\", "\\\\")));

        if( ignoreStartPosErr )
        {
            argsList.add("--start-position-ignore-error");
        }

        args = FSSArgFile.getArgArray(argsList);
        LinkedHashMap<Integer, LogCheckConfig> config = LogCheckInitialize.initialize(args);

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
        Future<Map<Integer, LogCheckResult>> lcFuture = lcThreadExe.submit(currRun);
        lcThreadExe.shutdown();

        Map<Integer, LogCheckResult> lcResult;

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
        Assert.assertTrue(lcResult.get(0).getStatuses().contains(LCResultStatus.SUCCESS));

        long currStoreFileCount;
        try(Stream<String> strm = Files.lines(storeLogFile))
        {
            currStoreFileCount = strm.count();
        }

        long currLogFileCount;
        try(Stream<String> strm = Files.lines(logFile))
        {
            currLogFileCount = strm.count();
        }

        LOGGER.debug(String.format("\nLog File Count : %d\nStore File Count: %d\n",
                currLogFileCount, currStoreFileCount));

        // Total lines is 1024, but across 26 files, last containing 24 lines
        Assert.assertTrue(currLogFileCount == logFileCount);

        Assert.assertTrue(currStoreFileCount == storeFileCount);

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

        LogFileBlock currTestBlock = null;

        currByteBuffer.clear();
        try( FileChannel logFC = FileChannel.open(logFile) )
        {
            if( logFC.size() < 1000 )
            {
                logFC.position(0);
                currTestBlock = currState.getLogFile().getFirstBlock();
            }
            else
            {
                // Read the last block from file.
                logFC.position(logFC.size() - 1000);
                currTestBlock = currState.getLogFile().getLastProcessedBlock();
            }

            logFC.read(currByteBuffer);
        }

        currByteBuffer.flip();
        md.update(currByteBuffer);

        currDigest = md.digest();
        byte[] lastDigest = currTestBlock.getHashDigest();

        Assert.assertArrayEquals(currDigest, lastDigest);
    }

    /**
     * Similar to the A003 test.  Except the log rotation is now done in parallel.
     *
     * FIXME : Ignore until MVP3 completed.
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
        LogCheckAppConfig appConfig = LogCheckAppInitialize.initialize(args);

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
        LinkedHashMap<Integer, LogCheckConfig> config = LogCheckInitialize.initialize(args);
        LogCheckRun currRun = new LogCheckRun(config);

        thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCRunThread-%d")
                .build();
        ExecutorService lcThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<Map<Integer, LogCheckResult>> lcFuture = lcThreadExe.submit(currRun);
        lcThreadExe.shutdown();

        Map<Integer, LogCheckResult> lcResult;

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
        Assert.assertTrue(lcResult.get(0).getStatuses().contains(LCResultStatus.SUCCESS));

        long currLogFileCount = Files.lines(logFile).count();
        long currStoreFileCount = Files.lines(storeLogFile).count();

        LOGGER.debug(String.format("\nLog File Count : %d\nStore File Count: %d\n",
                currLogFileCount, currStoreFileCount));

        Assert.assertTrue(currLogFileCount == 24);
        Assert.assertTrue(currStoreFileCount == 1024);

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
    public void A005_logRotateThenTailTwice() throws IOException, LogCheckException,
            InterruptedException, ExecutionException, NoSuchAlgorithmException
    {
        Path testDir = topTestDir.resolve("A005_logRotateThenTailTwice");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        // Reset the internal debug counter
        FileTailer.DEBUG_LCAPP_LOG_SEQUENCE = 0;

        // First test to generate the folder and data
        internal_logRotateThenTail(testDir, 0, 24, 1024, false);

        // Second test that should skip all processed
        // data and continue where left off
        internal_logRotateThenTail(testDir, 1024, 8, 2048, true);
    }

    /**
     * Scenario with lots of files.
     *
     * Count on the command line...
     * grep -n --binary-files=text "^[[:upper:]]\{4,6\}" app.log* | wc -l
     *
     * @throws IOException
     * @throws LogCheckException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws NoSuchAlgorithmException
     * @throws ESAException
     */
    @Test
    @Ignore
    public void A006_ACScenario_catchupLogsThenTail()
            throws IOException, LogCheckException, InterruptedException, ExecutionException,
                    NoSuchAlgorithmException, ESAException
    {
        LOGGER.debug("A006_ACScenario_catchupLogsThenTail");

        Path testDir = topTestDir.resolve("A006_ACScenario_catchupLogsThenTail")
                .toAbsolutePath();

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }
        Files.createDirectory(testDir);

        // Copy the data we need for testing
        FileUtils.copyDirectory(new File("test-data"),
                                    testDir.resolve("test-data").toFile());

        internal_ACScenario_catchupLogsThenTail(testDir, "25m", true);
    }

    @Test
    @Ignore
    public void A007_ACScenario_catchupLogsThenTail_twice()
            throws IOException, LogCheckException, InterruptedException, ExecutionException,
            NoSuchAlgorithmException, ESAException
    {
        LOGGER.debug("A007_ACScenario_catchupLogsThenTail_twice started");

        Path testDir = topTestDir.resolve("A007_ACScenario_catchupLogsThenTail_twice")
                .toAbsolutePath();

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }
        Files.createDirectory(testDir);

        // Copy the data we need for testing
        FileUtils.copyDirectory(new File("test-data"),
                testDir.resolve("test-data").toFile());

        internal_ACScenario_catchupLogsThenTail(testDir, "25m", true);

        // Run again without clearing the directory before hand
        // The result should be no extra files processed
        internal_ACScenario_catchupLogsThenTail(testDir, "1m", false);

        LOGGER.debug("A007_ACScenario_catchupLogsThenTail_twice completed");
    }

    private void internal_ACScenario_catchupLogsThenTail(final Path testDir,
                                                         final String stopAfter,
                                                         final boolean clearElasticSearch ) throws IOException, LogCheckException, ExecutionException, InterruptedException, ESAException
    {
        String[] args;
        List<String> argsList = new ArrayList<>(20);

        LOGGER.debug("Starting internal_ACScenario_catchupLogsThenTail");

        Path dataDir = testDir.resolve("test-data/web01-test-20160311-01").normalize();

        argsList.add(String.format("--config-file %s",
                dataDir.resolve("conf/logcheck-service-01.config.xml")
                        .normalize().toString().replace("\\", "\\\\")));
        argsList.add("--service");
        argsList.add(String.format("--stdout-file=%s",
                stdOutFile.toString().replace("\\", "\\\\")));
        argsList.add(String.format("--stop-after=%s", stopAfter));
        argsList.add(String.format("--preferred-dir %s",
                testDir.toString().replace("\\", "\\\\")));
        argsList.add("--create-missing-dirs");

        Path outFile = testDir.resolve("elasticsearch-out.txt");

        args = FSSArgFile.getArgArray(argsList);
        LinkedHashMap<Integer, LogCheckConfig> config = LogCheckInitialize.initialize(args);
        LogCheckRun currRun = new LogCheckRun(config);

        List<String> indexes = new ArrayList<>();
        indexes.add("logstash-*");

        URL eaURL = new URL("http://sludev01:9200");

        if( clearElasticSearch )
        {
            // INFO : Deletes all logstash indexes on the server
            EADelete.doDeleteIndex(eaURL, indexes);
        }

        ThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCRunThread-%d")
                .build();
        ExecutorService lcThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<Map<Integer, LogCheckResult>> lcFuture = lcThreadExe.submit(currRun);
        lcThreadExe.shutdown();

        Map<Integer, LogCheckResult> lcResult;

        try
        {
            // Wait for the logging to be completed
            lcResult = lcFuture.get();
        }
        finally
        {
            ;
        }

        Assert.assertNotNull(lcResult);
        Assert.assertTrue(lcResult.get(0).getStatuses().contains(LCResultStatus.SUCCESS));

        // Now download all data to a file
        EAScroll.doScroll(eaURL, indexes, outFile, false);

        // Parse the outfile
        ObjectMapper jsonMapper = new ObjectMapper();

        JsonNode rootArray = jsonMapper.readTree(outFile.toFile());

        List<LogEntryVO> esLogEntries = new ArrayList<>();

        String lastIndex = null;

        for( JsonNode root : rootArray )
        {
            JsonNode hits = root.get("hits")
                .get("hits");

            for( JsonNode currHit : hits )
            {
                String currIndex = currHit.get("_index").textValue();
                if( StringUtils.equalsIgnoreCase(currIndex, lastIndex) == false )
                {
                    LOGGER.debug(String.format("Index changed to '%s'", currIndex));
                    lastIndex = currIndex;
                }

                String currTimestamp = currHit.get("_source")
                                            .get("@timestamp")
                                            .textValue();
                String currLevel = currHit.get("_source")
                        .get("level")
                        .textValue();
                String currType = currHit.get("_source")
                        .get("type")
                        .textValue();
                String currLogger = currHit.get("_source")
                        .get("logger")
                        .textValue();
                String currHost = currHit.get("_source")
                        .get("host")
                        .textValue();
                String currMessage = currHit.get("_source")
                        .get("message")
                        .textValue();
                String currException = currHit.get("_source")
                        .get("exception")
                        .textValue();

                String currJsonRaw = currHit.get("_source")
                        .get("jsonRaw")
                        .textValue();

                String currAppSource = currHit.get("_source")
                        .get("appSource")
                        .textValue();

                String currAppStatusCode = currHit.get("_source")
                        .get("appStatusCode")
                        .textValue();

                String currAppChannel = currHit.get("_source")
                        .get("appChannel")
                        .textValue();

                String currAppType = currHit.get("_source")
                        .get("exception")
                        .textValue();

                String currAppRecordNumber = currHit.get("_source")
                        .get("appRecordNumber")
                        .textValue();

                String currAppEventId = currHit.get("_source")
                        .get("appEventId")
                        .textValue();

                String currAppComputerName = currHit.get("_source")
                        .get("appComputerName")
                        .textValue();

                String currAppTimeGenerated = currHit.get("_source")
                        .get("appTimeGenerated")
                        .textValue();

                String currAppDataStr = currHit.get("_source")
                        .get("appDataStr")
                        .textValue();

                LogEntryVO currVO
                        = LogEntryVO.from( currLevel,
                                            currLogger,
                                            currMessage,
                                            currException,
                                            currTimestamp,
                                            currType,
                                            currHost,
                                            currJsonRaw,
                                            currAppSource,
                                            currAppStatusCode,
                                            currAppChannel,
                                            currAppType,
                                            currAppRecordNumber,
                                            currAppEventId,
                                            currAppComputerName,
                                            currAppTimeGenerated,
                                            currAppDataStr);
                esLogEntries.add(currVO);
            }
        }

        Assert.assertTrue(esLogEntries.size() > 0);
        LOGGER.debug(String.format("Elastic returned %d log objects", esLogEntries.size()));

        List<LogEntryVO> fsLogEntries = new ArrayList<>();
        List<Path> logPaths = Files.list(dataDir.resolve("logs"))
                                    .collect(Collectors.toList());
        for( Path currLogPath: logPaths )
        {
            LOGGER.debug(String.format("Processing '%s'", currLogPath));

            Pattern linePat = Pattern.compile(LogCheckConstants.DEFAULT_MULTILINE_ROW_START_PATTERN);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(1024);
            int bytesRead = 0;
            int lineCount = 0;
            boolean seenCR = false;

            try( FileChannel rdr = FileChannel.open(currLogPath, StandardOpenOption.READ) )
            {
                while( (bytesRead  = rdr.read(buffer))!= -1 )
                {
                    buffer.flip();

                    for(int i = 0; i<buffer.limit(); i++)
                    {
                        final byte currBuff = buffer.get();
                        boolean doHandle = false;
                        switch (currBuff)
                        {
                            case '\n':
                                seenCR = false; // swallow CR before LF
                                doHandle = true;
                                break;

                            case '\r':
                                if (seenCR)
                                {
                                    lineBuf.write('\r');
                                }
                                seenCR = true;
                                break;

                            default:
                                if (seenCR)
                                {
                                    seenCR = false; // swallow final CR
                                    doHandle = true;
                                }
                                lineBuf.write(currBuff);
                        }

                        if( doHandle )
                        {
                            lineCount++;

                            String line = new String(lineBuf.toByteArray(),
                                    Charset.defaultCharset());

                            Matcher matcher = linePat.matcher(line);
                            if( matcher.matches() == false )
                            {
                                lineBuf.reset();
                                continue;
                            }

                            int currGC = matcher.groupCount();
                            if( currGC > 0 )
                            {
                                // Use the row start pattern for the first columns
                                String currLevel = matcher.group(1);
                                String currTimestamp = matcher.group(2);
                                String currLogger = matcher.group(3);
                                String currHost = matcher.group(4);

                                LogEntryVO currVO = LogEntryVO.from(currLevel, currLogger, null, null,
                                        currTimestamp, null, currHost, null, null, null, null, null, null, null,
                                        null, null, null);
                                fsLogEntries.add(currVO);

                                if( (fsLogEntries.size() > 0) && ((fsLogEntries.size() % 1000) == 0) )
                                {
                                    LOGGER.debug(String.format("FS Log Entries count : %d\nLines seen : %d",
                                            fsLogEntries.size(), lineCount));
                                }
                            }

                            lineBuf.reset();
                        }
                    }

                    buffer.clear();
                }
            }
        }

        Assert.assertTrue(fsLogEntries.size() > 0);
        Assert.assertEquals(esLogEntries.size(), fsLogEntries.size());

//        long currLogFileCount = Files.lines(logFile).count();
//        long currStoreFileCount = Files.lines(storeLogFile).count();
//
//        LOGGER.debug(String.format("\nLog File Count : %d\nStore File Count: %d\n",
//                currLogFileCount, currStoreFileCount));
//
//        Assert.assertTrue(currLogFileCount == 24);
//        Assert.assertTrue(currStoreFileCount == 1024);
//
//        LogCheckState currState
//                = LogCheckStateParser.readConfig(
//                ParserUtil.readConfig(stateFile,
//                        LCFileFormat.LCSTATE));
//
//        long logSize = Files.size(logFile);
//        long currPos = currState.getLogFile().getLastProcessedPosition();
//
//        Assert.assertTrue(logSize==currPos);
//
//        LogCheckTestFileUtils.checkAllLinesInFile(storeLogFile,
//                Pattern.compile(".*?:\\s+\\[(\\d+)\\].*"));
//
//        ByteBuffer currByteBuffer = ByteBuffer.allocate(1000);
//        try( FileChannel logFC = FileChannel.open(logFile) )
//        {
//            // Read the first block from file
//            logFC.read(currByteBuffer);
//        }
//
//        MessageDigest md =MessageDigest.getInstance("SHA-256");
//
//        currByteBuffer.flip();
//        md.update(currByteBuffer);
//
//        byte[] currDigest = md.digest();
//        byte[] firstDigest = currState.getLogFile().getFirstBlock().getHashDigest();
//
//        Assert.assertArrayEquals(currDigest, firstDigest);
//
//        currByteBuffer.clear();
//        try( FileChannel logFC = FileChannel.open(logFile) )
//        {
//            // Read the last block from file.
//            logFC.position(logFC.size()-1000);
//            logFC.read(currByteBuffer);
//        }
//
//        currByteBuffer.flip();
//        md.update(currByteBuffer);
//
//        currDigest = md.digest();
//        byte[] lastDigest = currState.getLogFile().getLastProcessedBlock().getHashDigest();
//
//        Assert.assertArrayEquals(currDigest, lastDigest);
    }
}