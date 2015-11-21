package com.sludev.logs.logcheckSampleApp.generator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by kervin on 2015-10-14.
 */
public final class RandomLineGenerator implements IDataGenerator
{
    private static final Logger log = LogManager.getLogger(RandomLineGenerator.class);

    @Override
    public String getLine(String data)
    {
        String res;
        String currData = data;

        if( currData == null )
        {
            currData = "Random Line Data";
        }

        res = String.format("%s : %s : %s\n", Instant.now(), currData, UUID.randomUUID());

        return res;
    }
}
