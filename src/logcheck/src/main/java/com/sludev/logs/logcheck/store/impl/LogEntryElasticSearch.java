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
package com.sludev.logs.logcheck.store.impl;

import com.sludev.logs.logcheck.enums.LCIndexNameFormat;
import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.config.entities.LogEntryVO;
import com.sludev.logs.logcheck.store.ILogEntryStore;
import com.sludev.logs.logcheck.utils.LogCheckConstants;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
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
 * Store log entries in a Elastic Search server.
 *
 * @author kervin
 */
public final class LogEntryElasticSearch implements ILogEntryStore
{
    private static final Logger log 
                             = LogManager.getLogger(LogEntryElasticSearch.class);

    private JestClient esClient;
    private URL elasticsearchURL;
    private String elasticsearchIndexName;
    private String elasticsearchIndexPrefix;
    private String elasticsearchLogType;
    private LCIndexNameFormat elasticsearchIndexNameFormat;


    public String getElasticsearchLogType()
    {
        return elasticsearchLogType;
    }

    public void setElasticsearchLogType(String e)
    {
        this.elasticsearchLogType = e;
    }

    public LCIndexNameFormat getElasticsearchIndexNameFormat()
    {
        return elasticsearchIndexNameFormat;
    }

    public void setElasticsearchIndexNameFormat(LCIndexNameFormat e)
    {
        this.elasticsearchIndexNameFormat = e;
    }

    public void setElasticsearchIndexNameFormat(String e)
    {
        LCIndexNameFormat lcinf = LCIndexNameFormat.valueOf(e);
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
    
    private LogEntryElasticSearch()
    {
        esClient = null;
        elasticsearchIndexNameFormat 
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT;
        elasticsearchLogType 
                = LogCheckConstants.DEFAULT_LOG_TYPE;
        elasticsearchIndexPrefix
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_PREFIX;
        
        setElasticsearchURL(LogCheckConstants.DEFAULT_ELASTICSEARCH_URL);
    }

    public static LogEntryElasticSearch from()
    {
        LogEntryElasticSearch res = new LogEntryElasticSearch();

        return res;
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
    public void destroy()
    {

    }

    /**
     * Put a log entry into the backend store, which is Elastic Search in this case.
     *
     * @param le The log entry that needs to be stored.
     * @return
     * @throws InterruptedException
     * @throws LogCheckException
     */
    @Override
    public LogCheckResult put(LogEntryVO le) throws InterruptedException, LogCheckException
    {
        log.debug( String.format("put() for logEntry '%s'\n", le.getTimeStamp()));
        
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        String currIndex = getIndex();
        String currLogType = getElasticsearchLogType();
        String currJSON = LogEntryVO.toJSON(le);
        
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
