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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kervin on 2015-12-07.
 */
public enum LCFileRegexComponent
{
    NONE,
    FILENAME_PREFIX,
    INTEGER_INC,
    TIMESTAMP;

    public static LCFileRegexComponent from(String comp)
    {
        LCFileRegexComponent res;

        res = LCFileRegexComponent.valueOf(StringUtils.upperCase(StringUtils.trim(comp)));

        return res;
    }

    public static List<LCFileRegexComponent> from( final String[] tailerBackupLogNameCompStrs )
    {
        List<LCFileRegexComponent> res = null;

        if(tailerBackupLogNameCompStrs != null )
        {
            res = new ArrayList<>(10);

            for( String nameComp : tailerBackupLogNameCompStrs )
            {
                if( StringUtils.isNoneBlank(nameComp) )
                {
                    String tempStr = nameComp.replace('-', '_')
                            .toUpperCase()
                            .trim();

                    res.add(LCFileRegexComponent.from(tempStr));
                }
            }
        }

        return res;
    }
}
