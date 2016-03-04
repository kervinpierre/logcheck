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

package com.sludev.logs.logcheck.config.writers;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.config.parsers.LogCheckConfigParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.main.LogCheckInitialize;
import com.sludev.logs.logcheck.utils.FSSArgFile;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by kervin on 2016-03-02.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCheckConfigWriterTest
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckConfigWriterTest.class);

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
    public void A001_testWrite() throws Exception
    {
        LOGGER.debug("A001_testWrite");

        List<String> argsList = new ArrayList<>();

        // Top tailing afer 1 minute
        argsList.add("--stop-after=1M");

        // Read the backup file from the
        argsList.add("--file-from-start");

        String logFile = "/tmp/test";
        argsList.add(String.format("--log-file %s", logFile));
        argsList.add("--log-entry-builder-type=singleline ");
        argsList.add("--log-entry-store-type=console,simplefile");
        argsList.add(String.format("--store-log-file %s", logFile ));
        argsList.add("--save-state");
        argsList.add(String.format("--state-file %s", logFile));
        argsList.add("--set-name=\"test app\"");
        argsList.add("--debug-flags=LOG_SOURCE_LC_APP");

        // Deduplication configuration
        argsList.add(String.format("--dedupe-dir-path %s", logFile));
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
        argsList.add(String.format("--tailer-backup-log-dir %s", logFile));

        String[] args = FSSArgFile.getArgArray(argsList);
        LogCheckConfig config = LogCheckInitialize.initialize(args);

        Path tmpConf = LogCheckConfigWriter.write(config);

        Assert.assertNotNull(tmpConf);
        Assert.assertTrue(Files.exists(tmpConf));

        LogCheckConfig lc = LogCheckConfigParser.readConfig(
                ParserUtil.readConfig(tmpConf, LCFileFormat.LCCONFIG));

        Assert.assertNotNull(lc);

        Assert.assertEquals(config, lc);
    }
}