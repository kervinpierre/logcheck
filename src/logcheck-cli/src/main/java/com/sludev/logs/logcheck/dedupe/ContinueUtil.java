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

package com.sludev.logs.logcheck.dedupe;

import com.sludev.logs.logcheck.config.entities.LogCheckDeDupeLog;
import com.sludev.logs.logcheck.config.entities.LogEntryDeDupe;
import com.sludev.logs.logcheck.config.parsers.LogCheckDeDupeLogParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by kervin on 2015-11-21.
 */
public final class ContinueUtil
{
    private static final Logger log = LogManager.getLogger(ContinueUtil.class);

    public static List<LogCheckDeDupeLog> readLastDeDupeLogs(final Path deDupeLogDir,
                                                             final String setName,
                                                             final String dirPrefix,
                                                             final Integer count) throws LogCheckException
    {
        String currSetDirPrefix = dirPrefix;
        if( currSetDirPrefix == null )
        {
            if( setName == null )
            {
                throw new LogCheckException("Both 'setName' and 'dirPrefix' are null");
            }

            currSetDirPrefix = setName.replaceAll("[^a-zA-Z0-9.-]", "_").trim();
        }

        List<Path> allFiles;
        try
        {
            final String pre = currSetDirPrefix;
            try( Stream<Path> currPathStream = Files.list(deDupeLogDir) )
            {
                allFiles = currPathStream.filter(p -> p.getFileName().startsWith(pre))
                        .collect(Collectors.toList());
            }
        }
        catch( IOException ex )
        {
            log.debug("Error listing Deduplication directory.", ex);

            throw new LogCheckException("Error listing Deduplication directory.", ex);
        }

        List<Path> currFiles;

        if( (count == null) || (allFiles.size() <= count) )
        {
            currFiles = allFiles;
        }
        else
        {
            currFiles = allFiles.subList(allFiles.size()-count, allFiles.size());
        }

        List<LogCheckDeDupeLog> res = new ArrayList<>();
        for( Path p : currFiles)
        {
            LogCheckDeDupeLog currLog
                    = LogCheckDeDupeLogParser.readConfig(
                            ParserUtil.readConfig(p, LCFileFormat.LCDDUPE));

            res.add(currLog);
        }

        return res;
    }

    public static List<LogEntryDeDupe> lastLogEntryDeDupes(final List<LogCheckDeDupeLog> logs,
                                                           final Integer count)
    {
        List<LogEntryDeDupe> res = new ArrayList<>();

        for( LogCheckDeDupeLog log : logs )
        {
            res.addAll(log.getLogEntryDeDupes());

            if( (count != null) && (res.size() >= count) )
            {
                res = res.subList(0, count);
                break;
            }
        }

        return res;
    }
}
