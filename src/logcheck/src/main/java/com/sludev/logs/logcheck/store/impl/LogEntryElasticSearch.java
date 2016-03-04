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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.searchbox.indices.aliases.GetAliases;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Store LOGGER entries in a Elastic Search server.
 *
 * @author kervin
 *
 */
public final class LogEntryElasticSearch implements ILogEntryStore
{
    private static final Logger LOGGER
                             = LogManager.getLogger(LogEntryElasticSearch.class);

    private final JestClient m_esClient;
    private final URL m_elasticsearchURL;
    private final String m_elasticsearchLogType;
    private String m_elasticsearchIndexName;
    private String m_elasticsearchIndexPrefix;
    private LCIndexNameFormat m_elasticsearchIndexNameFormat;

    public String getElasticsearchLogType()
    {
        return m_elasticsearchLogType;
    }

    public LCIndexNameFormat getElasticsearchIndexNameFormat()
    {
        return m_elasticsearchIndexNameFormat;
    }

    public void setElasticsearchIndexNameFormat(LCIndexNameFormat e)
    {
        this.m_elasticsearchIndexNameFormat = e;
    }

    public void setElasticsearchIndexNameFormat(String e)
    {
        LCIndexNameFormat lcinf = LCIndexNameFormat.valueOf(e);
        this.m_elasticsearchIndexNameFormat = lcinf;
    }
    
    public String getElasticsearchIndexName()
    {
        return m_elasticsearchIndexName;
    }

    public void setElasticsearchIndexName(String e)
    {
        this.m_elasticsearchIndexName = e;
    }

    public String getElasticsearchIndexPrefix()
    {
        return m_elasticsearchIndexPrefix;
    }

    public void setElasticsearchIndexPrefix(String e)
    {
        this.m_elasticsearchIndexPrefix = e;
    }

    public URL getElasticsearchURL()
    {
        return m_elasticsearchURL;
    }
    
    private LogEntryElasticSearch(final URL elasticsearchURL,
                                  final JestClient esClient,
                                  final String elasticsearchLogType)
    {
        m_elasticsearchIndexNameFormat
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT;
        m_elasticsearchIndexPrefix
                = LogCheckConstants.DEFAULT_ELASTICSEARCH_INDEX_PREFIX;

        this.m_elasticsearchURL = elasticsearchURL;

        if( StringUtils.isNoneBlank(elasticsearchLogType) )
        {
            m_elasticsearchLogType = elasticsearchLogType;
        }
        else
        {
            m_elasticsearchLogType
                    = LogCheckConstants.DEFAULT_LOG_TYPE;
        }

        if( esClient == null )
        {
            JestClientFactory factory = new JestClientFactory();

            factory.setHttpClientConfig(new HttpClientConfig
                    .Builder(m_elasticsearchURL.toString())
                    .multiThreaded(true)
                    .build());

            m_esClient = factory.getObject();
        }
        else
        {
            m_esClient = esClient;
        }

    }

    public static LogEntryElasticSearch from(final URL url,
                                             final JestClient esClient,
                                             final String elasticsearchLogType)
    {
        LogEntryElasticSearch res = null;

        res = new LogEntryElasticSearch(url, esClient, elasticsearchLogType);

        return res;
    }

    public static LogEntryElasticSearch from(final String urlStr,
                                             final String elasticsearchLogType) throws LogCheckException
    {
        LogEntryElasticSearch res = null;

        URL url = null;

        try
        {
            url = new URL(urlStr);
        }
        catch (MalformedURLException ex)
        {
            String errMsg = String.format("Invalid URL : '%s'", urlStr);

            LOGGER.debug( errMsg, ex );

            throw new LogCheckException(errMsg, ex);
        }

        res = from(url, null, elasticsearchLogType);

        return res;
    }

    public String getIndex()
    {
        String indx = null;
        String nowStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        
        switch( m_elasticsearchIndexNameFormat )
        {
            case PREFIX_DATE:
                indx = String.format("%s%s", m_elasticsearchIndexPrefix, nowStr);
                break;
                
            default:
                break;
        }
        
        return indx;
    }
    
    @Override
    public void init()
    {

    }

    @Override
    public void destroy()
    {

    }

    @Override
    public LCResultStatus testConnection() throws LogCheckException
    {
        LCResultStatus res = LCResultStatus.SUCCESS;

        GetAliases aliases = new GetAliases.
                Builder().
                build();

        JestResult clientRes;
        try
        {
            clientRes = m_esClient.execute(aliases);
        }
        catch( IOException ex )
        {
            LOGGER.debug("testConnection failed", ex);

            return LCResultStatus.FAIL;
        }

        if( clientRes.isSucceeded() == false )
        {
            LOGGER.debug(String.format("execute() : %s", clientRes.getErrorMessage()));

            return LCResultStatus.FAIL;
        }

        return res;
    }

    /**
     * Put a LOGGER entry into the backend store, which is Elastic Search in this case.
     *
     * @param le The LOGGER entry that needs to be stored.
     * @return
     * @throws InterruptedException
     * @throws LogCheckException
     */
    @Override
    public LogCheckResult put(LogEntryVO le) throws InterruptedException, LogCheckException
    {
        LOGGER.debug( String.format("put() for logEntry '%s'\n", le.getTimeStamp()));
        
        LogCheckResult res = LogCheckResult.from(LCResultStatus.SUCCESS);

        String currIndex = getIndex();
        String currLogType = getElasticsearchLogType();
        String currJSON = LogEntryVO.toJSON(le);
        
        Index index = new Index.Builder(currJSON).index(currIndex).type(currLogType).build();

        try
        {
            JestResult exRes = m_esClient.execute(index);
        }
        catch (Exception ex)
        {
            String errMsg = String.format("Error sending log entry to Elasticsearch '%s'", le.getLogger());
            
            LOGGER.info( errMsg, ex);
            
            throw new LogCheckException(errMsg, ex);
        }

        return res;
    }
}
