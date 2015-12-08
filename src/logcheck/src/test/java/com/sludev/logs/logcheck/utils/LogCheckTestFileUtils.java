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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kervin on 2015-12-06.
 */
public final class LogCheckTestFileUtils
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckTestFileUtils.class);

    public static void checkAllLinesInFile(Path file, Pattern linePattern) throws IOException
    {
        int lineI = 1;

        String[] lines = Files.lines(file).toArray(String[]::new);
        for( String line : lines )
        {
            int lineNo = 0;

            Matcher pm = linePattern.matcher(line);
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
    }
}
