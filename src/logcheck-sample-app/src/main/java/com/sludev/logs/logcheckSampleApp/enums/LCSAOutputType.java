package com.sludev.logs.logcheckSampleApp.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by kervin on 2015-10-14.
 */
public enum LCSAOutputType
{
    NONE,
    BUFFEREDWRITER,
    FILECHANNEL;

    public static LCSAOutputType from(String t)
    {
        LCSAOutputType res = LCSAOutputType.valueOf(StringUtils.upperCase(t));

        return res;
    }
}
