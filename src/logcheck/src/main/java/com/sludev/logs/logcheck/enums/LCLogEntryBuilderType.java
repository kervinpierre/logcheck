package com.sludev.logs.logcheck.enums;

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
    MULTILINE_DELIMITED;

    public static Logger log
            = LogManager.getLogger(LCLogEntryBuilderType.class);

    public static LCLogEntryBuilderType from( String s )
    {
        LCLogEntryBuilderType res = null;

        try
        {
            res = LCLogEntryBuilderType.valueOf(
                    StringUtils.upperCase(
                            StringUtils.trim(
                                    StringUtils.replace(
                                            StringUtils.remove(s, "-"), " ", "_"))));
        }
        catch(Exception ex)
        {
            log.debug(String.format("Invalid LogCheck LogEntryBuilder type '%s'", s), ex);
        }

        return res;
    }
}
