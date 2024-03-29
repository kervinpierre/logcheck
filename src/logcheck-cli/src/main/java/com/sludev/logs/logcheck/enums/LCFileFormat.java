/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
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
 *
 */

package com.sludev.logs.logcheck.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2015-08-24.
 */
public enum LCFileFormat
{
    NONE,
    LCCONFIG,
    LCSTATE,
    LCDDUPE;

    private static final Logger log = LogManager.getLogger(LCFileFormat.class);

    public static String getSchema( LCFileFormat format )
    {
        String res;
        switch( format )
        {
            case LCCONFIG:
                res = "logcheckconfig.xsd";
                break;

            case LCSTATE:
                res = "logcheckstate.xsd";
                break;

            case LCDDUPE:
                res = "logcheckdedupelog.xsd";
                break;

            default:
                res = "";
        }

        return res;
    }
}
