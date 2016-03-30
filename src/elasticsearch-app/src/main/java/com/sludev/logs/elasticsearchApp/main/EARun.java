package com.sludev.logs.elasticsearchApp.main;

import com.sludev.logs.elasticsearchApp.elasticsearch.EAScroll;
import com.sludev.logs.elasticsearchApp.entities.ESAppConfig;
import com.sludev.logs.elasticsearchApp.enums.EACmdAction;
import com.sludev.logs.elasticsearchApp.enums.ESAResult;
import com.sludev.logs.elasticsearchApp.utils.EAConstants;
import com.sludev.logs.elasticsearchApp.utils.ESAException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by kervin on 2016-03-29.
 */
public final class EARun implements Callable<ESAResult>
{
    private static final Logger LOGGER = LogManager.getLogger(EARun.class);

    private final ESAppConfig m_config;

    public ESAppConfig getConfig()
    {
        return m_config;
    }

    private EARun(final ESAppConfig config)
    {
        this.m_config = config;
    }

    public static EARun from(final ESAppConfig config)
    {
        EARun res = new EARun(config);

        return res;
    }

    @Override
    public ESAResult call() throws Exception
    {
        ESAResult res = null;
        Deque<EACmdAction> currActions = null;
        List<String> currIndexes = null;

        if( m_config == null )
        {
            throw new ESAException("Configuration cannot be null");
        }

        if( m_config.getElasticsearchActions() == null
                || m_config.getElasticsearchActions().isEmpty() )
        {
            LOGGER.warn("No actions were provided.  Defaulting to 'scroll'");

            currActions = new ArrayDeque<>();
            currActions.addLast(EACmdAction.SCROLL);
        }
        else
        {
            currActions = m_config.getElasticsearchActions();
        }

        if( m_config.getElasticsearchIndexes() == null
                || m_config.getElasticsearchIndexes().isEmpty() )
        {
            LOGGER.warn(String.format("No indexes were provided.  Defaulting to '%s'",
                    EAConstants.DEFAULT_ELASTICSEARCH_READ_INDEX));

            currIndexes = new ArrayList<>();
            currIndexes.add(EAConstants.DEFAULT_ELASTICSEARCH_READ_INDEX);
        }
        else
        {
            currIndexes = new ArrayList<>();
            currIndexes.addAll(m_config.getElasticsearchIndexes());
        }

        for( EACmdAction currAction : currActions )
        {
            switch( currAction )
            {
                case SCROLL:
                    {
                        EAScroll.doScroll(m_config.getElasticsearchURL(),
                                currIndexes,
                                m_config.getOutputFile(),
                                BooleanUtils.isTrue(m_config.getLogOutput()));
                    }
                    break;
            }
        }

        return res;
    }
}
