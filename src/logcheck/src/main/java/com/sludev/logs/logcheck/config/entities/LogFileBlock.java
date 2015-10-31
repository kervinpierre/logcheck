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
package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.enums.LCHashType;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by kervin on 10/27/2015.
 */
public final class LogFileBlock
{
    private static final Logger log
            = LogManager.getLogger(LogFileBlock.class);

    private final Long startPosition;
    private final Integer size;
    private final LCHashType hashType;
    private final byte[] hashDigest;

    public Long getStartPosition()
    {
        return startPosition;
    }

    public byte[] getHashDigest()
    {
        return hashDigest;
    }

    public LCHashType getHashType()
    {
        return hashType;
    }

    public Integer getSize()
    {
        return size;
    }

    private LogFileBlock(final Long startPosition,
                         final Integer size,
                         final LCHashType hashType,
                         final byte[] hashDigest)
    {
        this.startPosition = startPosition;
        this.size = size;
        this.hashType = hashType;
        this.hashDigest = hashDigest;
    }

    /*
    ** Generate the hash value
     */
    public static LogFileBlock from(final Long startPosition,
                                    final LCHashType hashType,
                                    final byte[] block) throws LogCheckException
    {
        LogFileBlock res;

        MessageDigest md = null;

        switch(hashType)
        {
            case SHA1:
                try
                {
                    md = MessageDigest.getInstance("SHA-1");
                }
                catch( NoSuchAlgorithmException ex )
                {
                    log.debug("", ex);
                }
                break;

            case SHA2:
                try
                {
                    md = MessageDigest.getInstance("SHA-256");
                }
                catch( NoSuchAlgorithmException ex )
                {
                    log.debug("", ex);
                }
                break;

            default:
                throw new LogCheckException("Invalid hash type");
        }

        if( md == null )
        {
            throw new LogCheckException("Invalid hash type");
        }

        md.update(block);

        byte[] value = md.digest();

        res = from(startPosition,
                block.length,
                hashType,
                value);

        return res;
    }

    public static LogFileBlock from(final Long startPosition,
                        final Integer size,
                        final LCHashType hashType,
                        final byte[] hashDigest)
    {
        LogFileBlock res = new LogFileBlock(startPosition,
                size,
                hashType,
                hashDigest);

        return res;
    }

    public static LogFileBlock from(final Path fl,
                                    final long pos,
                                    final int sz,
                                    final LCHashType hs) throws LogCheckException
    {
        FileChannel fc;

        try
        {
            if( Files.size(fl)<1)
            {
                throw new LogCheckException(
                        String.format("Empty file '%s'", fl));
            }

            if(pos < 0 || pos > Files.size(fl)-1)
            {
                throw new LogCheckException(
                        String.format("Invalid position %d for file '%s'", pos, fl));
            }
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error reading file size '%s'", fl);

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        if( sz < 0 || sz > LogCheckConstants.MAX_ID_BLOCK_SIZE)
        {
            throw new LogCheckException(
                    String.format("Invalid size %d for file '%s'", sz, fl));
        }

        try
        {
            fc = FileChannel.open(fl, StandardOpenOption.READ);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error opening file '%s'", fl);

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            fc.position(pos);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error setting file position '%s'", fl);

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        ByteBuffer buffer = ByteBuffer.allocate(sz);

        int bytesRead;

        try
        {
            bytesRead = fc.read(buffer);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error reading file '%s'", fl);

            log.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        buffer.flip();

        LogFileBlock res = LogFileBlock.from(pos, hs, buffer.array());

        return res;
    }
}
