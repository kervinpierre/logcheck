/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sludev.logs.logcheck.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Administrator
 */
public class LogCheckConfigFile
{
    private Path filePath;
    private LogCheckConfig config;

    public LogCheckConfig getConfig()
    {
        return config;
    }

    public void setConfig(LogCheckConfig c)
    {
        this.config = c;
    }

    public Path getFilePath()
    {
        return filePath;
    }

    public void setFilePath(Path f)
    {
        this.filePath = f;
    }
    
    public void setFilePath(String f)
    {
        Path p = Paths.get(f);
        this.filePath = p;
    }

    public LogCheckConfigFile()
    {
    }
    
    public void read()
    {
        ;
    }
}
