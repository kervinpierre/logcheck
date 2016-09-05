package com.sludev.logs.logcheck.config.entities.impl;

import com.sludev.logs.logcheck.config.entities.LogCheckError;
import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.config.entities.LogCheckStateStatusBase;
import com.sludev.logs.logcheck.enums.LCLogCheckStateType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;

import java.time.Instant;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 8/17/2016.
 */
public class WindowsEventLogCheckState extends LogCheckStateBase
{
    private WindowsEventLogCheckState( final UUID id,
                                       final String setName,
                                       final Instant saveDate,
                                       final List<LogCheckError> errors,
                                       final Deque<LogCheckStateStatusBase> completedStatuses)
    {
        super(id,
                LCLogCheckStateType.WINDOWS_EVENT_STATE,
                setName,
                saveDate,
                errors,
                completedStatuses);
    }

    public static WindowsEventLogCheckState from(final UUID id,
                                                 final String setName,
                                                 final Instant saveDate,
                                                 final List<LogCheckError> errors,
                                                 final Deque<LogCheckStateStatusBase> completedStatuses )
    {
        WindowsEventLogCheckState res = new WindowsEventLogCheckState(id,
                                                setName,
                                                saveDate,
                                                errors,
                                                completedStatuses);

        return res;
    }

    public static WindowsEventLogCheckState from( final String idStr,
                                      final String setName,
                                      final String saveDateStr,
                                      final List<LogCheckError> errors,
                                      final Deque<LogCheckStateStatusBase> completedStatuses) throws LogCheckException
    {
        Instant saveDate = null;
        UUID id = null;


        saveDate = getSaveDate(saveDateStr);
        id = getId(idStr);


        WindowsEventLogCheckState res = new WindowsEventLogCheckState(
                id,
                setName,
                saveDate,
                errors,
                completedStatuses);

        return res;
    }
}
