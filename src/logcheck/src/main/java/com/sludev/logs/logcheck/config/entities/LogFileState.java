package com.sludev.logs.logcheck.config.entities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.LocalTime;

/**
 * Created by kervin on 10/27/2015.
 */
public class LogFileState
{
    private static final Logger log
            = LogManager.getLogger(LogFileState.class);

    private final Path file;
    private final LocalTime lastProcessedTimeStart;
    private final LocalTime lastProcessedTimeEnd;
    private final Long lastProcessedPosition;
    private final Long lastProcessedLineNumber;
    private final Long lastProcessedCharNumber;
    private final LogFileBlock lastProcessedBlock;
    private final LogFileBlock firstBlock;
}
