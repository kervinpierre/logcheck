package com.sludev.logs.logcheckSampleApp.output;

import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Write to a file using a BufferedWriter.
 *
 * Created by kervin on 2015-10-14.
 */
public final class BufferedWriterWriteFile implements IWriteFile
{
    private static final Logger log = LogManager.getLogger(BufferedWriterWriteFile.class);

    private final Path file;
    private final Boolean createFile;
    private final Boolean append;
    private final Boolean truncate;
    private final Long maxWriteCount;

    //Mutable pointers
    private Long writeCount;

    private BufferedWriter bufferedWriter;

    @Override
    public Long getWriteCount()
    {
        return writeCount;
    }

    public BufferedWriterWriteFile(final Path file,
                                   final Boolean createFile,
                                   final Boolean append,
                                   final Boolean truncate,
                                   final Long maxWriteCount)
    {
        this.file = file;
        this.createFile = createFile;
        this.append = append;
        this.truncate = truncate;
        this.writeCount = 0L;
        this.maxWriteCount = maxWriteCount;
    }

    @Override
    public LCSAResult writeLine(String line) throws IOException
    {
        LCSAResult res =  LCSAResult.SUCCESS;

        bufferedWriter.write(line);
        bufferedWriter.flush();
        if( maxWriteCount != null
            && maxWriteCount > 0
            && ++writeCount >= maxWriteCount )
        {
            res = LCSAResult.COMPLETED_ROTATE_PENDING;
        }

        return res;
    }

    @Override
    public void closeFile() throws IOException
    {
        if( bufferedWriter == null )
        {
            return;
        }

        bufferedWriter.close();
    }

    @Override
    public void openFile() throws IOException, LogCheckAppException
    {
        List<OpenOption> options = new ArrayList<>();
        OpenOption[] optionsArray;

        options.add(StandardOpenOption.WRITE);

        if( createFile )
        {
            options.add(StandardOpenOption.CREATE);
        }

        if( append )
        {
            options.add(StandardOpenOption.APPEND);
        }

        if( truncate )
        {
            options.add(StandardOpenOption.TRUNCATE_EXISTING);
        }

        optionsArray = new OpenOption[options.size()];
        options.toArray(optionsArray);

        try
        {
            bufferedWriter = Files.newBufferedWriter(file, optionsArray);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Error opening file for writing '%s'", file);

            log.debug(errMsg, ex);

            throw new LogCheckAppException(errMsg, ex);
        }
    }
}
