package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 8/17/2016.
 */
public abstract class LogCheckStateBase
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogCheckStateBase.class);

    private final UUID m_id;
    private final String m_setName;
    private final Instant m_saveDate;
    private final String m_serverId;
    private final String m_sourceId;
    private final String m_recordId;
    private final Integer m_recordPosition;
    private final Integer m_recordCount;
    private final List<LogCheckError> m_errors;

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

    public String getSetName()
    {
        return m_setName;
    }

    public String getServerId()
    {
        return m_serverId;
    }

    public String getSourceId()
    {
        return m_sourceId;
    }

    public Instant getSaveDate()
    {
        return m_saveDate;
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

    public String getRecordId()
    {
        return m_recordId;
    }

    public Integer getRecordPosition()
    {
        return m_recordPosition;
    }

    public static Integer getRecordPosition(final String rp)
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

    public Integer getRecordCount()
    {
        return m_recordCount;
    }

    public List<LogCheckError> getErrors()
    {
        return m_errors;
    }

    protected LogCheckStateBase( final UUID id,
                                 final String setName,
                                 final String serverId,
                                 final String sourceId,
                                 final Instant saveDate,
                                 final String recordId,
                                 final Integer recordPosition,
                                 final Integer recordCount,
                                 final List<LogCheckError> errors)
    {
        if( id != null )
        {
            this.m_id = id;
        }
        else
        {
            this.m_id = null;
        }

        if( StringUtils.isNoneBlank(setName) )
        {
            this.m_setName = setName;
        }
        else
        {
            this.m_setName = null;
        }

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

        if( saveDate != null )
        {
            this.m_saveDate = saveDate;
        }
        else
        {
            this.m_saveDate = null;
        }

        if( StringUtils.isNoneBlank(recordId) )
        {
            this.m_recordId = recordId;
        }
        else
        {
            this.m_recordId = null;
        }

        if( recordPosition != null && recordPosition >= 0 )
        {
            this.m_recordPosition = recordPosition;
        }
        else
        {
            this.m_recordPosition = null;
        }

        if( recordCount != null && recordCount >= 0 )
        {
            this.m_recordCount = recordCount;
        }
        else
        {
            this.m_recordCount = null;
        }

        if( errors != null )
        {
            this.m_errors = errors;
        }
        else
        {
            this.m_errors = new ArrayList<>();
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
