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
package com.sludev.logs.logcheck.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Watch a list of folders for File-system events.
 * 
 * @author Kervin Pierre
 */
public final class LogCheckFSWatch implements AutoCloseable
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckFSWatch.class);

    private final WatchService m_watcher;
    private final Map<WatchKey,Path> m_keys;
    private final boolean m_recursive;

    public WatchService getWatcher()
    {
        return m_watcher;
    }

    public Map<WatchKey, Path> getKeys()
    {
        return m_keys;
    }

    public boolean isRecursive()
    {
        return m_recursive;
    }

    private LogCheckFSWatch(final List<Path> dirs,
                            final WatchService watcher,
                            final Map<WatchKey, Path> keys,
                            final boolean recursive) throws IOException
    {
        this.m_watcher = watcher;
        this.m_keys = keys;
        this.m_recursive = recursive;

        if (recursive)
        {
            LOGGER.debug(String.format("Scanning '%s'...\n", dirs));

            registerAll(dirs.get(0), keys, watcher);

            LOGGER.debug("Scanning is done.\n");
        }
        else
        {
            register(dirs.get(0), keys, watcher);
        }
    }

    private static void registerAll(final Path start,
                                    final Map<WatchKey,Path> keys,
                                    final WatchService watcher ) throws IOException
    {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException
            {
                register(dir, keys, watcher);

                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void register( final Path dir,
                                  final Map<WatchKey,Path> keys,
                                  final WatchService watcher ) throws IOException
    {
        WatchKey key = dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY );

        if (LOGGER.isDebugEnabled())
        {
            Path prev = keys.get(key);
            if (prev == null)
            {
                LOGGER.debug(String.format("register: %s\n", dir));
            }
            else
            {
                if (!dir.equals(prev))
                {
                    LOGGER.debug(String.format("update: %s -> %s\n", prev, dir));
                }
            }
        }

        keys.put(key, dir);
    }

    public void processEvents( LogCheckFSWatchAction action ) throws InterruptedException, LogCheckException
    {
        processEvents( action, getWatcher(), getKeys(), isRecursive() );
    }

    public static void processEvents(final LogCheckFSWatchAction action,
                                     final WatchService watcher,
                                     final Map<WatchKey,Path> keys,
                                     final boolean recursive)
            throws InterruptedException, LogCheckException
    {
        LOGGER.debug("Starting processEvents() loop.");

        while( true )
        {
            // wait for key to be signalled
            WatchKey key;
            try
            {
                key = watcher.take();
            }
            catch( InterruptedException ex )
            {
                LOGGER.debug("Interrupted...", ex);

                throw ex;
            }

            Path dir = keys.get(key);
            if( dir == null )
            {
                LOGGER.error("WatchKey not recognized.");

                continue;
            }

            for( WatchEvent<?> event : key.pollEvents() )
            {
                WatchEvent.Kind kind = event.kind();

                // TODO :  provide example of how OVERFLOW event is handled
                if( kind == StandardWatchEventKinds.OVERFLOW )
                {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = (WatchEvent<Path>)event;

                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                //LOGGER.debug(String.format("%s: %s\n", event.kind().name(), child));

                // TODO : Process event action.
                action.apply(ev, child);

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if( recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE) )
                {
                    try
                    {
                        if( Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS) )
                        {
                            registerAll(child, keys, watcher);
                        }
                    }
                    catch( IOException ex )
                    {
                        LOGGER.debug( String.format("Exception while processing events."), ex );
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if( !valid )
            {
                keys.remove(key);

                // all directories are inaccessible
                if( keys.isEmpty() )
                {
                    LOGGER.error("All directories are inaccessible.");

                    break;
                }
            }
        }
    }

    public static LogCheckFSWatch from(final List<Path> dirs,
                                       final WatchService watcher,
                                       final Map<WatchKey,Path> keys,
                                       final boolean recursive ) throws IOException
    {
        LogCheckFSWatch res = new LogCheckFSWatch(dirs, watcher, keys, recursive);

        return res;
    }

    public static LogCheckFSWatch from(final List<Path> dirs,
                                       final Map<WatchKey,Path> keys,
                                       final boolean recursive ) throws IOException
    {
        WatchService watcher = FileSystems.getDefault().newWatchService();

        return from(dirs, watcher, keys, recursive);
    }

    public static LogCheckFSWatch from(final List<Path> dirs,
                                       final boolean recursive ) throws IOException
    {
        Map<WatchKey,Path> keys = new HashMap<>(10);

        return from(dirs, keys, recursive);
    }

    public static LogCheckFSWatch from(final List<Path> dirs ) throws IOException
    {
        return from( dirs, false );
    }

    @Override
    public void close()
            throws Exception
    {
        if( m_watcher != null )
        {
            m_watcher.close();
        }
    }
}