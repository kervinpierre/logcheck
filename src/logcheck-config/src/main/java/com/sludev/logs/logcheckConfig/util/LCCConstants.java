package com.sludev.logs.logcheckConfig.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2016-02-12.
 */
public final class LCCConstants
{
    private static final Logger LOGGER = LogManager.getLogger(LCCConstants.class);

    public static final String LCC_DEFAULT_CONFIG_FILENAME = "logcheck.config.xml";
    public static final String LCC_DEFAULT_CONFIG_EXT      = "*.config.xml";
    public static final String LCC_DEFAULT_CONFIG_EXT_DESC = "LogCheck Config File (*.config.xml)";

    public static final String LCC_CONFIG_FILE_HIST01      = "logcheck.config.load.hist01";
    public static final String LCC_CONFIG_FILE_HIST02      = "logcheck.config.load.hist02";
    public static final String LCC_CONFIG_FILE_HIST03      = "logcheck.config.load.hist03";
    public static final String LCC_CONFIG_FILE_HIST04      = "logcheck.config.load.hist04";
    public static final String LCC_CONFIG_FILE_HIST05      = "logcheck.config.load.hist05";
    public static final int LCC_CONFIG_FILE_HIST_COUNT     = 5;
}
