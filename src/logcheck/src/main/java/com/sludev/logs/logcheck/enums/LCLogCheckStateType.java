package com.sludev.logs.logcheck.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Kervin on 8/4/2016.
 */
public enum LCLogCheckStateType
{
    NONE,
    FILE_STATE,
    WINDOWS_EVENT_STATE,
    SYSLOG_STATE;

    public static LCLogCheckStateType from( String type)
    {
        LCLogCheckStateType res;

        res = LCLogCheckStateType.valueOf(
                StringUtils.replace(
                    StringUtils.upperCase(
                        StringUtils.trim(type)), "-", "_"));

        return res;
    }

    public static String XMLName( LCLogCheckStateType state )
    {
        String res = null;

        if( state != null )
        {
            res = StringUtils.lowerCase(
                    StringUtils.replace(state.toString(), "_", "-"));
        }

        return res;
    }
}
