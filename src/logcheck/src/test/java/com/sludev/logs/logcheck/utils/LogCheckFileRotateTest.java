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

package com.sludev.logs.logcheck.utils;

import com.sludev.logs.logcheck.LogCheckProperties;
import com.sludev.logs.logcheck.LogCheckTestWatcher;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.io.FileUtils;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by kervin on 2015-12-23.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LogCheckFileRotateTest
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckFileRotateTest.class);

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

    @After
    public void tearDown()
    {

    }

    @Test
    public void A001_testPrevNameSimple() throws IOException, LogCheckException
    {
        Path testDir = Paths.get("/tmp/A001_testPrevNameSimple");

        List<LCFileRegexComponent> comps = new ArrayList<>(10);
        comps.add(LCFileRegexComponent.FILENAME_PREFIX);
        comps.add(LCFileRegexComponent.INTEGER_INC);

        Path currName = Paths.get("test file.name.01.log.0001.bak");

        Deque<String> arr = new ArrayDeque<>(10);
        arr.addLast("test file.name.01.log");
        arr.addLast("test file.name.01.log.0000.bak");
        arr.addLast("test file.name.01.log.0002.bak");
        arr.addLast("test file.name.01.log.0001.bak");
        arr.addLast("test file.name.01.log.0052.bak");
        arr.addLast("test file.name.01.log.0300.bak");

        LogCheckTestFileUtils.createRandomLogFiles01(testDir, arr);

        List<Path> skipNames = new ArrayList<>(10);
        skipNames.add(Paths.get("test file.name.01.log.0002.bak"));

        Pattern matchPattern = Pattern.compile("(.*?)\\.(\\d+)\\.bak");

        // Reversed, DESC
        Path prevName = LogCheckFileRotate.prevName(testDir, currName, matchPattern,
                skipNames, comps, true, true);

        Assert.assertNotNull(prevName);
        Assert.assertTrue(Objects.equals(prevName.getFileName().toString(),
                                                "test file.name.01.log.0000.bak"));

        // Forward, ASC
        prevName = LogCheckFileRotate.prevName(testDir, currName, matchPattern,
                skipNames, comps, true, false);

        Assert.assertNotNull(prevName);
        Assert.assertTrue(Objects.equals(prevName.getFileName().toString(),
                "test file.name.01.log.0052.bak"));
    }
}
