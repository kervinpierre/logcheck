package com.sludev.logs.elasticsearchApp.elasticsearch;

import com.sludev.logs.elasticsearchApp.utils.EAConstants;
import com.sludev.logs.elasticsearchApp.utils.ESAException;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchScroll;
import io.searchbox.params.Parameters;
import io.searchbox.params.SearchType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

/**
 * Created by kervin on 2016-03-29.
 */
public final class EAScroll
{
    private static final Logger LOGGER = LogManager.getLogger(EAScroll.class);

    public static void doScroll(final URL elasticsearchURL,
                                final List<String> index,
                                final Path outFile,
                                final boolean logOutput) throws IOException, ESAException
    {
        if( outFile != null && Files.exists(outFile) )
        {
            Files.write(outFile, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
        }

        JestClientFactory factory = new JestClientFactory();

        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(elasticsearchURL.toString())
                .multiThreaded(true)
                .build());

        JestClient client = factory.getObject();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        int currSize = EAConstants.DEFAULT_ELASTICSEARCH_PAGESIZE;

        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(index)
                .setParameter("pretty", "1")
                // .addType(type)
                .setParameter(Parameters.SEARCH_TYPE, SearchType.SCAN)
                .setParameter(Parameters.SIZE, currSize)
                .setParameter(Parameters.SCROLL, "5m")
                .build();

        LOGGER.info(String.format("Search : '%s'\n%s",
                            search.getURI(), search.getData(null)));

        JestResult result = handleResult(client, search, outFile, logOutput);

        String scrollId = result.getJsonObject()
                                    .get("_scroll_id").getAsString();

        int totalHits = result.getJsonObject()
                                .get("hits").getAsJsonObject()
                                .get("total").getAsInt();

        int shardCount = result.getJsonObject()
                .get("_shards").getAsJsonObject()
                .get("total").getAsInt();

        LOGGER.info(String.format("Total Hits : %d\nShard Count : %d",
                totalHits, shardCount));

        int currentResultSize = 0;
        int pageNumber = 1;
        int totalRecs = 0;
        do
        {
            SearchScroll scroll = new SearchScroll.Builder(scrollId, "5m")
                    .setParameter("pretty", 1)
                    .setParameter(Parameters.SIZE, currSize)
                    .build();

            result = handleResult(client, scroll, outFile, logOutput);

            scrollId = result.getJsonObject()
                                .get("_scroll_id").getAsString();

            currentResultSize = result.getJsonObject()
                                .get("hits").getAsJsonObject()
                                .get("hits").getAsJsonArray().size();

            totalRecs += currentResultSize;

            LOGGER.info(String.format("Page # %d which had %d results. Total is %d",
                    pageNumber++, currentResultSize, totalRecs));
        }
        while( currentResultSize > 0);

        LOGGER.debug(String.format("doScroll() : Completed %d Records.", totalRecs));
    }

    private static JestResult handleResult( final JestClient client,
                                            final Action action,
                                            final Path outFile,
                                            final boolean logOutput) throws IOException, ESAException
    {
        JestResult result = null;

        result = client.execute(action);
        if( result.isSucceeded() )
        {
            String currJSON = result.getJsonString();

            if( outFile != null )
            {
                Files.write(outFile, currJSON.getBytes(), StandardOpenOption.CREATE);
            }

            if( logOutput )
            {
                LOGGER.debug(String.format("RESULT :\n'%s'", currJSON));
            }

            //List hits = ((List) ((Map) result.getJsonMap().get("hits")).get("hits"));
            //LOGGER.debug("hits.size(): " + hits.size());
        }
        else
        {
            String msg = String.format("Result Error : %s\n'%s'",
                    result.getErrorMessage(), result.getJsonString());

            LOGGER.debug(msg);
            throw new ESAException(msg);
        }

        return result;
    }
}
