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
package com.sludev.logs.logcheck.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import jnr.posix.POSIX;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manage the file locking in the application.
 * 
 * @author kervin
 */
public class LogCheckLockFile
{
    private static final Logger LOGGER
                      = LogManager.getLogger(LogCheckLockFile.class);
    
    public static int getLockPID(final Path lkFile) throws IOException, LogCheckException
    {
        int res = 0;
        
        if( (lkFile == null) || (Files.exists(lkFile) == false) )
        {
            throw new FileNotFoundException(String.format("Missing Lock File '%s", lkFile));
        }
        
        List<String> lkLines = Files.readAllLines(lkFile);

        if( (lkLines == null) || (lkLines.size() < 1) )
        {
            throw new LogCheckException(
                    String.format("Invalid or empty LOCK file specified at %s.\nPlease delete it to continue.", lkFile));
        }

        String pidStr = lkLines.get(0);

        try
        {
            res = Integer.parseInt(pidStr.trim());
        }
        catch( NumberFormatException ex )
        {
            throw new LogCheckException(String.format("Invalid LOCK file PID '%s'", pidStr), ex);
        }
        
        return res;
    }
    
    /**
     * Create a lock file if it doesn't exist.
     * 
     * @param lkPath Path to the lock file being created.
     * @return true if the lock file was created
     */
    public static boolean acquireLockFile(final Path lkPath)
    {
        boolean res = false;
        
        try
        {
            File lkFile = lkPath.toFile();
            if( lkFile.exists() )
            {
                return false;
            }
            
            if( lkFile.createNewFile() == false )
            {
                return false;
            }
            
            try(PrintWriter pw = new PrintWriter(lkFile))
            {          
                POSIX cp = LogCheckPOSIX.createPOSIX();
                int pid = cp.getpid();

                pw.format("%d", pid);
                
                res = true;
            }
             
        }
        catch (IOException ex)
        {
            LOGGER.error( String.format("Error locking '%s'", lkPath), ex);
        }
        
        return res;
    }

    /**
     * Delete the lock file.
     * 
     * @param lkPath
     * @return
     */
    public static boolean releaseLockFile(final Path lkPath)
    {
        boolean res = false;
        
        try
        {
            Files.delete(lkPath);
            res = true;
        }
        catch (IOException ex)
        {
            LOGGER.error( String.format("Error deleting lock file '%s'", lkPath), ex);
            return false;
        }
        
        return res;
    }
}
