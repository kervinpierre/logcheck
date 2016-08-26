/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.tail.impl;

import com.sludev.logs.logcheck.config.entities.LogFileState;
import com.sludev.logs.logcheck.config.entities.impl.LogCheckState;
import com.sludev.logs.logcheck.config.entities.impl.WindowsEventLogCheckState;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.log.ILogEntryBuilder;
import com.sludev.logs.logcheck.tail.ITail;
import com.sludev.logs.logcheck.tail.TailerResult;
import com.sludev.logs.logcheck.tail.TailerStatistics;
import com.sludev.logs.logcheck.utils.LCWindowsEventWrapper;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple implementation of tailing Windows Events
 *
 * This should be the only class with open handles on the Windows Event Log
 *
 * Notes : "BUG: ReadEventLog Fails with Error 87"
 *          https://support.microsoft.com/en-us/kb/177199
 */
public final class WindowsEventTailer implements ITail
{
    private static final Logger LOGGER
                    = LogManager.getLogger(WindowsEventTailer.class);

    /**
     * The connection string for connecting to the Windows Event Service.
     */
    private final String m_windowsEventConnection;

    /**
     * The character set that will be used to read the m_file.
     */
    private final Charset m_cset;

    /**
     * The amount of time to wait for the m_file to be updated.
     */
    private final long m_delayMillis;

    private final Map<String, Integer> m_startPosition;

    /**
     * Whether to tail from the end or start of m_file
     */
    private final boolean m_end;

    private final boolean m_statsValidate;
    private final boolean m_statsCollect;

    /**
     * Reset the statistics on disk before starting tailing
     */
    private final boolean m_statsReset;

    /**
     * The listener to notify of events when tailing.
     */
    private final List<ILogEntryBuilder> m_builders;

    /**
     * Whether to close and reopen the m_file whilst waiting for more input.
     */
    private final boolean m_reOpen;

    private final boolean m_startPosIgnoreErr;

    private final boolean m_stopOnEOF;

    private final int m_saveTimerSeconds;

    private final TailerStatistics m_statistics;

    private final LCHashType m_hashType;
    private final Integer m_idBlockSize;
    private final String m_setName;

    /**
     * Needed since Future will not differentiate between cancelled and
     * terminated.
     */
    private final CountDownLatch m_completionLatch;

    // Mutable

    private final Set<LCDebugFlag> m_debugFlags;

    /**
     * The tailer will run as long as this value is true.
     */
    private volatile boolean m_run = true;

    private volatile boolean m_doCollectStats = false;

    private String lineRemainder;

    private WindowsEventLogCheckState m_lastLCState;

    private TailerResult m_finalResult;
    /**
     * Used to debug and track Logcheck Log App's log sequence.
     */
    public static int DEBUG_LCAPP_LOG_SEQUENCE = 0;

    /**
     * Creates a Tailer for the given m_file, with a specified buffer size.
     *
     * @param windowsEventConnection Connection string for connecting to Windows Events.
     * @param cset the Charset to be used for reading the m_file
     * @param delayMillis the delay between checks of the m_file for new content
     * in milliseconds.
     * @param end Set to true to tail from the end of the m_file, false to tail
     * from the beginning of the m_file.
     * @param reOpen if true, close and reopen the m_file between reading chunks
     * @param bufSize Buffer size
     */
    private WindowsEventTailer( final String windowsEventConnection,
                                final Map<String, Integer> startPosition,
                                final Charset cset,
                                final List<ILogEntryBuilder> builders,
                                final long delayMillis,
                                final boolean end,
                                final boolean reOpen,
                                final boolean startPosIgnoreErr,
                                final boolean statsValidate,
                                final boolean statsCollect,
                                final boolean statsReset,
                                final boolean stopOnEOF,
                                final int bufSize,
                                final int saveTimerSeconds,
                                final TailerStatistics stats,
                                final LCHashType hashType,
                                final Integer idBlockSize,
                                final String setName,
                                final Set<LCDebugFlag> debugFlags,
                                final CountDownLatch completionLatch)
    {
        this.m_windowsEventConnection = windowsEventConnection;
        this.m_delayMillis = delayMillis;
        this.m_end = end;
        this.m_startPosIgnoreErr = startPosIgnoreErr;

        // Save and prepare the listener
        this.m_builders = builders;
        this.m_reOpen = reOpen;
        this.m_statsValidate = statsValidate;
        this.m_statsCollect = statsCollect;
        this.m_statsReset = statsReset;
        this.m_cset = cset;
        this.m_statistics = stats;

        this.m_hashType = hashType;
        this.m_idBlockSize = idBlockSize;
        this.m_setName = setName;

        this.m_stopOnEOF = stopOnEOF;

        this.m_startPosition = startPosition;

        this.lineRemainder = null;
        this.m_lastLCState = null;

        this.m_debugFlags = debugFlags;

        this.m_saveTimerSeconds = saveTimerSeconds;
        this.m_completionLatch = completionLatch;

        this.m_finalResult = null;
    }

    public static WindowsEventTailer from( final String windowsEventConnection,
                                           final Map<String, Integer> startPosition,
                                           final Charset cset,
                                           final List<ILogEntryBuilder> builders,
                                           final long delayMillis,
                                           final boolean end,
                                           final boolean reOpen,
                                           final boolean startPosIgnoreErr,
                                           final boolean statsValidate,
                                           final boolean statsCollect,
                                           final boolean statsReset,
                                           final boolean stopOnEOF,
                                           final int bufSize,
                                           final int saveTimerSeconds,
                                           final TailerStatistics stats,
                                           final LCHashType hashType,
                                           final Integer idBlockSize,
                                           final String setName,
                                           final Set<LCDebugFlag> debugFlags,
                                           final CountDownLatch completionLatch)
    {
        WindowsEventTailer res = new WindowsEventTailer(windowsEventConnection,
                startPosition,
                cset,
                builders,
                delayMillis,
                end,
                reOpen,
                startPosIgnoreErr,
                statsValidate,
                statsCollect,
                statsReset,
                stopOnEOF,
                bufSize,
                saveTimerSeconds,
                stats,
                hashType,
                idBlockSize,
                setName,
                debugFlags,
                completionLatch);

        return res;
    }

    /**
     * Return the Windows Connection String for Events.
     *
     * @return the Events connection string
     */
    public String getWindowsEventConnection()
    {
        return m_windowsEventConnection;
    }

    /**
     * Allows us to pass the result object out as soon as it's ready. No need
     * to wait for call() to complete and return that value.  Which can happen
     * if the completion latch is signalled before return.
     *
     * @return
     */
    public synchronized TailerResult getFinalResult()
    {
        return m_finalResult;
    }

    /**
     * Follows changes in the file, calling the ITailerListener's handle method
     * for each new line.
     */
    @Override
    public TailerResult call() throws LogCheckException, IOException
    {
        if( LOGGER.isInfoEnabled() )
        {
            LOGGER.info(String.format("Starting tailer : TailerStart .\n%s", toString()));
        }

        TailerResult res = TailerResult.from(null, null);
        Map<String,List<String>> readers = null;
        int flags = 0;
        long position = 0; // position within the file

        if( m_reOpen )
        {
            // Default result for re-open, is REOPEN
            res.getResultSet().add(LCTailerResult.REOPEN);
        }

        ScheduledExecutorService statsSchedulerExe = null;
        if( m_saveTimerSeconds > 0 )
        {
            // Signals to the reader when to collect statistics
            BasicThreadFactory tailerSaveFactory = new BasicThreadFactory.Builder()
                    .namingPattern("winEventTailerCollectThread-%d")
                    .daemon(true)
                    .build();

            statsSchedulerExe
                    = Executors.newScheduledThreadPool(1, tailerSaveFactory);

            statsSchedulerExe.scheduleWithFixedDelay(() ->
                    {
                        m_doCollectStats = true;
                    }, m_saveTimerSeconds,
                    m_saveTimerSeconds,
                    TimeUnit.SECONDS );
        }

        try
        {
            if( StringUtils.isBlank(m_windowsEventConnection) )
            {
                throw new LogCheckException("Windows Event Connection string cannot be empty");
            }

            String[] WECToks = m_windowsEventConnection.split(":");

            String currServerName = WECToks[0];
            String currSourceNameStr = WECToks[1];
            String[] currSources = currSourceNameStr.split(",");

            readers = new HashMap<>();

            readers.put(currServerName, Arrays.asList(currSources));

            // Now loop the log events
            while( m_run && (readers != null) && readers.isEmpty()==false )
            {

                // FIXME : If the log file is rotated between validation above
                //         and here it is possible to lose the tail logs in that
                //         file.

                // FIXME : ID the FIRST_BLOCK on the file here, before reading.
                //         If this ID fails we don't bother with the log builders

                // Final check for a log rotate before reading the log file
                // This is really the last time we can try for now.
                if( Thread.interrupted() )
                {
                    m_run = false;

                    LOGGER.debug("call() : Interrupt detected.  Exiting.");

                    res.getResultSet().add(LCTailerResult.INTERRUPTED);
                }

                Map<String, Integer> readFlags = new HashMap<>();
                Map<String, Integer> offsets = new HashMap<>();

                for( String svrK : readers.keySet() )
                {
                    List<String> srcs = readers.get(svrK);
                    for( String src : srcs )
                    {
                        // https://support.microsoft.com/en-us/kb/177199
                        // SEEK flag fails with Error 87
                        readFlags.put(src, WinNT.EVENTLOG_SEQUENTIAL_READ | WinNT.EVENTLOG_FORWARDS_READ);
                        offsets.put(src, 0);
                    }
                }

                // Read the events in the Event database
                readLines(readers, readFlags, offsets);

                // TODO : Confirm FIRST_BLOCK here, and ID LAST_BLOCK from read data
                //        Detects 'Log swap while tailing'

                // FIXME : Statistics thread should NOT ID file blocks.
                //         This causes lots of races

                // Save the last state if it hasn't been for specific
                // result codes.  Don't save the state if there was a
                // validation error.
                if( res.getResultSet().contains(LCTailerResult.VALIDATION_FAIL)
                        || res.getResultSet().contains(LCTailerResult.INTERRUPTED))
                {
                    LOGGER.debug("Skipping state save because result includes VALIDATION_FAIL.");
                }
                else
                {
//                    if( m_statsCollect )
//                    {
//                        // FIXME : Last processed time incorrect or missing
//                        for( WinNT.HANDLE h : readers )
//                        {
//                            LOGGER.debug("call() : Saving state.");
//                            m_lastLCState = getState(h,
//                                    m_setName,
//                                    Instant.now());
//
//                            m_statistics.save(m_lastLCState, true, false);
//                            //m_statistics.clearPendingSaveState();
//                        }
//                    }
                }

                // Delay exit if requested.
                // This gives us a fixed interval *between* calls.
                // BUG : Delay has to be inside this loop for non-reopen tailing
                if( m_delayMillis > 0 )
                {
                    LOGGER.debug(String.format("Sleeping/delay for %dms\n",
                            m_delayMillis));

                    // Delay without interrupts
                    final WindowsEventTailer objInstance = this;
                    final AtomicBoolean delayCompleted = new AtomicBoolean(false);
                    final Thread delayThread = new Thread( () ->
                    {
                        try
                        {
                            Thread.sleep(m_delayMillis);
                        }
                        catch( InterruptedException ex )
                        {
                            LOGGER.debug("Uninterruptable sleep interrupted!"
                                    + "  This shouldn't have except during shutdown.");
                        }
                        finally
                        {
                            synchronized( objInstance )
                            {
                                delayCompleted.set(true);
                                objInstance.notifyAll();
                            }
                        }
                    });
                    delayThread.setName("fileTailerDelayThread");
                    delayThread.setDaemon(true);
                    delayThread.start();

                    while( delayCompleted.get() == false )
                    {
                        try
                        {
                            synchronized( this )
                            {
                                wait(Math.max(m_delayMillis, 1));
                            }
                        }
                        catch( InterruptedException ex )
                        {
                            // Ignore interrupts
                            res.getResultSet().add(LCTailerResult.INTERRUPTED);
                            LOGGER.debug("Delay ignored interrupt.");
                        }
                    }

                    res.getResultSet().add(LCTailerResult.DELAY_COMPLETED);
                    LOGGER.debug("End delay/sleep");
                }
            }
        }
        catch( final InterruptedException ex )
        {
            res.getResultSet().add(LCTailerResult.INTERRUPTED);
        }
        finally
        {
            if( res.getState() == null )
            {
                LOGGER.debug("call() : result without a Log State object.  Adding last state...");
                //res = TailerResult.from(res.getResultSet(), m_lastLCState);
            }

            m_finalResult = res;

//            if( m_completionLatch != null )
//            {
//                m_completionLatch.countDown();
//            }

            if( statsSchedulerExe != null )
            {
                statsSchedulerExe.shutdown();
            }
        }

        if( LOGGER.isDebugEnabled() )
        {
            StringBuilder resStr = new StringBuilder(100);
            for( LCTailerResult tr : res.getResultSet() )
            {
                resStr.append(String.format("%s, ", tr));
            }

            LOGGER.debug(String.format("WindowsEventTailer thread exit ( %s )", resStr));
        }

        return res;
    }

    /**
     * Print out the current object.
     *
     * @return
     */
    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder(100);

        res.append("FileTailer\n{\n");
        res.append(String.format("    Start Position  : '%s'\n", m_startPosition));
        res.append(String.format("    Run             : '%b'\n", m_run));
        res.append(String.format("    Re-open wait    : '%b'\n", m_reOpen));
        res.append(String.format("    Stats Collect   : '%b'\n", m_statsCollect));
        res.append(String.format("    Stats Validate  : '%b'\n", m_statsValidate));
        res.append(String.format("    Stats Reset     : '%b'\n", m_statsReset));
        res.append(String.format("    Stop on EOF     : '%b'\n", m_stopOnEOF));
        res.append(String.format("    Tail from End   : '%b'\n", m_end));
        res.append(String.format("    Delay           : '%d' ms\n", m_delayMillis));
        res.append(String.format("    Charset         : '%s'\n", m_cset));
        res.append(String.format("    Ignore Start Pos Error : '%b'\n", m_startPosIgnoreErr));
        res.append("}\n");

        return res.toString();
    }

    /**
     * Allows the tailer to complete its current loop and return.
     */
    public void stop()
    {
        this.m_run = false;
    }

    private String ev2Str( LCWindowsEventWrapper eventWrapper )
    {
        StringBuilder res = new StringBuilder(100);

        Advapi32Util.EventLogRecord ev = eventWrapper.getEvent();

        res.append(String.format("{\"source\": \"%s\",", ev.getSource()));
        res.append(String.format("\"statusCode\": \"%d\",", ev.getStatusCode()));
        res.append(String.format("\"channel\": \"%s\",", eventWrapper.getChannel()));
        res.append(String.format("\"severity\": \"%s\",", ev.getType()));
        res.append(String.format("\"recordNumber\": \"%d\",", ev.getRecordNumber()));
        res.append(String.format("\"computerName\": \"%s\",", eventWrapper.getComputerName()));

        WinNT.EVENTLOGRECORD currRec = ev.getRecord();

        res.append(String.format("\"timeGenerated\": \"%s\",",
                Instant.ofEpochSecond(currRec.TimeGenerated.intValue())));

        // FIXME : Treat the data as binary, even though it's usually text
        res.append(String.format("\"dataStr\": \"%s\"}",
                new String(ev.getData(), StandardCharsets.UTF_16LE )));

        return res.toString();
    }

    /**
     * Read new lines.
     *
     * @param readers The handles from the Windows Events
     * @throws IOException if an I/O error occurs.
     */
    private void readLines( final Map<String, List<String>> readers,
                            final Map<String, Integer> readFlags,
                            final Map<String, Integer> offSet)
            throws IOException, LogCheckException, InterruptedException
    {
        if( LOGGER.isDebugEnabled() )
        {
            StringBuilder sb = new StringBuilder();

            for( String k : readers.keySet() )
            {
                List<String> srcs = readers.get(k);

                sb.append(String.format("server : '%s'\n", k));

                for( String src : srcs )
                {
                    sb.append(String.format("    source : '%s'\n        readFlags : '%d'\n        offset : '%s'\n",
                            src, readFlags.get(src), offSet.get(src)));
                }
            }

            LOGGER.debug(String.format("readLines() : %s\n", sb));
        }

        if (readers == null)
        {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        String lastSvr = null;
        String lastSrc = null;
        String lastRecId = null;
        Integer lastRecPos = null;
        Integer lastRecCount = null;

        Map<String, Map<String, WinNT.HANDLE>> handleMaps = new HashMap<>();

        int runCount = 0;
        boolean firstLoop = true;

        while( m_run )
        {
            for( String svrK : readers.keySet() )
            {
                List<String> sources = readers.get(svrK);
                Map<String, WinNT.HANDLE> serverMap;
                if( handleMaps.containsKey(svrK) )
                {
                    serverMap = handleMaps.get(svrK);
                }
                else
                {
                    serverMap = new HashMap<>();
                    handleMaps.put(svrK, serverMap);
                }

                for( String src : sources )
                {
                    WinNT.HANDLE reader;
                    Integer currReadFlags = readFlags.get(src);
                    Integer currOffSet = 0;

                    if( firstLoop )
                    {
                        currOffSet = offSet.get(src);
                    }

                    if( serverMap.containsKey(src) )
                    {
                        reader = serverMap.get(src);
                    }
                    else
                    {
                        reader = Advapi32.INSTANCE.OpenEventLog(svrK, src);
                        serverMap.put(src, reader);
                    }

                    List<LCWindowsEventWrapper> events
                            = readEvents(reader, currReadFlags, currOffSet, 0, src);

                    for( LCWindowsEventWrapper record : events )
                    {
                        for( ILogEntryBuilder ib : m_builders )
                        {
                            ib.handleLogLine(ev2Str(record));
                        }
                    }

                    IntByReference pnCount = new IntByReference();
                    if( Advapi32.INSTANCE.GetNumberOfEventLogRecords(reader, pnCount) )
                    {
                        lastRecCount = pnCount.getValue();
                    }

                    if( events != null && events.isEmpty() == false )
                    {
                        lastRecId = Objects.toString( events.get(events.size()-1)
                                                            .getEvent().getEventId());
                        lastRecPos = events.get(events.size()-1)
                                                            .getEvent().getRecordNumber();
                    }

                    lastSvr = svrK;
                    lastSrc = src;
                }
            }

            firstLoop = false;
        }

        // Collect statistics
        if( m_statsCollect && m_doCollectStats && (runCount++ % 10 == 0) )
        {
            //m_statistics.setLastProcessedPosition(reader.position());

            try
            {
                m_lastLCState = getState(m_setName,
                        lastSvr, lastSrc,
                        lastRecId, lastRecPos, lastRecCount,
                        Instant.now());

                m_statistics.putPendingSaveState(m_lastLCState);
            }
            catch( InterruptedException ex )
            {
                // Let's not stop processing over a Save State interrupt
                LOGGER.info("readLines() : putPendingSaveState() interrupted.");
            }

            m_doCollectStats = false;
        }

        if( m_run == false )
        {
            // We've been asked to stop running
            LOGGER.debug("Tailer process stopping by request.");
        }
    }

    public static List<LCWindowsEventWrapper> readEvents( final WinNT.HANDLE reader,
                                                                final int readFlags,
                                                                final int offSet,
                                                                final int maxReadCount,
                                                                final String source )
    {
        IntByReference pnBytesRead = new IntByReference();
        IntByReference pnMinNumberOfBytesNeeded = new IntByReference();

        Memory _buffer = new Memory(1024 * 64);
        List<LCWindowsEventWrapper> res = new ArrayList<>();

        int rc = 0;
        for(int i=0;;i++)
        {
            if( Advapi32.INSTANCE
                    .ReadEventLog(reader, readFlags,
                            0, _buffer, (int) _buffer.size(), pnBytesRead,
                            pnMinNumberOfBytesNeeded) == false )
            {
                rc = Kernel32.INSTANCE.GetLastError();

                LOGGER.debug(String.format("ReadEventLog() : iteration %d : GetLastError() %d ( 0x%08X )",
                        i, rc, rc & 0xFFFFFFFF));

                switch(rc)
                {
                    // not enough bytes in the buffer, resize
                    case WinError.ERROR_INSUFFICIENT_BUFFER:
                    {
                        _buffer = new Memory(pnMinNumberOfBytesNeeded.getValue());

                        if( !Advapi32.INSTANCE.ReadEventLog(reader,
                                readFlags, 0,
                                _buffer, (int) _buffer.size(), pnBytesRead,
                                pnMinNumberOfBytesNeeded) )
                        {
                            throw new Win32Exception(
                                    Kernel32.INSTANCE.GetLastError());
                        }

                        rc = Kernel32.INSTANCE.GetLastError();
                    }
                    break;

                    case WinError.ERROR_HANDLE_EOF:
                    {
                        LOGGER.debug(String.format("ReadEventLog() : Handle EOF at %d : GetLastError() %d ( 0x%08X )",
                                i, rc, rc & 0xFFFFFFFF));
                    }
                    break;
                }
            }

            if( rc != WinError.ERROR_SUCCESS )
            {
                break;
            }

            Advapi32Util.EventLogRecord record = new Advapi32Util.EventLogRecord(_buffer);
            String computerName = null;

            // Get the computer name
            {
                Pointer pevlr = _buffer;
                WinNT.EVENTLOGRECORD record2 = new WinNT.EVENTLOGRECORD(pevlr);

                ByteBuffer names = pevlr.getByteBuffer(record2.size(),
                        (record2.UserSidLength.intValue() != 0
                                ? record2.UserSidOffset.intValue()
                                    : record2.StringOffset.intValue()) - record2.size());

                names.position(0);
                CharBuffer namesBuf = names.asCharBuffer();
                String[] splits = namesBuf.toString().split("\0");
                computerName = splits[1];
            }

            if( offSet > 0 && i < offSet )
            {
                LOGGER.debug(String.format("Skipping entry number %d", i));
            }
            else
            {
                res.add(LCWindowsEventWrapper.from(record, computerName, source));

                if( maxReadCount > 0 && res.size() >= maxReadCount )
                {
                    break;
                }
            }
        }

        return res;
    }

    public static WindowsEventLogCheckState getState( final String setName,
                                          final String serverId,
                                          final String sourceId,
                                          final String recordId,
                                          final Integer recordPosition,
                                          final Integer recordCount,
                                          final Instant lastProcessedTimeStart) throws LogCheckException, IOException
    {
        WindowsEventLogCheckState res = null;

        LOGGER.debug(String.format("getState() : Called on %s;%s", serverId, sourceId));

        // FIXME : We're resetting the processed file list
        res = WindowsEventLogCheckState.from(UUID.randomUUID(),
                setName,
                serverId,
                sourceId,
                lastProcessedTimeStart,
                recordId,
                recordPosition,
                recordCount,
                null);

        return res;
    }

    public static Set<LCTailerResult> validateStatistics(LogCheckState state) throws LogCheckException
    {
        Set<LCTailerResult> res = EnumSet.noneOf(LCTailerResult.class);

        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(String.format("Validating Statistics :\n%s", state));
        }

        // Validate start and stop blocks are correct?
       // LogCheckState state = getState(true);
        LogFileState currFState = state.getLogFile();
        if( currFState != null )
        {
            try
            {
                res.addAll( LogFileState.validateFileBlocks(currFState, null, true) );
            }
            catch( LogCheckException ex )
            {
                LOGGER.warn("Error validating file block", ex);

                res.add(LCTailerResult.VALIDATION_ERROR);
            }

            if( res.contains( LCTailerResult.SUCCESS ) == false)
            {
                LOGGER.debug("Log Check File Block VALIDATION_FAIL or VALIDATION_ERROR.");
            }
        }

        LOGGER.debug(String.format("Log Check File Block Validation result is %s", res));

        return res;
    }

}
