/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2016] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * Signal various testing features and modes across the application.
 *
 * Created by kervin on 2016-01-11.
 */
public enum LCDebugFlag
{
    NONE,

    /**
     * Used during debugging to signal that the logs being tailed
     * are from the Log Check Sample Application.  This means we
     * can test the logs being tailed in certain ways.
     */
    LOG_SOURCE_LC_APP;

    public static LCDebugFlag from(String flag)
    {
        LCDebugFlag res;

        res = LCDebugFlag.valueOf(StringUtils.upperCase(StringUtils.trim(flag)));

        return res;
    }
}
