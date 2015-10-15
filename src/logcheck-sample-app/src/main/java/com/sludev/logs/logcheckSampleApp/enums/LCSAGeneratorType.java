package com.sludev.logs.logcheckSampleApp.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by kervin on 2015-10-14.
 */
public enum LCSAGeneratorType
{
    NONE,
    RANDOMLINE;

    public static LCSAGeneratorType from(String s)
    {
        LCSAGeneratorType res = LCSAGeneratorType.valueOf(StringUtils.upperCase(s));

        return res;
    }
}
