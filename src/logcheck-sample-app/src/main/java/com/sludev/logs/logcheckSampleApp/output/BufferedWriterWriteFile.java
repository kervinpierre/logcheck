package com.sludev.logs.logcheckSampleApp.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kervin on 2015-10-14.
 */
public final class BufferedWriterWriteFile implements IWriteFile
{
    private static final Logger log = LogManager.getLogger(BufferedWriterWriteFile.class);

    private final Path file;
    private final Boolean createFile;
    private final Boolean append;
    private final Boolean truncate;

    private BufferedWriter bufferedWriter;

    public BufferedWriterWriteFile(final Path file,
                                   final Boolean createFile,
                                   final Boolean append,
                                   final Boolean truncate)
    {
        this.file = file;
        this.createFile = createFile;
        this.append = append;
        this.truncate = truncate;
    }

    @Override
    public void writeLine(String line) throws IOException
    {
        bufferedWriter.write(line);
        bufferedWriter.flush();
    }

    @Override
    public void openFile() throws IOException
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

        bufferedWriter = Files.newBufferedWriter(file, optionsArray);
    }
}
