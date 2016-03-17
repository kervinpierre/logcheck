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

package com.sludev.logs.logcheck.store.impl;

import com.sludev.logs.logcheck.config.entities.LogEntryVO;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.store.ILogEntryStore;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Store Log Entries in a simple local file.
 *
 * Created by kervin on 2015-11-26.
 */
public final class LogEntrySimpleFile implements ILogEntryStore
{
    private static final Logger log = LogManager.getLogger(LogEntrySimpleFile.class);

    private final Path logOutputFile;
    private final Boolean reOpenFile;

    // Mutable
    private FileChannel file;

    private LogEntrySimpleFile(final Path logOutputFile,
                               final Boolean reOpenFile)
    {
        this.logOutputFile = logOutputFile;
        this.reOpenFile = reOpenFile;

        file = null;
    }

    public static LogEntrySimpleFile from(final Path logOutputFile,
                               final Boolean reOpenFile)
    {
        LogEntrySimpleFile res = new LogEntrySimpleFile(logOutputFile,
                reOpenFile);

        return res;
    }

    @Override
    public void init() throws LogCheckException
    {
        if( BooleanUtils.isNotFalse(reOpenFile) )
        {
            return;
        }

        if( logOutputFile == null )
        {
            throw new LogCheckException("Log Output File cannot be null.");
        }

        try
        {
            file = FileChannel.open(logOutputFile,
                                        StandardOpenOption.CREATE,
                                        StandardOpenOption.APPEND);
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error opening log file '%s'", logOutputFile);

            log.debug(errMsg, ex);

            throw new LogCheckException(errMsg, ex);
        }
    }

    @Override
    public void destroy() throws LogCheckException
    {
        if( file != null && file.isOpen() )
        {
            try
            {
                file.close();
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error closing log file '%s'", logOutputFile);

                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }
    }

    @Override
    public LCResultStatus testConnection() throws LogCheckException
    {
        return null;
    }

    @Override
    public LogCheckResult putValueObj(LogEntryVO entryVO) throws InterruptedException, LogCheckException
    {
        LogCheckResult res
                = LogCheckResult.from(LCResultStatus.SUCCESS);
        FileChannel currFile = null;

        if( BooleanUtils.isNotFalse(reOpenFile) )
        {
            try
            {
                currFile = FileChannel.open(logOutputFile, StandardOpenOption.CREATE);
            }
            catch( IOException ex )
            {
                String errMsg = String.format("Error opening log file '%s'", logOutputFile);

                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }
        else
        {
            currFile = file;
        }

        String outStr = String.format("%s\n", LogEntryVO.toJSON(entryVO));
        ByteBuffer buff = ByteBuffer.wrap(outStr.getBytes());

        try
        {
            currFile.write(buff);
        }
        catch( IOException ex )
        {
            log.debug("Error writing to log file", ex);
        }
        finally
        {
            if( BooleanUtils.isNotFalse(reOpenFile) )
            {
                try
                {
                    currFile.close();
                }
                catch( IOException ex )
                {
                    log.debug("Error closing log file", ex);
                }
            }
        }

        return res;
    }
}
