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
 */

package com.sludev.logs.logcheck.enums;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2015-11-26.
 */
public enum LCLogEntryStoreType
{
    NONE,
    CONSOLE,
    SIMPLEFILE,
    ELASTICSEARCH;


    public static Logger log
            = LogManager.getLogger(LCLogEntryStoreType.class);

    public static LCLogEntryStoreType from( String s )
    {
        LCLogEntryStoreType res = null;

        try
        {
            res = LCLogEntryStoreType.valueOf(
                    StringUtils.upperCase(
                            StringUtils.trim(
                                    StringUtils.replace(s, "-", "_"))));
        }
        catch(Exception ex)
        {
            log.debug(String.format("Invalid LogCheck LogEntryStore type '%s'", s), ex);
        }

        return res;
    }
}
