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
public class LogCheckConfig
{
    private boolean service;
    private String cronScheduleString;
    private String emailOnError;
    private String smtpServer;
    private String smtpPort;
    private String smtpPass;
    private String smtpUser;
    private String smtpProto;
    private boolean dryRun;
    private boolean showVersion;
    private Path lockFilePath;
    private Path logPath;
    private Path statusFilePath;
    private Path configFilePath;
    private String redisServer;
    private String redisPort;

    public Path getConfigFilePath()
    {
        return configFilePath;
    }

    public void setConfigFilePath(Path c)
    {
        this.configFilePath = c;
    }

    public void setConfigFilePath(String c)
    {
        Path p = Paths.get(c);
        this.configFilePath = p;
    }
    
    public boolean isService()
    {
        return service;
    }

    public void setService(boolean service)
    {
        this.service = service;
    }

    public String getCronScheduleString()
    {
        return cronScheduleString;
    }

    public void setCronScheduleString(String cronScheduleString)
    {
        this.cronScheduleString = cronScheduleString;
    }

    public String getEmailOnError()
    {
        return emailOnError;
    }

    public void setEmailOnError(String emailOnError)
    {
        this.emailOnError = emailOnError;
    }

    public String getSmtpServer()
    {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer)
    {
        this.smtpServer = smtpServer;
    }

    public String getSmtpPort()
    {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort)
    {
        this.smtpPort = smtpPort;
    }

    public String getSmtpPass()
    {
        return smtpPass;
    }

    public void setSmtpPass(String smtpPass)
    {
        this.smtpPass = smtpPass;
    }

    public String getSmtpUser()
    {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser)
    {
        this.smtpUser = smtpUser;
    }

    public String getSmtpProto()
    {
        return smtpProto;
    }

    public void setSmtpProto(String smtpProto)
    {
        this.smtpProto = smtpProto;
    }

    public boolean isDryRun()
    {
        return dryRun;
    }

    public void setDryRun(boolean dryRun)
    {
        this.dryRun = dryRun;
    }

    public boolean isShowVersion()
    {
        return showVersion;
    }

    public void setShowVersion(boolean showVersion)
    {
        this.showVersion = showVersion;
    }

    public Path getLockFilePath()
    {
        return lockFilePath;
    }

    public void setLockFilePath(Path lockFilePath)
    {
        this.lockFilePath = lockFilePath;
    }

    public void setLockFilePath(String lockFilePath)
    {
        Path p = Paths.get(lockFilePath);
        this.lockFilePath = p;
    }
     
    public Path getLogPath()
    {
        return logPath;
    }

    public void setLogPath(Path logPath)
    {
        this.logPath = logPath;
    }

    public void setLogPath(String logPath)
    {
        Path p = Paths.get(logPath);
        this.logPath = p;
    }
    
    public Path getStatusFilePath()
    {
        return statusFilePath;
    }

    public void setStatusFilePath(Path statusFilePath)
    {
        this.statusFilePath = statusFilePath;
    }

    public void setStatusFilePath(String statusFilePath)
    {
        Path p = Paths.get(statusFilePath);
        this.statusFilePath = p;
    }
    
    public String getRedisServer()
    {
        return redisServer;
    }

    public void setRedisServer(String redisServer)
    {
        this.redisServer = redisServer;
    }

    public String getRedisPort()
    {
        return redisPort;
    }

    public void setRedisPort(String redisPort)
    {
        this.redisPort = redisPort;
    }
    
    
}
