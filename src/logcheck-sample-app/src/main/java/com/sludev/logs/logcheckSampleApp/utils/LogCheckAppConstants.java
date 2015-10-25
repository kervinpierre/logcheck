package com.sludev.logs.logcheckSampleApp.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2015-10-14.
 */
public final class LogCheckAppConstants
{
    private static final Logger log = LogManager.getLogger(LogCheckAppConstants.class);

    public static String PROD_VERSION = "0.9";
    public static long MAX_BACKUP_FILES = 10000;
}
