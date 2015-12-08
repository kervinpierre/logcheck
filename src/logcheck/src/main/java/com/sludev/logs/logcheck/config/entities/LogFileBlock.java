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
import com.sludev.logs.logcheck.exceptions.LogCheckException;
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
import java.util.Arrays;

/**
 * Represents a data block in a file on disk.
 *
 * Stores the blocks location ( start of byte number ) and also its digest.
 *
 * Created by kervin on 10/27/2015.
 */
public final class LogFileBlock
{
    private static final Logger LOGGER
            = LogManager.getLogger(LogFileBlock.class);

    private final String m_name;
    private final Long m_startPosition;
    private final Integer m_size;
    private final LCHashType m_hashType;
    private final byte[] m_hashDigest;

    public Long getStartPosition()
    {
        return m_startPosition;
    }

    public byte[] getHashDigest()
    {
        return m_hashDigest;
    }

    public LCHashType getHashType()
    {
        return m_hashType;
    }

    public Integer getSize()
    {
        return m_size;
    }

    public String getName()
    {
        return m_name;
    }

    private LogFileBlock(final String name,
                         final Long startPosition,
                         final Integer size,
                         final LCHashType hashType,
                         final byte[] hashDigest)
    {
        this.m_name = name;
        this.m_startPosition = startPosition;
        this.m_size = size;
        this.m_hashType = hashType;
        this.m_hashDigest = hashDigest;
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
                LOGGER.debug(errMsg, ex);

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
                LOGGER.debug(errMsg, ex);

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
                LOGGER.debug(errMsg, ex);

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
                LOGGER.debug(errMsg, ex);

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

    /**
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

        if( LOGGER.isDebugEnabled() )
        {
            if( block.length > LogCheckConstants.MAX_ID_BLOCK_SIZE )
            {
                LOGGER.debug(String.format("Block size is greater than ID max. Size = %d",
                        block.length));
            }
            else
            {
                LOGGER.debug(String.format("Block size %d : \n=======\n%s\n=======\n",
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
                    LOGGER.debug("Failed getting SHA-1 Hash", ex);
                }
                break;

            case SHA2:
                try
                {
                    md = MessageDigest.getInstance("SHA-256");
                }
                catch( NoSuchAlgorithmException ex )
                {
                    LOGGER.debug("Failed getting SHA-256 Hash", ex);
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

        if( LOGGER.isDebugEnabled() )
        {
            LOGGER.debug(String.format("Hash '%s' : %x", hashType, new java.math.BigInteger(1, value)));
        }

        res = from(name,
                startPosition,
                block.length,
                hashType,
                value);

        return res;
    }

    public static LogFileBlock from(final String name,
                                    final Path file,
                                    final long pos,
                                    final int size,
                                    final LCHashType hashType) throws LogCheckException
    {
        FileChannel fc;

        try
        {
            if( Files.size(file)<1)
            {
                throw new LogCheckException(
                        String.format("Empty file '%s'", file));
            }

            if(pos < 0 || pos > Files.size(file)-1)
            {
                throw new LogCheckException(
                        String.format("Invalid position %d for file with size %d '%s'",
                                                                pos, Files.size(file), file));
            }
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error reading file size '%s'", file);

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        if( size < 0 || size > LogCheckConstants.MAX_ID_BLOCK_SIZE)
        {
            throw new LogCheckException(
                    String.format("Invalid size %d for file '%s'", size, file));
        }

        try
        {
            fc = FileChannel.open(file, StandardOpenOption.READ);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error opening file '%s'", file);

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        try
        {
            fc.position(pos);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error setting file position '%s'", file);

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        ByteBuffer buffer = ByteBuffer.allocate(size);

        int bytesRead;

        try
        {
            bytesRead = fc.read(buffer);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error reading file '%s'", file);

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        buffer.flip();

        byte[] actualBytes = new byte[buffer.remaining()];
        buffer.get(actualBytes);

        LogFileBlock res = LogFileBlock.from(name,
                pos,
                hashType,
                actualBytes);

        return res;
    }

    public static boolean isValidFileBlock( final Path file,
                                            final LogFileBlock block ) throws LogCheckException
    {
        boolean res = false;
        Integer size = null;
        ByteBuffer bb = null;
        Long pos = null;

        if( block == null )
        {
            throw new IllegalArgumentException("Log File Block cannot be null.");
        }

        if( file == null )
        {
            throw new IllegalArgumentException("Log File Path cannot be null.");
        }

        size = block.getSize();
        pos = block.getStartPosition();

        if( size == null || size < 1 || size > LogCheckConstants.MAX_ID_BLOCK_SIZE )
        {
            throw new IllegalArgumentException(
                    String.format("Invalid Log File Block Size %d.", size));
        }

        if( pos == null || pos < 0  )
        {
            throw new IllegalArgumentException(
                    String.format("Invalid Log File Position %d.", pos));
        }

        bb = ByteBuffer.allocate(size);

        FileChannel logFC = null;
        try
        {
            logFC = FileChannel.open(file);

            // Read the last block from file.
            logFC.position(pos);
            logFC.read(bb);
        }
        catch( IOException ex )
        {
            LOGGER.debug(String.format("Error reading Log File Block from '%s'", file), ex);
        }
        finally
        {
            if( logFC != null )
            {
                try
                {
                    logFC.close();
                }
                catch( IOException ex )
                {
                    LOGGER.debug(String.format("Error closing Log File '%s'", file), ex);
                }
            }
        }

        bb.flip();

        if( size != bb.limit() )
        {
            throw new LogCheckException(
                    String.format("Log File Block Size %d is not equal to bytes read %d.\n%s\n",
                            size, bb.limit(), block));
        }

        String currHashType = LCHashType.toId(block.getHashType());
        MessageDigest md = null;

        try
        {
            md = MessageDigest.getInstance(currHashType);
        }
        catch( NoSuchAlgorithmException ex )
        {
            LOGGER.debug("Error generating message digest.", ex);

            throw new LogCheckException("Error generating message digest.", ex);
        }

        md.update(bb);

        byte[] currDigest = md.digest();
        byte[] blockDigest = block.getHashDigest();

        res = Arrays.equals(currDigest, blockDigest);

        if( LOGGER.isDebugEnabled() && res == false )
        {
            LOGGER.debug(String.format(
                    "Log File Block is invalid.\nBlock\n======\n'%s'\n", block));
        }

        return res;
    }

    @Override
    public String toString()
    {
        String res = String.format("Name  : '%s'\nPos   : %d\nSize   : %d\nHash Type : %s\nHash '%s'\n",
                m_name, m_startPosition, m_size, m_hashType, Hex.encodeHexString(m_hashDigest));

        return res;
    }
}
