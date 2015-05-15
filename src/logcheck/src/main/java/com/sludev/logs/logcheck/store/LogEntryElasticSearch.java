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
package com.sludev.logs.logcheck.store;

import com.sludev.logs.logcheck.enums.LogCheckIndexNameFormat;
import com.sludev.logs.logcheck.log.ILogEntrySource;
import com.sludev.logs.logcheck.log.LogEntry;
import com.sludev.logs.logcheck.log.LogEntryBuilder;
import com.sludev.logs.logcheck.log.LogEntryVO;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckResult;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author kervin
 */
public class LogEntryElasticSearch implements ILogEntryStore
{
    private static final Logger log 
                             = LogManager.getLogger(LogEntryElasticSearch.class);

    private ILogEntrySource mainLogEntrySource;
    private JestClient esClient;
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

    public URL getElasticsearchURL()
    {
        return elasticsearchURL;
    }

    public final void setElasticsearchURL(URL u)
    {
        this.elasticsearchURL = u;
    }
    
    public final void setElasticsearchURL(String e)
    {
        URL u = null;
        
        try
        {
            u = new URL(e);
        }
        catch (MalformedURLException ex)
        {
            log.error( String.format("Invalid URL : '%s'", e), ex );
        }
        
        this.elasticsearchURL = u;
    }
    
    public LogEntryElasticSearch()
    {
        mainLogEntrySource = null;
        esClient = null;
        elasticsearchIndexNameFormat 
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT;
        elasticsearchLogType 
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_LOG_TYPE;
        elasticsearchIndexPrefix
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_PREFIX;
        
        setElasticsearchURL(LogCheckConstants.DEFAULT_ELASTICSEARCH_URL);
    }
    
    public String getIndex()
    {
        String indx = null;
        String nowStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        
        switch(elasticsearchIndexNameFormat)
        {
            case PREFIX_DATE:
                indx = String.format("%s%s", elasticsearchIndexPrefix, nowStr);
                break;
                
            default:
                break;
        }
        
        return indx;
    }
    
    @Override
    public void init()
    {
        JestClientFactory factory = new JestClientFactory();
        
        factory.setHttpClientConfig(new HttpClientConfig
                        .Builder(elasticsearchURL.toString())
                        .multiThreaded(true)
                        .build());
 
        esClient = factory.getObject();
    }
    
    @Override
    public LogCheckResult call() throws Exception
    {
        LogCheckResult res = new LogCheckResult();
        LogEntry currEntry;
        
        while(true)
        {       
            // Block until the next log entry
            currEntry = mainLogEntrySource.next();
            
            LogCheckResult putRes = put(currEntry);
            if( putRes == null )
            {
                break;
            }
        }
        
        return res;
    }
    
    @Override
    public void setMainLogEntrySource(ILogEntrySource src)
    {
        mainLogEntrySource = src;
    }

    @Override
    public LogCheckResult put(LogEntry le) throws InterruptedException, LogCheckException
    {
        log.debug( String.format("put() for logEntry '%s'\n", le.getTimeStamp()));
        
        LogCheckResult res = new LogCheckResult();
        
        LogEntryVO currVO = LogEntryBuilder.logEntry2VO(le);
        String currIndex = getIndex();
        String currLogType = getElasticsearchLogType();
        String currJSON = LogEntryBuilder.vo2JS(currVO);
        
        Index index = new Index.Builder(currJSON).index(currIndex).type(currLogType).build();

        try
        {
            JestResult exRes = esClient.execute(index);
        }
        catch( InterruptedException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            String errMsg = String.format("Error sending log entry to Elasticsearch '%s'", le.getLogger());
            
            log.info( errMsg, ex);
            
            throw new LogCheckException(errMsg, ex);
        }

        return res;
    }
}
