package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.enums.LCLogCheckStateType;
import com.sludev.logs.logcheck.enums.LCLogSourceType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 8/17/2016.
 */
public abstract class LogCheckStateBase
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckStateBase.class);

    private final LCLogCheckStateType m_type;

    private final UUID m_id;
    private final String m_setName;
    private final Instant m_saveDate;
    private final List<LogCheckError> m_errors;
    private final Deque<LogCheckStateStatusBase> m_completedStatuses;

    // MUTABLE
    private volatile boolean m_pendingSave = false;

    public boolean isPendingSave()
    {
        return m_pendingSave;
    }

    public void setPendingSave(boolean save)
    {
        m_pendingSave = save;
    }

    public UUID getId()
    {
        return m_id;
    }

    public static UUID getId(String idStr)
            throws LogCheckException
    {
        UUID res = null;

        if( StringUtils.isNoneBlank(idStr) )
        {
            try
            {
                res = UUID.fromString(idStr);
            }
            catch( DateTimeParseException ex )
            {
                String errMsg = String.format("Invalid ID '%s'", idStr);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        return res;
    }

    public Deque<LogCheckStateStatusBase> getCompletedStatuses()
    {
        return m_completedStatuses;
    }

    public String getSetName()
    {
        return m_setName;
    }

    public Instant getSaveDate()
    {
        return m_saveDate;
    }

    public LCLogCheckStateType getLCType()
    {
        return m_type;
    }

    public static Instant getSaveDate(String sd)
            throws LogCheckException
    {
        Instant res = null;

        if( StringUtils.isNoneBlank(sd) )
        {
            try
            {
                res = Instant.parse(sd);
            }
            catch( DateTimeParseException ex )
            {
                String errMsg = String.format("Invalid Timestamp Save Date'%s'", sd);
                LOGGER.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        return res;
    }


    public List<LogCheckError> getErrors()
    {
        return m_errors;
    }

    protected LogCheckStateBase( final UUID id,
                                 final LCLogCheckStateType type,
                                 final String setName,
                                 final Instant saveDate,
                                 final List<LogCheckError> errors,
                                 final Deque<LogCheckStateStatusBase> statuses)
    {

        if( id != null )
        {
            this.m_id = id;
        }
        else
        {
            this.m_id = null;
        }

        if( type != null )
        {
            this.m_type = type;
        }
        else
        {
            this.m_type = null;
        }

        if( StringUtils.isNoneBlank(setName) )
        {
            this.m_setName = setName;
        }
        else
        {
            this.m_setName = null;
        }

        if( saveDate != null )
        {
            this.m_saveDate = saveDate;
        }
        else
        {
            this.m_saveDate = null;
        }

        if( errors != null )
        {
            this.m_errors = errors;
        }
        else
        {
            this.m_errors = new ArrayList<>();
        }

        if( statuses != null )
        {
            this.m_completedStatuses = statuses;
        }
        else
        {
            this.m_completedStatuses = new ArrayDeque<>();
        }
    }

    @Override
    public String toString()
    {
        StringBuilder res = new StringBuilder(100);

        res.append("LogCheckState :\n");
        res.append(String.format("    Set Name  : %s\n", m_setName ));
        res.append(String.format("    Save Date : %s\n", m_saveDate ));
        //res.append(String.format("    Log File State: %s\n", m_logFile));

        return res.toString();
    }
}
