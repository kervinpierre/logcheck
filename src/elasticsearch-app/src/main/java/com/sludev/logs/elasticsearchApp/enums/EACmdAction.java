package com.sludev.logs.elasticsearchApp.enums;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2016-03-29.
 */
public enum EACmdAction
{
    NONE,
    SCROLL;

    private static final Logger LOGGER = LogManager.getLogger( EACmdAction.class );

    public static EACmdAction from(final String action)
    {
        EACmdAction res;

        if( StringUtils.isBlank(action) )
        {
            LOGGER.debug("action cannot be null");

            return null;
        }

        res = EACmdAction.valueOf(
                StringUtils.upperCase(
                        StringUtils.trim(action)));

        return res;
    }
}
