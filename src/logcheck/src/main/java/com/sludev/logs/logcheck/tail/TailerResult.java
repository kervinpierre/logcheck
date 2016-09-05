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

package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.config.entities.LogCheckStateBase;
import com.sludev.logs.logcheck.enums.LCTailerResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kervin on 2016-01-04.
 */
public final class TailerResult
{
    private static final Logger LOGGER = LogManager.getLogger(TailerResult.class);

    private final Set<LCTailerResult> m_result;
    private final LogCheckStateBase m_state;

    public Set<LCTailerResult> getResultSet()
    {
        return m_result;
    }

    public LogCheckStateBase getState()
    {
        return m_state;
    }

    private TailerResult( final Set<LCTailerResult> result,
                          final LogCheckStateBase state)
    {
        if( result == null )
        {
            this.m_result = new HashSet<>(5);
        }
        else
        {
            this.m_result = result;
        }

        this.m_state = state;
    }

    public static TailerResult from( final Set<LCTailerResult> result,
                                     final LogCheckStateBase state)
    {
        TailerResult res = new TailerResult(result,
                                                    state);

        return res;
    }

    public String toString()
    {
        StringBuilder res = new StringBuilder(100);

        String msg = "";
        if( m_result != null )
        {
            for( LCTailerResult lcres : m_result )
            {
                msg += lcres.toString() + ", ";
            }
        }

        res.append("\nTailerResult\n{\n");
        res.append(String.format("  result : '%s'\n", msg));
        res.append("}\n");

        return res.toString();
    }
}
