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

package com.sludev.logs.logcheck.fs;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckFSWatch;
import com.sludev.logs.logcheck.utils.LogCheckFSWatchAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Watch the file-system for file changes.
 *
 * Created by kervin on 2016-01-27.
 */
public final class BackupFileWatch implements Callable<Integer>
{
    private static final Logger LOGGER = LogManager.getLogger(BackupFileWatch.class);

    private final List<Path> m_watchPaths;
    private final Pattern m_fileMatcher;
    private final LogCheckFSWatchAction m_action;
    private final BlockingDeque<Future> m_listeners;
    private final BlockingDeque<Path> m_detectedCreatePaths;
    private final AtomicLong m_detectedCreatePathCount;

    public Path getLastDetectedCreatePath()
    {
        return m_detectedCreatePaths.pollLast();
    }

    public long getDetectedCreatePathCount()
    {
        synchronized( m_detectedCreatePaths )
        {
            return m_detectedCreatePathCount.longValue();
        }
    }

    private void addDetectedCreatePath(Path path)
            throws InterruptedException
    {
        synchronized( m_detectedCreatePaths )
        {
            m_detectedCreatePaths.putLast(path);
            m_detectedCreatePathCount.incrementAndGet();
        }
    }

    public void addWatchOnTask(Future future)
            throws InterruptedException
    {
        m_listeners.putLast(future);
    }

    private BackupFileWatch(final List<Path> watchPaths,
                            final Pattern fileMatcher,
                            final LogCheckFSWatchAction action,
                            final BlockingDeque<Future> listeners,
                            final BlockingDeque<Path> detectedPaths)
    {
        m_detectedCreatePathCount = new AtomicLong(0);

        if( watchPaths != null )
        {
            this.m_watchPaths = watchPaths;
        }
        else
        {
            this.m_watchPaths = new ArrayList<>(10);
        }

        if( fileMatcher != null )
        {
            this.m_fileMatcher = fileMatcher;
        }
        else
        {
            this.m_fileMatcher = null;
        }

        if( listeners != null )
        {
            this.m_listeners = listeners;
        }
        else
        {
            this.m_listeners = new LinkedBlockingDeque<>();
        }

        if( detectedPaths != null )
        {
            this.m_detectedCreatePaths = detectedPaths;
        }
        else
        {
            this.m_detectedCreatePaths = new LinkedBlockingDeque<>();
        }

        this.m_action = action;
    }

    public static BackupFileWatch from(final List<Path> watchPaths,
                                       final Pattern fileMatcher,
                                       final LogCheckFSWatchAction action,
                                       final BlockingDeque<Future> listeners,
                                       final BlockingDeque<Path> detectedPaths)
    {
        BackupFileWatch res = new BackupFileWatch(watchPaths,
                                                    fileMatcher,
                                                    action,
                                                    listeners,
                                                    detectedPaths);

        return res;
    }

    @Override
    public Integer call() throws LogCheckException
    {
        Integer res = 0;
        LogCheckFSWatch watch;

        try
        {
            watch = LogCheckFSWatch.from(m_watchPaths);

            if( m_action != null )
            {
                watch.processEvents(m_action);
            }
            else
            {
                watch.processEvents((WatchEvent<Path> event, Path path)
                        ->
                {
                    int watchRes = 0;

                    if( event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)
                            == false )
                    {
                        return watchRes;
                    }

                    if( m_fileMatcher != null )
                    {
                        if( m_fileMatcher.matcher(path.getFileName().toString()).matches() )
                        {
                            LOGGER.debug(String.format("New file detected '%s'",
                                    path));

                            addDetectedCreatePath(path);

                            // New backup file created
                            for( Future thr : m_listeners )
                            {
                                if( thr.isDone() || thr.isCancelled() )
                                {
                                    // This listener has already been stopped
                                    m_listeners.remove(thr);
                                }
                                else
                                {
                                    // Signal that the directory has a
                                    // new file
                                    thr.cancel(true);
                                }
                            }
                        }
                    }

                    return watchRes;
                });
            }
        }
        catch (IOException | LogCheckException ex)
        {
            LOGGER.error(String.format("Error watching backup directory...\n'%s'",
                    StringUtils.join(m_watchPaths.toArray())), ex);

            return 1;
        }
        catch (InterruptedException ex)
        {
            LOGGER.info(String.format("Interrupted watching backup directory...\n'%s'",
                    StringUtils.join(m_watchPaths.toArray())), ex);
        }

        return res;
    }
}
