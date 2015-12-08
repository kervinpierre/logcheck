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

import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by kervin on 2015-12-07.
 */
public final class LogCheckFileRotate
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckFileRotate.class);

    public static Path nextName( final Path parentDir,
                                 final Pattern matchPattern,
                                 final List<LCFileRegexComponent> comps,
                                 final boolean requireParent ) throws LogCheckException
    {
        Path res = null;

        if( parentDir == null )
        {
            throw new LogCheckException("Parent directory cannot be null");
        }

        if( Files.notExists(parentDir) )
        {
            if( requireParent )
            {
                throw new LogCheckException(String.format("Parent directory '%s' must exist.",
                        parentDir));
            }
            else
            {
                return res;
            }
        }

        List<Path> children = new ArrayList<>(10);
        try
        {
            Stream<Path> pathStrm = Files.list(parentDir);
            if( matchPattern != null )
            {
                pathStrm = pathStrm.filter( p -> matchPattern.matcher(
                                    p.getFileName().toString()).matches() );
            }
            children.addAll(Arrays.asList(pathStrm.toArray(Path[]::new)));
        }

        catch( IOException ex )
        {
            LOGGER.debug("Error list backup directory.", ex);
        }

        if( children.size() < 1 )
        {
            // No file-system child objects in the parent directory.
            return res;
        }

/*        if( matchPattern != null && comps != null )
        {
            // TODO : Execute an alternate sort use the components array
            for( Path child : children )
            {
                String path = child.getFileName().toString();
                Matcher pathMatcher = matchPattern.matcher(path);

                if( pathMatcher.matches() )
                {
                    List<String> values = new ArrayList<>(10);
                    for( int i = 1; i <= pathMatcher.groupCount(); i++ )
                    {
                        LCFileRegexComponent type = comps.get(i-1);
                        String currVal = pathMatcher.group(i);

                        values.add(currVal);
                    }
                }
            }
        }
        else*/
        {
            // Use the natural sort to return the most recent
            Collections.sort(children);
            Collections.reverse(children);

            res = children.get(children.size() - 1);
        }


        return res;
    }
}
