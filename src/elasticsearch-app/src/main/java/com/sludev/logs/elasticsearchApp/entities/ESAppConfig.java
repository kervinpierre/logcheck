package com.sludev.logs.elasticsearchApp.entities;

import com.sludev.logs.elasticsearchApp.enums.EACmdAction;
import com.sludev.logs.elasticsearchApp.utils.EAConstants;
import com.sludev.logs.elasticsearchApp.utils.ESAException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by kervin on 2016-03-29.
 */
public final class ESAppConfig
{
    private static final Logger LOGGER = LogManager.getLogger(ESAppConfig.class);

    private final URL m_elasticsearchURL;
    private final Deque<String> m_elasticsearchIndexes;
    private final Deque<EACmdAction> m_elasticsearchActions;
    private final Path m_outputFile;
    private final Boolean m_logOutput;

    public Boolean getLogOutput()
    {
        return m_logOutput;
    }

    public URL getElasticsearchURL()
    {
        return m_elasticsearchURL;
    }

    public Deque<String> getElasticsearchIndexes()
    {
        return m_elasticsearchIndexes;
    }

    public Deque<EACmdAction> getElasticsearchActions()
    {
        return m_elasticsearchActions;
    }

    public Path getOutputFile()
    {
        return m_outputFile;
    }

    private ESAppConfig(final URL elasticsearchURL,
                        final Deque<String> elasticsearchIndexes,
                        final Deque<EACmdAction> elasticsearchActions,
                        final Path outputFile,
                        final Boolean logOutput)
    {
        if( elasticsearchURL != null )
        {
            this.m_elasticsearchURL = elasticsearchURL;
        }
        else
        {
            URL tempURL = null;
            try
            {
                tempURL = new URL(EAConstants.DEFAULT_ELASTICSEARCH_URL);
            }
            catch( MalformedURLException ex )
            {
                LOGGER.debug(String.format("Invalid Elasticsearch URL '%s'",
                        EAConstants.DEFAULT_ELASTICSEARCH_URL ), ex);
            }

            this.m_elasticsearchURL = tempURL;
        }

        if( elasticsearchIndexes != null )
        {
            this.m_elasticsearchIndexes = elasticsearchIndexes;
        }
        else
        {
            this.m_elasticsearchIndexes = new ArrayDeque<>();
        }

        if( elasticsearchActions != null )
        {
            this.m_elasticsearchActions = elasticsearchActions;
        }
        else
        {
            this.m_elasticsearchActions = new ArrayDeque<>();
        }

        this.m_outputFile = outputFile;
        this.m_logOutput = logOutput;
    }

    public static ESAppConfig from(final URL elasticsearchURL,
                final Deque<String> elasticsearchIndexes,
                final Deque<EACmdAction> elasticsearchActions,
                final Path outputFile,
                final Boolean logOutput)
    {
        ESAppConfig res = new ESAppConfig(elasticsearchURL,
                elasticsearchIndexes,
                elasticsearchActions,
                outputFile,
                logOutput);

        return res;
    }

    public static ESAppConfig from(final String elasticsearchURLStr,
                                   final String[] elasticsearchIndexesStr,
                                   final String[] elasticsearchActionsStr,
                                   final String outputFileStr,
                                   final Boolean logOutput) throws ESAException
    {
        ESAppConfig res;

        URL elasticsearchURL = null;
        Deque<EACmdAction> elasticsearchActions = null;
        Deque<String> elasticsearchIndexes = null;
        Path outputFile = null;

        if( StringUtils.isNoneBlank(elasticsearchURLStr) )
        {
            try
            {
                elasticsearchURL = new URL(elasticsearchURLStr);
            }
            catch( MalformedURLException ex )
            {
                LOGGER.debug(String.format("Invalid URL %s", elasticsearchURLStr), ex);
            }
        }

        if( elasticsearchActionsStr != null )
        {
            elasticsearchActions = new ArrayDeque<>();
            for( String currActionStr : elasticsearchActionsStr )
            {
                EACmdAction currAction = EACmdAction.from(currActionStr);
                if( currAction == null )
                {
                    String msg = String.format("Invalid Action '%s'", currActionStr);
                    LOGGER.debug(msg);

                    throw new ESAException(msg);
                }

                elasticsearchActions.addLast(currAction);
            }
        }

        if( elasticsearchIndexesStr != null )
        {
            elasticsearchIndexes = new ArrayDeque<>();
            for( String currIndexStr : elasticsearchIndexesStr )
            {
                if( StringUtils.isNoneBlank(currIndexStr) )
                {
                    elasticsearchIndexes.addLast(
                            StringUtils.trim(currIndexStr));
                }
            }
        }

        if( StringUtils.isNoneBlank(outputFileStr) )
        {
            outputFile = Paths.get(outputFileStr);
        }

        res = from(elasticsearchURL,
                        elasticsearchIndexes,
                        elasticsearchActions,
                        outputFile,
                        logOutput);

        return res;
    }
}
