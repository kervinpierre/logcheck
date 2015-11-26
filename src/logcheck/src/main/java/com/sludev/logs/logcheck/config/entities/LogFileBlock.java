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
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
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

    private final String name;
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

    public String getName()
    {
        return name;
    }

    private LogFileBlock(final String name,
                         final Long startPosition,
                         final Integer size,
                         final LCHashType hashType,
                         final byte[] hashDigest)
    {
        this.name = name;
        this.startPosition = startPosition;
        this.size = size;
        this.hashType = hashType;
        this.hashDigest = hashDigest;
    }

    public static LogFileBlock from(final String name,
                                    final Long startPosition,
                                    final Integer size,
                                    final LCHashType hashType,
                                    final byte[] hashDigest)
    {
        LogFileBlock res = new LogFileBlock(name,
                startPosition,
                size,
                hashType,
                hashDigest);

        return res;
    }

    public static LogFileBlock from(final String name,
                                    final String startPositionStr,
                                    final String sizeStr,
                                    final String hashTypeStr,
                                    final String hashDigestStr) throws LogCheckException
    {
        Long startPosition = null;
        Integer size = null;
        LCHashType hashType = null;
        byte[] hashDigest = null;

        if( StringUtils.isNoneBlank(startPositionStr) )
        {
            try
            {
                startPosition = Long.parseLong(startPositionStr);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid integer for Start Position '%s'", startPositionStr);
                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(sizeStr) )
        {
            try
            {
                size = Integer.parseInt(sizeStr);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid integer size '%s'", sizeStr);
                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(hashTypeStr) )
        {
            try
            {
                hashType = LCHashType.from(hashTypeStr);
            }
            catch( IllegalArgumentException ex )
            {
                String errMsg = String.format("Invalid Hash Type '%s'", hashTypeStr);
                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        if( StringUtils.isNoneBlank(hashDigestStr) )
        {
            try
            {
                hashDigest = Hex.decodeHex(hashDigestStr.toCharArray());
            }
            catch( DecoderException  ex )
            {
                String errMsg = String.format("Invalid Hash Digest '%s'", hashDigestStr);
                log.debug(errMsg, ex);

                throw new LogCheckException(errMsg, ex);
            }
        }

        LogFileBlock res = new LogFileBlock(name,
                startPosition,
                size,
                hashType,
                hashDigest);

        return res;
    }

    /*
    ** Generate the hash value
     */
    public static LogFileBlock from(final String name,
                                    final Long startPosition,
                                    final LCHashType hashType,
                                    final byte[] block) throws LogCheckException
    {
        if( block == null || block.length < 1 )
        {
            throw new LogCheckException("Invalid block");
        }

        if( log.isDebugEnabled() )
        {
            if( block.length > LogCheckConstants.MAX_ID_BLOCK_SIZE )
            {
                log.debug(String.format("Block size is greater than ID max. Size = %d",
                        block.length));
            }
            else
            {
                log.debug(String.format("Block size %d : \n=======\n%s\n=======\n",
                        block.length, new String(block)));
            }
        }

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
                    log.debug("Failed getting SHA-1 Hash", ex);
                }
                break;

            case SHA2:
                try
                {
                    md = MessageDigest.getInstance("SHA-256");
                }
                catch( NoSuchAlgorithmException ex )
                {
                    log.debug("Failed getting SHA-256 Hash", ex);
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

        if( log.isDebugEnabled() )
        {
            log.debug(String.format("Hash '%s' : %x", hashType, new java.math.BigInteger(1, value)));
        }

        res = from(name,
                startPosition,
                block.length,
                hashType,
                value);

        return res;
    }

    public static LogFileBlock from(final String name,
                                    final Path fl,
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

        LogFileBlock res = LogFileBlock.from(name,
                pos,
                hs,
                buffer.array());

        return res;
    }
}
