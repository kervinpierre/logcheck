package com.sludev.logs.logcheckSampleApp.output;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * Created by kervin on 2015-10-14.
 */
public interface IWriteFile
{
    public static final Logger log = LogManager.getLogger(IWriteFile.class);

    void writeLine(String line) throws IOException;

    static void truncate(Path file) throws IOException
    {
        FileChannel outChan = FileChannel.open(file);
        outChan.truncate(0);
        outChan.close();
    }

    void openFile() throws IOException;
}
