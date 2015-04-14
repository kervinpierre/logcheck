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
package com.sludev.logs.logcheck.utils;

import com.sludev.logs.logcheck.enums.LogCheckIndexNameFormat;

/**
 *
 * @author kervin
 */
public class LogCheckConstants
{
    public static final String PROD_LONG_NAME = "LogCheck App";
    public static final String PROD_VERSION = "0.9";
    public static final String PROD_BUILD = "20150406-01";
    
    public static final long   DEFAULT_POLL_INTERVAL = 5;
    public static final String   DEFAULT_ELASTICSEARCH_URL = "http://127.0.0.1:9200";
    
    public static final int    DEFAULT_LOG_READ_BUFFER_SIZE_BYTES = 80;
    
    public static final LogCheckIndexNameFormat DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT 
                                        = LogCheckIndexNameFormat.PREFIX_DATE;
    public static final String DEFAULT_ELASTICSEARCH_INDEX_PREFIX = "logstash-";
    public static final String DEFAULT_ELASTICSEARCH_LOG_TYPE = "logcheck";
}
