/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2016] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.utils;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.net.URI;

/**
 * Created by kervin on 2016-03-08.
 */
@Plugin(name = "FSSLog4JConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public final class FSSLog4JConfigurationFactory extends ConfigurationFactory
{
    @Override
    protected String[] getSupportedTypes()
    {
        return new String[]
                {
                        "*"
                };
    }

    /**
     * @see org.apache.logging.log4j.core.config.ConfigurationFactory#getConfiguration(org.apache.logging.log4j.core.config.ConfigurationSource)
     */
    @Override
    public Configuration getConfiguration(ConfigurationSource source)
    {
        return getConfiguration(source.toString(), null);
    }

    /**
     * @see org.apache.logging.log4j.core.config.ConfigurationFactory#getConfiguration(java.lang.String,
     * java.net.URI)
     */
    @Override
    public Configuration getConfiguration(String name, URI configLocation)
    {
        ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();

        return createConfiguration(name, builder);
    }

    public static Configuration createConfiguration( final String name,
                                                     final ConfigurationBuilder<BuiltConfiguration> builder)
    {
        final Configuration configuration = new FSSLog4JConfiguration();

        return configuration;
    }
}
