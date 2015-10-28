package com.sludev.logs.logcheck.tail;

import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import com.sludev.logs.logcheck.config.entities.LogFileState;
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

/**
 * Track the statistics on an ongoing tailer job.
 *
 * Created by kervin on 10/27/2015.
 */
public class TailerStatistics
{
    private static final Logger log
            = LogManager.getLogger(TailerStatistics.class);

    private final Path file;

    // Mutable
    private LogFileState logFileState;
    private Long lastProcessedPosition;

    public Path getFile()
    {
        return file;
    }

    public LogFileState getLogFileState()
    {
        return logFileState;
    }

    public void setLogFileState(LogFileState logFileState)
    {
        this.logFileState = logFileState;
    }

    public Long getLastProcessedPosition()
    {
        return lastProcessedPosition;
    }

    public void setLastProcessedPosition(Long lastProcessedPosition)
    {
        this.lastProcessedPosition = lastProcessedPosition;
    }

    private TailerStatistics(final Path file)
    {
        this.file = file;

        this.lastProcessedPosition = 0L;
        this.logFileState = null;
    }

    public static TailerStatistics from(final Path file)
    {
        TailerStatistics res = new TailerStatistics(file);

        return res;
    }

    public static LogFileBlock getBlock(Path fl, long pos, int sz, LCHashType hs) throws LogCheckException
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

        LogFileBlock res = LogFileBlock.from(pos, sz, hs);

        return res;
    }
}
