package com.sludev.logs.logcheck.config.entities;

import java.time.Instant;

/**
 * Created by Administrator on 8/29/2016.
 */
public class LogCheckStateStatusBase
{
    private final Instant m_processedStamp;
    private final Boolean m_processed;

    public Boolean isProcessed()
    {
        return m_processed;
    }

    public Instant getProcessedStamp()
    {
        return m_processedStamp;
    }

    protected LogCheckStateStatusBase( final Instant processedStamp,
                                      final Boolean processed )
    {
        this.m_processedStamp = processedStamp;
        this.m_processed = processed;
    }

    public static LogCheckStateStatusBase from( final Instant processedStamp,
                                                final Boolean processed )
    {
        LogCheckStateStatusBase res = new LogCheckStateStatusBase(processedStamp, processed);

        return res;
    }
}
