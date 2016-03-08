/*
 *  SLU Dev Inc. CONFIDENTIAL
 *  DO NOT COPY
 * 
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 *  the property of SLU Dev Inc. and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to SLU Dev Inc. and its suppliers and
 *  may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.enums;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

/**
 *
 * @author Kervin Pierre <info@sludev.com>
 */
public enum FSSVerbosityEnum
{
    NONE,
    ALL,
    MINIMUM,
    MAXIMUM,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public static FSSVerbosityEnum from(String verbose)
    {
        FSSVerbosityEnum res = NONE;

        if( StringUtils.isNoneBlank(verbose) )
        {
            try
            {
                res = FSSVerbosityEnum.valueOf(
                        StringUtils.upperCase(
                                StringUtils.trim(verbose)));
            }
            catch( Exception ex )
            {
                ;
            }
        }

        return res;
    }

    public static Level toLevel(FSSVerbosityEnum verbosityEnum)
    {
        Level res = Level.OFF;

        if( verbosityEnum == null )
        {
            return Level.OFF;
        }

        switch( verbosityEnum )
        {
            case NONE:
                res = Level.OFF;
                break;

            case ALL:
                res = Level.ALL;
                break;

            case MINIMUM:
                res = Level.ERROR;
                break;

            case MAXIMUM:
                res = Level.DEBUG;
                break;

            case DEBUG:
                res = Level.DEBUG;
                break;

            case INFO:
                res = Level.INFO;
                break;

            case WARN:
                res = Level.WARN;
                break;

            case ERROR:
                res = Level.ERROR;
                break;
        }

        return res;
    }
}
