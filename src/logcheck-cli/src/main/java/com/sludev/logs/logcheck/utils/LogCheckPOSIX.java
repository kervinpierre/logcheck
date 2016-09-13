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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import jnr.constants.platform.Errno;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import jnr.posix.POSIXHandler;

/**
 *
 * @author kervin
 */
public class LogCheckPOSIX
{

    public static POSIX createPOSIX()
    {
        POSIX currPosix = POSIXFactory.getPOSIX(new POSIXHandler()
        {
            @Override
            public void error(Errno errno, String s)
            {
            }

            @Override
            public void unimplementedError(String s)
            {
            }

            @Override
            public void warn(WARNING_ID warning_id, String s, Object... objects)
            {
            }

            @Override
            public boolean isVerbose()
            {
                return false;
            }

            @Override
            public File getCurrentWorkingDirectory()
            {
                return new File(".");
            }

            @Override
            public String[] getEnv()
            {
                return new String[0];
            }

            @Override
            public InputStream getInputStream()
            {
                return System.in;
            }

            @Override
            public PrintStream getOutputStream()
            {
                return System.out;
            }

            @Override
            public int getPID()
            {
                return 0;
            }

            @Override
            public PrintStream getErrorStream()
            {
                return System.err;
            }

            @Override
            public void error(Errno errno, String string, String string1)
            {
                ;
            }

        }, true);
        
        return currPosix;
    }
}
