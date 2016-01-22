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

import com.sludev.logs.logcheck.enums.LCIndexNameFormat;

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

    public static final int DEFAULT_LCBLOCK_SAMPLE_SIZE = 80;

    public static final String DEFAULT_MULTILINE_ROW_START_PATTERN
            = "^[\\s]*([A-Z]+)[\\s]+\\[([\\p{Alnum}:,\\s-]+)\\][\\s]+([\\p{Alnum}\\.]+)[\\s]+([\\w\\.-]+)[\\s]*$";

    public static final String DEFAULT_MULTILINE_ROW_END_PATTERN
            = "^\\[logging row end\\]$";

    public static final String DEFAULT_MULTILINE_COL_END_PATTERN
            = "^\\[logging column end\\]$";

    public static final String DEFAULT_MULTILINE_IGNORE_LINE = "^[=]{5,}$";

    //public static final int    DEFAULT_LOG_READ_BUFFER_SIZE_BYTES = 4096;
    // For testing...
    public static final int    DEFAULT_LOG_READ_BUFFER_SIZE_BYTES = 64;

    public static final LCIndexNameFormat DEFAULT_ELASTICSEARCH_INDEX_NAME_FORMAT
                                        = LCIndexNameFormat.PREFIX_DATE;
    public static final String DEFAULT_ELASTICSEARCH_INDEX_PREFIX = "logstash-";
    public static final String DEFAULT_LOG_TYPE = "logcheck";

    public static final String DEFAULT_SET_NAME = "MISSING-SET-NAME";

    public static int MAX_ID_BLOCK_SIZE = 10000000;
    public static int DEFAULT_ID_BLOCK_SIZE = 1000;

    public static int DEFAULT_DEDUPE_LOGS_BEFORE_WRITE = 5;
    public static int MAX_DEDUPE_LOGS_PER_FILE = Integer.MIN_VALUE;

    /**
     * Number of deduplication log files to keep before deleting the older files.
     *
     * This can be changed on the command line.
     */
    public static int DEFAULT_DEDUPE_LOG_FILES_ROTATE = 3;

    /**
     * The maximum number of deduplication log files to keep on disk.
     * More log files than this number will result in a program error.
     */
    public static int MAX_DEDUPE_LOG_FILES = 1000;

    public static String DEFAULT_DEDUPE_LOG_FILE_NAME = "LogEntryDeDupe";

    public static int DEFAULT_SAVE_STATE_INTERVAL_SECONDS = 15;
}
