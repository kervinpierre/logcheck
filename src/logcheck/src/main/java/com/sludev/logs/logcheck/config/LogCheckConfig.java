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
package com.sludev.logs.logcheck.config;

import com.sludev.logs.logcheck.enums.LogCheckIndexNameFormat;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author kervin
 */
public class LogCheckConfig
{
    private static final org.apache.logging.log4j.Logger log 
                             = LogManager.getLogger(LogCheckConfig.class);
    
    private boolean service;
    private long pollIntervalSeconds;
    private String emailOnError;
    private String smtpServer;
    private String smtpPort;
    private String smtpPass;
    private String smtpUser;
    private String smtpProto;
    private boolean dryRun;
    private boolean showVersion;
    private boolean fileFromStart;
    private Path lockFilePath;
    private Path logPath;
    private Path statusFilePath;
    private Path configFilePath;
    private Path holdingFolderPath;
    private URL elasticsearchURL;
    private String elasticsearchIndexName;
    private String elasticsearchIndexPrefix;
    private String elasticsearchLogType;
    private LogCheckIndexNameFormat elasticsearchIndexNameFormat;

    public String getElasticsearchLogType()
    {
        return elasticsearchLogType;
    }

    public void setElasticsearchLogType(String e)
    {
        this.elasticsearchLogType = e;
    }

    public LogCheckIndexNameFormat getElasticsearchIndexNameFormat()
    {
        return elasticsearchIndexNameFormat;
    }

    public void setElasticsearchIndexNameFormat(LogCheckIndexNameFormat e)
    {
        this.elasticsearchIndexNameFormat = e;
    }

    public void setElasticsearchIndexNameFormat(String e)
    {
        LogCheckIndexNameFormat lcinf = LogCheckIndexNameFormat.valueOf(e);
        this.elasticsearchIndexNameFormat = lcinf;
    }
    
    public String getElasticsearchIndexName()
    {
        return elasticsearchIndexName;
    }

    public void setElasticsearchIndexName(String e)
    {
        this.elasticsearchIndexName = e;
    }

    public String getElasticsearchIndexPrefix()
    {
        return elasticsearchIndexPrefix;
    }

    public void setElasticsearchIndexPrefix(String e)
    {
        this.elasticsearchIndexPrefix = e;
    }

    public boolean isFileFromStart()
    {
        return fileFromStart;
    }

    public void setFileFromStart(boolean f)
    {
        this.fileFromStart = f;
    }

    public Path getHoldingFolderPath()
    {
        return holdingFolderPath;
    }

    public void setHoldingFolderPath(Path h)
    {
        this.holdingFolderPath = h;
    }

    public void setHoldingFolderPath(String h)
    {
        Path p = Paths.get(h);
        this.holdingFolderPath = p;
    }
    
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

    public long getPollIntervalSeconds()
    {
        return pollIntervalSeconds;
    }

    public void setPollIntervalSeconds(long p)
    {
        this.pollIntervalSeconds = p;
    }

    public void setPollIntervalSeconds(String p)
    {
        this.pollIntervalSeconds = Long.parseLong(p);
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

    public void setDryRun(boolean d)
    {
        this.dryRun = d;
    }

    public void setDryRun(String d)
    {
        boolean b = Boolean.valueOf(d);
        this.dryRun = b;
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
    
    public URL getElasticsearchURL()
    {
        return elasticsearchURL;
    }

    public final void setElasticsearchURL(URL u)
    {
        this.elasticsearchURL = u;
    }
    
    public final void setElasticsearchURL(String u)
    {
        URL esu = null;
        
        try
        {
            esu = new URL(u);
        }
        catch (MalformedURLException ex)
        {
            log.error( String.format("Invalid Elasticsearch URL : '%s'", u), ex);
        }
        
        this.elasticsearchURL = esu;
    }
    
    public LogCheckConfig()
    {
        pollIntervalSeconds = LogCheckConstants.DEFAULT_POLL_INTERVAL;
        setElasticsearchURL(LogCheckConstants.DEFAULT_ELASTICSEARCH_URL);
        fileFromStart = false;
        
        elasticsearchIndexPrefix 
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_PREFIX;
        elasticsearchIndexNameFormat 
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT;
        
        elasticsearchLogType = LogCheckConstants.DEFAULT_ELASTICSEARCH_LOG_TYPE;
    }
}
