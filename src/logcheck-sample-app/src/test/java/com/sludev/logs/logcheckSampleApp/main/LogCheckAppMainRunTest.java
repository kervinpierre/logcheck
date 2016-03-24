package com.sludev.logs.logcheckSampleApp.main;

import com.sludev.logs.logcheckSampleApp.LogCheckTestWatcher;
import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.utils.FSSArgFile;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by kervin on 2015-12-19.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCheckAppMainRunTest
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckAppMainRunTest.class);

    private Properties m_testProperties;

    @Rule
    public TestWatcher m_testWatcher = new LogCheckTestWatcher();

    @Before
    public void setUp() throws Exception
    {

    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void A001_testRotateAndTimeout()
            throws IOException, LogCheckAppException, ExecutionException, InterruptedException
    {
        Path testDir = Paths.get("/tmp/A001_testRotateAndTimeout01");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        String[] args;
        List<String> argsList = new ArrayList<>(20);

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");

        argsList.add(String.format("--output-file %s", logFile));
        argsList.add("--delete-logs");
        argsList.add("--stop-after-count 1K");

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

        LCSAResult res = lcAppFuture.get();

        Assert.assertNotNull(res);
        Assert.assertTrue(res==LCSAResult.SUCCESS);

        Path[] files = Files.list(testDir).toArray(Path[]::new);

        Assert.assertTrue(files.length==26);
        Assert.assertTrue(Files.lines(logFile).count()==24);

        for( Path p : files )
        {
            if( p.equals(logFile) )
                continue;

            Assert.assertTrue(Files.lines(p).count() == 40);
        }
    }

    @Test
    public void A002_testRotateAndTimeoutRepeat() throws IOException, InterruptedException, ExecutionException, LogCheckAppException
    {
        Path testDir = Paths.get("/tmp/A002_testRotateAndTimeoutRepeat");

        if( Files.exists(testDir) )
        {
            FileUtils.deleteDirectory(testDir.toFile());
        }

        Files.createDirectory(testDir);

        Path logFile = testDir.resolve("logcheck-sample-app-output.txt");

        testRotateAndTimeout_Internal(logFile, 0);
        testRotateAndTimeout_Internal(logFile, 1024);

        Path[] files = Files.list(testDir).toArray(Path[]::new);

        Assert.assertTrue(files.length==51);
        Assert.assertTrue(Files.lines(logFile).count()==8);

        for( Path p : files )
        {
            if( p.equals(logFile) )
                continue;

            Assert.assertTrue(Files.lines(p).count() == 40);
        }
    }

    private void testRotateAndTimeout_Internal(final Path logFile,
                                               final int lineStartNumber) throws LogCheckAppException, ExecutionException, InterruptedException, IOException
    {
        String[] args;
        List<String> argsList = new ArrayList<>(20);

        argsList.add(String.format("--output-file %s", logFile));
        argsList.add("--stop-after-count 1K");

        // The following should cause a log rotate per second
        // This should cause an issue with the tailer also having
        // a poll interval of a second.
        //
        // ...and the handling of that issue is what this test is
        // about.
        argsList.add("--output-frequency 25ms");
        argsList.add("--rotate-after-count 40");

        argsList.add(String.format("--start-line-number %d", lineStartNumber));

        args = FSSArgFile.getArgArray(argsList);
        LogCheckAppConfig appConfig = LogCheckAppInitialize.initialize(args);

        LogCheckAppMainRun currAppRun = new LogCheckAppMainRun(appConfig);

        BasicThreadFactory thFactory = new BasicThreadFactory.Builder()
                .namingPattern("testLCAppRunThread-%d")
                .build();
        ExecutorService appThreadExe = Executors.newSingleThreadExecutor(thFactory);
        Future<LCSAResult> lcAppFuture = appThreadExe.submit(currAppRun);
        appThreadExe.shutdown();

        LCSAResult res = lcAppFuture.get();

        Assert.assertNotNull(res);
        Assert.assertTrue(res==LCSAResult.SUCCESS);
    }
}