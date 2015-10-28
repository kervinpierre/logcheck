package com.sludev.logs.logcheck.config.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Created by kervin on 10/27/2015.
 */
public class LogCheckState
{
    private static final Logger log
            = LogManager.getLogger(LogCheckState.class);

    private final LogFileState logFile;
    private final LocalTime saveDate;
    private final UUID id;
    private final String setName;
}
