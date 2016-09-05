package com.sludev.logs.logcheck.config.entities.impl;

import com.sludev.logs.logcheck.config.entities.LogCheckStateStatusBase;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.Set;

/**
 * Created by Administrator on 8/29/2016.
 */
public class WindowsEventSourceStatus extends LogCheckStateStatusBase
{
    private static final Logger LOGGER
            = LogManager.getLogger(WindowsEventSourceStatus.class);

    private final String m_serverId;
    private final String m_sourceId;
    private final String m_entryId;
    private final Integer m_recordNumber;
    private final Integer m_recordCount;

    public String getServerId()
    {
        return m_serverId;
    }

    public String getSourceId()
    {
        return m_sourceId;
    }

    public String getEntryId()
    {
        return m_entryId;
    }

    public Integer getRecordNumber()
    {
        return m_recordNumber;
    }

    public Integer getRecordCount()
    {
        return m_recordCount;
    }

    private WindowsEventSourceStatus(  final String serverId,
                                        final String sourceId,
                                        final String entryId,
                                        final Integer recordNumber,
                                        final Integer recordCount,
                                        final Instant processedStamp,
                                        final Boolean processed )
    {
        super(processedStamp, processed);

        if( StringUtils.isNoneBlank(serverId) )
        {
            this.m_serverId = serverId;
        }
        else
        {
            this.m_serverId = null;
        }

        if( StringUtils.isNoneBlank(sourceId) )
        {
            this.m_sourceId = sourceId;
        }
        else
        {
            this.m_sourceId = null;
        }

        if( StringUtils.isNoneBlank(entryId) )
        {
            this.m_entryId = entryId;
        }
        else
        {
            this.m_entryId = null;
        }

        if( recordNumber != null && recordNumber >= 0 )
        {
            this.m_recordNumber = recordNumber;
        }
        else
        {
            this.m_recordNumber = null;
        }

        if( recordCount != null && recordCount >= 0 )
        {
            this.m_recordCount = recordCount;
        }
        else
        {
            this.m_recordCount = null;
        }
    }

    public static WindowsEventSourceStatus from (  final String serverId,
                                                    final String sourceId,
                                                    final String entryId,
                                                    final Integer recordPosition,
                                                    final Integer recordCount,
                                                    final Instant processedStamp,
                                                    final Boolean processed )
    {
        WindowsEventSourceStatus res = new WindowsEventSourceStatus(serverId,
                sourceId, entryId, recordPosition, recordCount,
                processedStamp, processed);

        return res;
    }


    public static WindowsEventSourceStatus from (  final String serverId,
                                                    final String sourceId,
                                                    final String entryId,
                                                    final String recordPositionStr,
                                                    final String recordCountStr,
                                                   final String processedStampStr,
                                                   final Boolean processed)
            throws LogCheckException
    {
        Integer recordPosition = null;
        Integer recordCount = null;
        Instant processedStamp = null;

        recordPosition = getRecordNumber(recordPositionStr);
        recordCount = getRecordCount(recordCountStr);


        try
        {
            processedStamp = Instant.parse(processedStampStr);
        }
        catch( Exception ex )
        {
            LOGGER.debug(String.format("Error parsing '%s'", processedStampStr), ex);
        }

        WindowsEventSourceStatus res = new WindowsEventSourceStatus(serverId,
                sourceId, entryId, recordPosition, recordCount, processedStamp, processed);

        return res;
    }

    public static Integer getRecordNumber( final String rp)
            throws LogCheckException
    {
        Integer res = null;

        if( StringUtils.isNoneBlank(rp) )
        {
            try
            {
                res = Integer.parseInt(rp);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Invalid number for Record Position '%s'", rp);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        return res;
    }

    public static Integer getRecordCount(final String rc)
            throws LogCheckException
    {
        Integer res = null;

        if( StringUtils.isNoneBlank(rc) )
        {
            try
            {
                res = Integer.parseInt(rc);
            }
            catch( NumberFormatException ex )
            {
                String errMsg = String.format("Invalid number for Record Count '%s'", rc);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        return res;
    }

    /**
     * Validates a Windows Event Source Status.
     *
     * This can't be made very useful/performant without a working Event Log SEEK function.  And currently
     * we can't rely on EVENTLOG_SEEK_READ due to KB 177199
     * @param status
     * @return
     * @throws LogCheckException
     */
    public static Set<LCTailerResult> validateStatus( WindowsEventSourceStatus status )
            throws LogCheckException
    {
        Set<LCTailerResult> res = null;

        int readFlags = WinNT.EVENTLOG_SEQUENTIAL_READ | WinNT.EVENTLOG_FORWARDS_READ;
        Memory _buffer = new Memory(1024 * 64);
        IntByReference pnBytesRead = new IntByReference();
        IntByReference pnMinNumberOfBytesNeeded = new IntByReference();
        WinNT.HANDLE reader = Advapi32.INSTANCE.OpenEventLog(status.getServerId(), status.getSourceId());

        // Validate count
        IntByReference pnCount = new IntByReference();
        int lastRecCount = 0;
        if( Advapi32.INSTANCE.GetNumberOfEventLogRecords(reader, pnCount) )
        {
            lastRecCount = pnCount.getValue();

            if( status.getRecordCount() == null )
            {
                ;
            }
            else
            {
                if( status.getRecordCount() > lastRecCount )
                {
                    // This isn't necessarily an error, the logs could have been
                    // truncated since our last read
                    LOGGER.warn(String.format("Validate expects count of %d but found %d",
                            status.getRecordCount(), lastRecCount));
                }
            }
        }

        return res;
    }
}
