package com.sludev.logs.logcheckConfig.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Created by kervin on 2016-02-13.
 */
public final class LCCPreferenceHelper
{
    private static final Logger LOGGER = LogManager.getLogger(LCCPreferenceHelper.class);

    public static void clearLoadFileHistory(Preferences pref)
    {
        pref.remove(LCCConstants.LCC_CONFIG_FILE_HIST01);
        pref.remove(LCCConstants.LCC_CONFIG_FILE_HIST02);
        pref.remove(LCCConstants.LCC_CONFIG_FILE_HIST03);
        pref.remove(LCCConstants.LCC_CONFIG_FILE_HIST04);
        pref.remove(LCCConstants.LCC_CONFIG_FILE_HIST05);
    }

    public static void addAndRotateLoadFileHistory(Preferences pref, String currentLoadFile)
    {
        Set<String> tempHist = new LinkedHashSet<>();

        if( StringUtils.isNoneBlank(currentLoadFile) )
        {
            tempHist.add(currentLoadFile);
        }

        String currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST01, null);
        if( StringUtils.isNoneBlank(currPrefValue) )
        {
            tempHist.add(currPrefValue);
        }

        currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST02, null);
        if( StringUtils.isNoneBlank(currPrefValue) )
        {
            tempHist.add(currPrefValue);
        }

        currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST03, null);
        if( StringUtils.isNoneBlank(currPrefValue) )
        {
            tempHist.add(currPrefValue);
        }

        currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST04, null);
        if( StringUtils.isNoneBlank(currPrefValue) )
        {
            tempHist.add(currPrefValue);
        }

        currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST05, null);
        if( StringUtils.isNoneBlank(currPrefValue) )
        {
            tempHist.add(currPrefValue);
        }

        clearLoadFileHistory(pref);

        Iterator<String> tempIt = tempHist.iterator();

        if( tempIt.hasNext() )
        {
            currPrefValue = tempIt.next();
            if( StringUtils.isNoneBlank(currPrefValue) )
            {
                pref.put(LCCConstants.LCC_CONFIG_FILE_HIST01, currPrefValue);
            }

            if( tempIt.hasNext() )
            {
                currPrefValue = tempIt.next();
                if( StringUtils.isNoneBlank(currPrefValue) )
                {
                    pref.put(LCCConstants.LCC_CONFIG_FILE_HIST02, currPrefValue);
                }

                if( tempIt.hasNext() )
                {
                    currPrefValue = tempIt.next();
                    if( StringUtils.isNoneBlank(currPrefValue) )
                    {
                        pref.put(LCCConstants.LCC_CONFIG_FILE_HIST03, currPrefValue);
                    }

                    if( tempIt.hasNext() )
                    {
                        currPrefValue = tempIt.next();
                        if( StringUtils.isNoneBlank(currPrefValue) )
                        {
                            pref.put(LCCConstants.LCC_CONFIG_FILE_HIST04, currPrefValue);
                        }

                        if( tempIt.hasNext() )
                        {
                            currPrefValue = tempIt.next();
                            if( StringUtils.isNoneBlank(currPrefValue) )
                            {
                                pref.put(LCCConstants.LCC_CONFIG_FILE_HIST05, currPrefValue);
                            }
                        }
                    }
                }
            }
        }
    }
}
