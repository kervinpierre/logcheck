package com.sludev.logs.logcheckConfig.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.prefs.Preferences;

/**
 * Created by kervin on 2016-02-12.
 */
public final class LCCAppState
{
    private static final Logger LOGGER = LogManager.getLogger(LCCAppState.class);

    private String configFile;
    private Preferences preferences;

    public Preferences getPreferences()
    {
        return preferences;
    }

    public void setPreferences(Preferences preferences)
    {
        this.preferences = preferences;
    }

    public String getConfigFile()
    {
        return configFile;
    }

    public void setConfigFile(String s)
    {
        configFile = s;
    }

    public LCCAppState()
    {
        configFile = null;
        preferences = null;
    }
}
