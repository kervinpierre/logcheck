package com.sludev.logs.logcheck.config.entities.impl;

import com.sludev.logs.logcheck.config.entities.LogCheckError;
import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.config.entities.LogCheckStateStatusBase;
import com.sludev.logs.logcheck.enums.LCLogCheckStateType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static Deque<LogCheckStateStatusBase> mergeStatuses(Deque<LogCheckStateStatusBase> a,
                                                               Deque<LogCheckStateStatusBase> b)
    {
        Deque<LogCheckStateStatusBase> res = null;

        if( a == null || a.isEmpty() )
        {
            if( b == null || b.isEmpty() )
            {
                return null;
            }
            else
            {
                res = new ArrayDeque<>(b);
                return res;
            }
        }
        else
        {
            if( b == null || b.isEmpty() )
            {
                res = new ArrayDeque<>(a);
                return res;
            }
        }

        Map<Pair<String, String>, WindowsEventSourceStatus> im = new HashMap<>();
        Stream.concat(a.stream(), b.stream())
                .map( i -> (WindowsEventSourceStatus)i )
                .forEach( i -> im.put(Pair.of(i.getServerId(), i.getSourceId()), i));

        res = new ArrayDeque<>(im.values());

        return res;
    }
}
