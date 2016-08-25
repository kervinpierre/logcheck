package com.sludev.logs.logcheck.utils;

import com.sun.jna.platform.win32.Advapi32Util;

/**
 * Created by Administrator on 8/25/2016.
 */
public final class LCWindowsEventWrapper
{
    final Advapi32Util.EventLogRecord m_event;
    final String m_computerName;

    public Advapi32Util.EventLogRecord getEvent()
    {
        return m_event;
    }

    public String getComputerName()
    {
        return m_computerName;
    }

    private LCWindowsEventWrapper( final Advapi32Util.EventLogRecord event,
                                   final String computerName )
    {
        m_event = event;
        m_computerName = computerName;
    }

    public static LCWindowsEventWrapper from( final Advapi32Util.EventLogRecord event,
                                              final String computerName )
    {
        LCWindowsEventWrapper res = new LCWindowsEventWrapper(event, computerName);

        return res;
    }
}
