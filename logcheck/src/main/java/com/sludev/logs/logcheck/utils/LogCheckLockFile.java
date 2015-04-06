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
    private static final Logger log 
                      = LogManager.getLogger(LogCheckLockFile.class);
    
    public static int getLockPID(Path lk) throws IOException
    {
        int res = 0;
        
        if( Files.exists(lk) == false )
        {
            throw new FileNotFoundException();
        }
        
        List<String> lkLines = Files.readAllLines(lk);
        String pidStr = lkLines.get(0);
        res = Integer.parseInt(pidStr.trim());
        
        return res;
    }
    
    /**
     * Create a lock file if it doesn't exist.
     * 
     * @param lk Path to the lock file being created.
     * @return true if the lock file was created
     */
    public static boolean acquireLockFile(Path lk)
    {
        boolean res = false;
        
        try
        {
            File lkFile = lk.toFile();
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
            log.error( String.format("Error locking '%s'", lk), ex);
        }
        
        return res;
    }

    /**
     * Delete the lock file.
     * 
     * @param lk
     * @return
     */
    public static boolean releaseLockFile(Path lk)
    {
        boolean res = false;
        
        try
        {
            Files.delete(lk);
            res = true;
        }
        catch (IOException ex)
        {
            log.error( String.format("Error deleting lock file '%s'", lk), ex);
            return false;
        }
        
        return res;
    }
}
