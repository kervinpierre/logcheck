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

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for processing argument files.  These are files which hold command line arguments.
 *
 * Created by kervin on 2015-11-25.
 */
public final class FSSArgFile
{
    private static final Logger log = LogManager.getLogger(FSSArgFile.class);

    public static String[] getArgArray(Path file) throws LogCheckException
    {
        String[] res = null;

        BufferedReader reader = null;
        String argLine = "";

        try
        {
            reader = Files.newBufferedReader(file);
            boolean stop = false;
            boolean multiLineComment = false;

            do
            {
                String currLine = reader.readLine();
                if( currLine == null )
                {
                    stop = true;
                }
                else if(  currLine.matches("^\\s*#.*") )
                {
                    // Ignore single line comments everywhere
                    ;
                }
                else if(  currLine.matches("^\\s*/\\*.*?\\*/\\s*$") )
                {
                    // Ignore single line comments everywhere
                    ;
                }
                else if(  currLine.matches("^\\s*/\\*.*") )
                {
                    // Multi-line comment start
                    multiLineComment = true;
                }
                else if( currLine.matches("^.*?\\*/\\s*$") )
                {
                    // Multi-line comment end
                    multiLineComment = false;
                }
                else if( StringUtils.isBlank(currLine) )
                {
                    if( StringUtils.isNoneBlank(argLine) )
                    {
                        // Blank lines stop line processing
                        stop = true;
                    }
                }
                else if( multiLineComment )
                {
                    // We are inside a multiline comment.
                    // Ignore input.
                    ;
                }
                else
                {
                    if( StringUtils.isBlank(argLine) )
                    {
                        argLine = currLine.trim();
                    }
                    else
                    {
                        argLine = String.format("%s %s", argLine, currLine.trim());
                    }

                    if( argLine.endsWith("\\") )
                    {
                        Matcher m = Pattern.compile("^(.*?)(\\s*\\\\)$").matcher(argLine);

                        if( m.matches() )
                        {
                            argLine = m.group(1);
                        }
                    }
                    else
                    {
                        stop = true;
                    }

                }
            }
            while(  stop == false );

            // Use CSV parser because I really do not what to deal with quote and escape rules
            // more than I have too.
            CSVReader cread = new CSVReader(new StringReader(argLine+"\n"), ' ', '"', '\\');
            res = cread.readNext();
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error reading argument file '%s'", file);

            log.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        return res;
    }
}
