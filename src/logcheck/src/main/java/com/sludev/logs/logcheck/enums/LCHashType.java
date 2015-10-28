package com.sludev.logs.logcheck.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by kervin on 10/27/2015.
 */
public enum LCHashType
{
    NONE,
    SHA1,
    SHA2,
    MD5;

    public static LCHashType from(String s)
    {
        LCHashType res = LCHashType.valueOf(StringUtils.upperCase(
                StringUtils.trim(s)));

        return res;
    }
}
