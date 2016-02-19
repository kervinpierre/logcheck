package com.sludev.logs.logcheckConfig.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2016-02-12.
 */
public final class LCCConstants
{
    private static final Logger LOGGER = LogManager.getLogger(LCCConstants.class);

    public static final String LCC_DEFAULT_ALLFILES_EXT      = "*";
    public static final String LCC_DEFAULT_ALLFILES_EXT_DESC = "All Files";

    public static final String LCC_DEFAULT_CONFIG_FILENAME = "logcheck.config.xml";
    public static final String LCC_DEFAULT_CONFIG_EXT      = "*.config.xml";
    public static final String LCC_DEFAULT_CONFIG_EXT_DESC = "LogCheck Config File (*.config.xml)";

    public static final String LCC_DEFAULT_LOCK_FILENAME = "logcheck.lock.xml";
    public static final String LCC_DEFAULT_LOCK_EXT      = "*.lock.xml";
    public static final String LCC_DEFAULT_LOCK_EXT_DESC = "LogCheck Lock File (*.lock.xml)";

    public static final String LCC_DEFAULT_ARG_FILENAME = "logcheck-args.txt";
    public static final String LCC_DEFAULT_ARG_EXT      = "*.txt";
    public static final String LCC_DEFAULT_ARG_EXT_DESC = "LogCheck Arguments File (*.txt)";

    public static final String LCC_DEFAULT_LOG_FILENAME = "application.log";
    public static final String LCC_DEFAULT_LOG_EXT      = "*.log";
    public static final String LCC_DEFAULT_LOG_EXT_DESC = "Application Log File (*.log)";

    public static final String LCC_DEFAULT_STATE_FILENAME = "logcheck-state.xml";
    public static final String LCC_DEFAULT_STATE_EXT      = "*.xml";
    public static final String LCC_DEFAULT_STATE_EXT_DESC = "LogCheck State File (*.xml)";

    public static final String LCC_DEFAULT_ERROR_FILENAME = "logcheck-state.errors.xml";
    public static final String LCC_DEFAULT_ERROR_EXT      = "*.errors.xml";
    public static final String LCC_DEFAULT_ERROR_EXT_DESC = "LogCheck State File (*.errors.xml)";

    public static final String LCC_CONFIG_FILE_HIST01      = "logcheck.config.load.hist01";
    public static final String LCC_CONFIG_FILE_HIST02      = "logcheck.config.load.hist02";
    public static final String LCC_CONFIG_FILE_HIST03      = "logcheck.config.load.hist03";
    public static final String LCC_CONFIG_FILE_HIST04      = "logcheck.config.load.hist04";
    public static final String LCC_CONFIG_FILE_HIST05      = "logcheck.config.load.hist05";
    public static final int LCC_CONFIG_FILE_HIST_COUNT     = 5;
}
