package com.sludev.logs.logcheck.enums;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 10/23/2015.
 */
public enum LCLogEntryBuilderType
{
    NONE,
    SINGLELINE,
    NCSA_COMMON_LOG,
    WINDOWS_EVENT,
    MULTILINE_DELIMITED;

    public static Logger LOGGER
            = LogManager.getLogger(LCLogEntryBuilderType.class);

    public static LCLogEntryBuilderType from( String s )
            throws LogCheckException
    {
        LCLogEntryBuilderType res = null;

        try
        {
            res = LCLogEntryBuilderType.valueOf(
                    StringUtils.upperCase(
                            StringUtils.trim(
                                    StringUtils.replace(s, "-", "_"))));
        }
        catch(Exception ex)
        {
            String errMsg = String.format("Invalid LogCheck LogEntryBuilder type '%s'", s);

            LOGGER.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        return res;
    }
}
