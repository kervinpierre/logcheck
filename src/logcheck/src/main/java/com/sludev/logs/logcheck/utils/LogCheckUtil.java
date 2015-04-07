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
package com.sludev.logs.logcheck.utils;

/**
 *
 * @author kervin
 */
public class LogCheckUtil
{
    public static void displayVersion()
    {
        System.out.println( String.format(
                "\n%s : Version %s: Build [%s]\n\u00a9 SLU Dev Inc.  All Rights Reserved.\n", 
                    LogCheckConstants.PROD_LONG_NAME,
                    LogCheckConstants.PROD_VERSION,
                    LogCheckConstants.PROD_BUILD) );
    }
}
