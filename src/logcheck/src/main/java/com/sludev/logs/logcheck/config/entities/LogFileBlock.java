package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.enums.LCHashType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 10/27/2015.
 */
public class LogFileBlock
{
    private static final Logger log
            = LogManager.getLogger(LogFileBlock.class);

    private final Long startPosition;
    private final Integer size;
    private final LCHashType hashType;
    private final String hashValue;


    private LogFileBlock(final Long startPosition,
                         final Integer size,
                         final LCHashType hashType,
                         final String hashValue)
    {
        this.startPosition = startPosition;
        this.size = size;
        this.hashType = hashType;
        this.hashValue = hashValue;
    }

    /*
    ** Generate the hash value
     */
    public static LogFileBlock from(final Long startPosition,
                             final Integer size,
                             final LCHashType hashType)
    {
        LogFileBlock res;

        // FIXME : Generate hash
        String value = null;

        res = from(startPosition,
                size,
                hashType,
                value);

        return res;
    }

    public static LogFileBlock from(final Long startPosition,
                        final Integer size,
                        final LCHashType hashType,
                        final String hashValue)
    {
        LogFileBlock res = new LogFileBlock(startPosition,
                size,
                hashType,
                hashValue);

        return res;
    }
}
