package com.sludev.logs.elasticsearchApp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2016-03-29.
 */
public final class EAConstants
{
    private static final Logger LOGGER = LogManager.getLogger(EAConstants.class);

    public static final String PROD_VERSION = "0.9";
    public static final String DEFAULT_ELASTICSEARCH_URL = "http://127.0.0.1:9200";

    public static final int DEFAULT_ELASTICSEARCH_PAGESIZE = 100;
    public static final String DEFAULT_ELASTICSEARCH_READ_INDEX = "_all";
}
