/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.enums;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author kervin
 */
public enum LCLogLevel
{
    NONE,
    ALL,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    AUDIT_FAILURE,
    AUDIT_SUCCESS;

    public static LCLogLevel from( String s )
    {
        LCLogLevel res;

        String val = StringUtils.upperCase(
                        StringUtils.trim(s));

        switch( val )
        {
            case "INFORMATIONAL":
                res = INFO;
                break;

            case "WARNING":
                res = WARN;
                break;

            case "AUDITFAILURE":
                res = AUDIT_FAILURE;
                break;

            case "AUDITSUCCESS":
                res = AUDIT_SUCCESS;
                break;

            default:
                res = LCLogLevel.valueOf(val);
                break;
        }

        return res;
    }
}
