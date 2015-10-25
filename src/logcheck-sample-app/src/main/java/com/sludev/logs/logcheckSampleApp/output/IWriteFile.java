package com.sludev.logs.logcheckSampleApp.output;

import com.sludev.logs.logcheckSampleApp.enums.LCSAResult;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppConstants;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interface for writing to files.
 *
 * Created by kervin on 2015-10-14.
 */
public interface IWriteFile
{
    public static final Logger log = LogManager.getLogger(IWriteFile.class);

    LCSAResult writeLine(String line) throws IOException;

    static void truncateFile(Path file) throws IOException
    {
        log.debug( String.format("Truncating '%s'.", file));

        FileChannel outChan = FileChannel.open(file);
        outChan.truncate(0);
        outChan.close();
    }

    void openFile() throws IOException, LogCheckAppException;
    void closeFile() throws IOException;

    /**
     * Rotate a file and return the path of the new backup file.
     *
     * @return Path of the new backup file
     */
    static Path rotateFile( final Path file,
                            final Long maxBackups,
                            final Boolean confirmDelete ) throws LogCheckAppException
    {
        log.debug(String.format("Rotating '%s'", file));

        if(Files.notExists(file))
        {
            String errMsg = String.format("File does not exist '%s'", file);

            log.debug(errMsg);
            throw new LogCheckAppException(errMsg);
        }

        if(Files.isDirectory(file))
        {
            String errMsg = String.format("Cannot be a directory '%s'", file);

            log.debug(errMsg);
            throw new LogCheckAppException(errMsg);
        }

        //        String currFileName = file.getFileName().toString();
        //        Pattern fp = Pattern.compile("(.*)\\.(\\d+)\\.bak");
        //        Matcher fpm = fp.matcher(currFileName);
        //        if( fpm.matches())
        //        {
        //            currFileName = fpm.group(1);
        //        }

        if( maxBackups != null
                && ( maxBackups < 0
                || maxBackups > LogCheckAppConstants.MAX_BACKUP_FILES ) )
        {
            throw new LogCheckAppException(
                    String.format("Invalid Maximum Backup-file count. %d needs to be between 0 and %d",
                            maxBackups, LogCheckAppConstants.MAX_BACKUP_FILES));
        }

        Path newPath = null;
        for(int i = 0; i < LogCheckAppConstants.MAX_BACKUP_FILES; i++)
        {
//            if( i >= maxBackups-1 )
//            {
//                ;
//            }
//
            String newName = String.format("%s.%04d.bak", file.getFileName().toString(), i);
            newPath = file.getParent().resolve(newName);
            if(Files.notExists(newPath))
            {
                break;
            }
        }

        if(newPath == null || Files.exists(newPath))
        {
            String errMsg = String.format("Invalid path '%s'\nNull or greater than 9999?", newPath);

            log.debug(errMsg);
            throw new LogCheckAppException(errMsg);
        }

        try
        {
            Files.copy(file, newPath);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Copy failed '%s' to '%s'", file, newPath);

            log.debug(errMsg, ex);
            throw new LogCheckAppException(errMsg, ex);
        }

        try
        {
            // Purposely delete instead of truncate in place to force
            // dealing with FILE_SHARE_DELETE CreateFile() issue on
            // Windows OS.
            Files.delete(file);

            if( confirmDelete != null
                    && confirmDelete )
            {
                // Give the FS sometime to execute the delete
      /*        try
                {
                    Thread.sleep(2000);
                }
                catch(InterruptedException ex)
                {
                    log.debug("Sleep interrupted", ex);
                }*/

                if(Files.exists(file) || Files.notExists(file) == false)
                {
                    // Delete failed
                    String errMsg = String.format("Failed deleting '%s'", file);

                    log.debug(errMsg);
                    throw new LogCheckAppException(errMsg);
                }
            }

            // FIXME : Make sure attributes stay the same
            Files.createFile(file);
        }
        catch(IOException ex)
        {
            String errMsg = String.format("Truncate failed '%s'", file);

            log.debug(errMsg, ex);
            throw new LogCheckAppException(errMsg, ex);
        }

        try
        {
            log.debug(String.format("Created backup '%s', size '%d'", newPath, Files.size(newPath)));
        }
        catch(IOException ex)
        {
            log.debug("Error logging on backup file", ex);
        }

        return newPath;
    }

    Long getWriteCount();
}
