package com.sludev.logs.elasticsearchApp.elasticsearch;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Delete;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * Created by kervin on 2016-04-01.
 */
public final class EADelete
{
    private static final Logger LOGGER = LogManager.getLogger(EADelete.class);

    public static void doDeleteIndex( final URL elasticsearchURL,
                                      final Collection<String> indexes ) throws IOException
    {
        JestClientFactory factory = new JestClientFactory();

        factory.setHttpClientConfig(new HttpClientConfig
                .Builder(elasticsearchURL.toString())
                .multiThreaded(true)
                .build());

        JestClient client = factory.getObject();

        for( String currIndex : indexes )
        {
            Delete deleteDocument = new Delete.Builder("")
                    .index(currIndex)
                    .build();

            JestResult res = client.execute(deleteDocument);

            LOGGER.warn(String.format("Deleting Index '%s' returned...\n'%s'",
                    currIndex, res.getJsonString()));
        }
    }
}
