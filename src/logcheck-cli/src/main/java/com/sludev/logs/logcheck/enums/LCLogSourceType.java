package com.sludev.logs.logcheck.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Kervin on 8/4/2016.
 */
public enum LCLogSourceType
{
    NONE,
    FILE_LOCAL,
    WINDOWS_EVENT,
    SYSLOG;

    public static LCLogSourceType from(String type)
    {
        LCLogSourceType res;

        res = LCLogSourceType.valueOf(
                StringUtils.replace(
                    StringUtils.upperCase(
                        StringUtils.trim(type)), "-", "_"));

        return res;
    }
}
