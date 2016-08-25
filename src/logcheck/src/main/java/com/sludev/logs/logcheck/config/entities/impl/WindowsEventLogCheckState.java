package com.sludev.logs.logcheck.config.entities.impl;

import com.sludev.logs.logcheck.config.entities.LogCheckError;
import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.exceptions.LogCheckException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 8/17/2016.
 */
public class WindowsEventLogCheckState extends LogCheckStateBase
{
    private WindowsEventLogCheckState( final UUID id,
                                       final String setName,
                                       final String serverId,
                                       final String sourceId,
                                       final Instant saveDate,
                                       final String recordId,
                                       final Integer recordPosition,
                                       final Integer recordCount,
                                       final List<LogCheckError> errors )
    {
        super(id,
                setName,
                serverId,
                sourceId,
                saveDate,
                recordId,
                recordPosition,
                recordCount,
                errors);
    }

    public static WindowsEventLogCheckState from(final UUID id,
                                                 final String setName,
                                                 final String serverId,
                                                 final String sourceId,
                                                 final Instant saveDate,
                                                 final String recordId,
                                                 final Integer recordPosition,
                                                 final Integer recordCount,
                                                 final List<LogCheckError> errors )
    {
        WindowsEventLogCheckState res = new WindowsEventLogCheckState(id,
                                                setName,
                                                serverId,
                                                sourceId,
                                                saveDate,
                                                recordId,
                                                recordPosition,
                                                recordCount,
                                                errors);

        return res;
    }

    public static WindowsEventLogCheckState from( final String idStr,
                                      final String setName,
                                      final String serverId,
                                      final String sourceId,
                                      final String saveDateStr,
                                      final String recordId,
                                      final String recordPositionStr,
                                      final String recordCountStr,
                                      final List<LogCheckError> errors) throws LogCheckException
    {
        Instant saveDate = null;
        UUID id = null;
        Integer recordPosition = null;
        Integer recordCount = null;

        saveDate = getSaveDate(saveDateStr);
        id = getId(idStr);
        recordPosition = getRecordPosition(recordPositionStr);
        recordCount = getRecordCount(recordCountStr);

        WindowsEventLogCheckState res = new WindowsEventLogCheckState(
                id,
                setName,
                serverId,
                sourceId,
                saveDate,
                recordId,
                recordPosition,
                recordCount,
                errors);

        return res;
    }
}
