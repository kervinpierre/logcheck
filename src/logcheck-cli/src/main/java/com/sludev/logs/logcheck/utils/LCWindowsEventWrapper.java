package com.sludev.logs.logcheck.utils;

import com.sun.jna.platform.win32.Advapi32Util;

/**
 * Created by Administrator on 8/25/2016.
 */
public final class LCWindowsEventWrapper
{
    private final Advapi32Util.EventLogRecord m_event;
    private final String m_computerName;
    private final String m_channel;

    public Advapi32Util.EventLogRecord getEvent()
    {
        return m_event;
    }

    public String getComputerName()
    {
        return m_computerName;
    }

    public String getChannel()
    {
        return m_channel;
    }

    private LCWindowsEventWrapper( final Advapi32Util.EventLogRecord event,
                                   final String computerName,
                                   final String channel )
    {
        m_event = event;
        m_computerName = computerName;
        m_channel = channel;
    }

    public static LCWindowsEventWrapper from( final Advapi32Util.EventLogRecord event,
                                              final String computerName,
                                              final String channel )
    {
        LCWindowsEventWrapper res = new LCWindowsEventWrapper(event, computerName, channel);

        return res;
    }
}
