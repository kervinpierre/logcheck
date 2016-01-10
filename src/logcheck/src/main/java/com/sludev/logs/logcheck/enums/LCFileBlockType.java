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

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2015-12-28.
 */
public enum LCFileBlockType
{
    NONE,
    ALL,
    FIRSTBLOCK,
    LASTBLOCK;

    private static final Logger LOGGER
            = LogManager.getLogger(LCFileBlockType.class);

    public static LCFileBlockType from(String type) throws LogCheckException
    {
        LCFileBlockType res = null;

        try
        {
            res = LCFileBlockType.valueOf(StringUtils.upperCase(StringUtils.trim(type)));
        }
        catch( IllegalArgumentException ex )
        {
            String errMsg = String.format("Invalid type '%s'", type);

            LOGGER.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }

        return res;
    }
}
