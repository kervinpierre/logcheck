/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2016] SLU Dev Inc. <info@sludev.com>
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

package com.sludev.logs.logcheck.store;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.enums.LCDeDupeAction;
import com.sludev.logs.logcheck.enums.LCLogLevel;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.log.impl.LogEntryQueueSource;
import com.sludev.logs.logcheck.store.impl.LogEntryConsole;
import com.sludev.logs.logcheck.utils.FSSLog4JConfiguration;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by kervin on 2016-03-16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ILogEntryStoreTest
{
    private static final Logger LOGGER
            = LogManager.getLogger(ILogEntryStoreTest.class);

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

    @Test
    public void A001_testProcess() throws Exception
    {
        FSSLog4JConfiguration.setVerbosity(FSSVerbosityEnum.DEBUG);
        LOGGER.debug("A001_testProcess");

        BlockingDeque<LogEntry> completedEntries = new LinkedBlockingDeque<>();
        List<ILogEntryStore> stores = new ArrayList<>(1);
        Path deDupeDirPath = Paths.get("/tmp/A001_testProcess/deDupe/");
        ILogEntrySource src = LogEntryQueueSource.from(completedEntries);
        UUID runUUID = UUID.randomUUID();

        FileUtils.forceMkdir(deDupeDirPath.toFile());

        LocalDateTime currTime = LocalDateTime.now();

        LogEntry currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        ILogEntryStore currStore = LogEntryConsole.from();
        stores.add(currStore);

        LogCheckResult res = ILogEntryStore.process(src, stores, deDupeDirPath,
                                                    runUUID, 5, 10, 10,
                                                    null, null, 2L, 4L,
                                                    LCDeDupeAction.BREAK,
                                                    1L, TimeUnit.MINUTES);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatuses().contains(LCResultStatus.TIMEDOUT));

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        currLE = LogEntry.from(null, LCLogLevel.ERROR, "com.example",
                "Log message body. Error 01.", "Exception Stacktrace:",
                currTime, "localhost", null);
        completedEntries.putLast(currLE);

        res = ILogEntryStore.process(src, stores, deDupeDirPath,
                runUUID, 5, 10, 10,
                null, null, 2L, 4L,
                LCDeDupeAction.BREAK,
                1L, TimeUnit.MINUTES);

        Assert.assertNotNull(res);
        Assert.assertTrue(res.getStatuses().contains(LCResultStatus.BREAK));
    }
}