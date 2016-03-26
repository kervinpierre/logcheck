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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities supporting the file rotate features of the application.
 *
 * Created by kervin on 2015-12-07.
 */
public final class LogCheckFileRotate
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckFileRotate.class);

    public static Path prevName(final Path parentDir,
                                final Path currentName,
                                final Pattern matchPattern,
                                final List<Path> skipNames,
                                final List<LCFileRegexComponent> comps,
                                final boolean requireParentDir,
                                final boolean reverse) throws LogCheckException, IOException
    {
        Path res = null;

        if( LOGGER.isDebugEnabled() )
        {
            String dirList = null;

            try
            {
                // BUG : This keeps directory handles open.  'Too many open files' Exception results.
                try( Stream<Path> dirStream =  Files.list(parentDir) )
                {
                    dirList = dirStream.map(Path::toString).collect(Collectors.joining("\n"));
                }
            }
            catch( IOException ex )
            {
                String errMsg = String.format("prevName() : Error listing backup directory '%s'", parentDir);

                LOGGER.debug(errMsg, ex);

                throw ex;
            }

            // MAX_NOISE
         //   LOGGER.debug(String.format("prevName() : \n    parentDir : '%s'\n    currentName : '%s'\n\n%s",
         //           parentDir, currentName, dirList));
        }

        if( parentDir == null )
        {
            throw new LogCheckException("prevName() : Parent directory cannot be null");
        }

        if( Files.notExists(parentDir) )
        {
            if( requireParentDir )
            {
                throw new LogCheckException(String.format("Parent directory '%s' must exist.",
                        parentDir));
            }
            else
            {
                LOGGER.debug((String.format("Parent directory '%s' does not exist.",
                        parentDir)));

                return res;
            }
        }

        if( matchPattern == null )
        {
            LOGGER.debug("prevName() : Match Pattern is mandatory and cannot be null.");

            return null;
        }

        List<Path> children = new ArrayList<>(10);
        try
        {
            Stream<Path> pathStrm = Files.list(parentDir);

            pathStrm = pathStrm.filter( p ->
            {
                boolean filterRes = matchPattern.matcher(p.getFileName().toString()).matches();

                if( (skipNames != null) && (skipNames.size() > 0) )
                {
                    filterRes = filterRes
                            && (skipNames.contains(p.getFileName()) == false);
                }

                return filterRes;
            } );

            children.addAll(Arrays.asList(pathStrm.toArray(Path[]::new)));
        }
        catch( IOException ex )
        {
            LOGGER.debug("prevName() : Error list backup directory.", ex);

            throw ex;
        }

        if( children.size() < 1 )
        {
            LOGGER.debug("prevName() : No children found.");

            // No file-system child objects in the parent directory.
            return null;
        }

        if( (matchPattern != null) && (comps != null) )
        {
            List<Pair<Path, List<String>>> childrenComps = new ArrayList<>();

            for( Path child : children )
            {
                String path = child.getFileName().toString();
                Matcher pathMatcher = matchPattern.matcher(path);

                if( pathMatcher.matches() )
                {
                    if( comps.size() != pathMatcher.groupCount() )
                    {
                        String errMsg = String.format("prevName() : Component list size ( %d ) "
                                + " and Group Count ( %d ) are not equal ",
                                comps.size(), pathMatcher.groupCount());

                        LOGGER.debug(errMsg);

                        throw new LogCheckException(errMsg);
                    }

                    List<String> values = new ArrayList<>(10);
                    for( int pathI = 0; pathI < pathMatcher.groupCount(); pathI++ )
                    {
                        LCFileRegexComponent type = comps.get(pathI);
                        String currVal = pathMatcher.group(pathI+1);

                        values.add(currVal);
                    }

                    childrenComps.add(Pair.of(child, values));
                }
            }

            // Sort the child paths by components
            childrenComps = childrenComps.stream()
                .sorted((l1, l2) ->
                {
                    int sortRes = 0;

                    for( int compI = 0; compI < comps.size(); compI++ )
                    {
                        int currSortRes = 0;
                        String s1 = l1.getRight().get(compI);
                        String s2 = l2.getRight().get(compI);
                        LCFileRegexComponent c1 = comps.get(compI);

                        switch( c1 )
                        {
                            case INTEGER_INC:
                                {
                                    try
                                    {
                                        currSortRes = Integer.compare(Integer.parseInt(s1),
                                                Integer.parseInt(s2));
                                    }
                                    catch( NumberFormatException ex )
                                    {
                                        LOGGER.debug(String.format("Integer parse exception, '%s', '%s'",
                                                s1, s2), ex);
                                    }
                                }
                                break;

                            case TIMESTAMP:
                                currSortRes = s1.compareTo(s2);
                                break;

                            default:
                                break;
                        }

                        if( currSortRes != 0 )
                        {
                            sortRes = currSortRes;
                            break;
                        }
                    }

                    if( reverse )
                    {
                        // we're interested in the reverse
                        sortRes = -sortRes;
                    }

                    return sortRes;
                }).collect(Collectors.toList());

            if(  childrenComps.isEmpty() )
            {
                LOGGER.debug("prevName() : Children Components is empty.");
            }
            else
            {
                if( currentName != null )
                {
                    // Find the Path that follows the "Current Name" path
                    final AtomicReference<Path> found = new AtomicReference<>(null);

                    if( childrenComps.size() == 1 )
                    {
                        // The result of this should always be null
                        // Regardless of what 'Current Name' is
                        res = null;
                    }
                    else
                    {
                        childrenComps.stream()
                                .map(Pair::getLeft)
                                .reduce((a, b) ->
                                {
                                    // Find currentName then find the file after
                                    if( a.getFileName().equals(currentName.getFileName())
                                            && (found.get() == null) )
                                    {
                                        found.set(b);
                                    }

                                    return b;
                                });


                        res = found.get();
                    }

                }
                else
                {
                    res = childrenComps.stream()
                            .map(Pair::getLeft)
                            .findFirst().get();
                }
            }
        }
        else
        {
            // Use the natural sort to return the most recent
            Collections.sort(children);

            if( reverse )
            {
                Collections.reverse(children);
            }

            res = children.get(0);
        }

        if( (currentName != null) && (res != null) && currentName.equals(res) )
        {
            String errMsg = String.format("prevName() 'Result' == 'Current Name' argument '%s'",
                    currentName);

            LOGGER.debug(errMsg);
            throw new LogCheckException( errMsg );
        }

        if( res == null )
        {
            LOGGER.info(String.format("prevName() returning null for current name '%s' and parent dir '%s'",
                    currentName, parentDir));
        }
        else
        {
            LOGGER.debug((String.format("prevName() returning '%s'", res)));
        }

        return res;
    }
}
