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

package com.sludev.logs.logcheck.config.entities.impl;

import com.sludev.logs.logcheck.config.entities.LogCheckStateStatusBase;
import com.sludev.logs.logcheck.config.entities.LogFileBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Instant;

/**
 * Track the "status" of the files on disk.  This can be used to retrieve tracked
 * file data as more log files are being processed
 * Created by kervin on 2016-03-18.
 */
public final class LogFileStatus extends LogCheckStateStatusBase
{
    private static final Logger LOGGER = LogManager.getLogger(LogFileStatus.class);

    private final Path m_path;
    private final LogFileBlock m_fullFileBlock;


    public LogFileBlock getFullFileBlock()
    {
        return m_fullFileBlock;
    }

    public Path getPath()
    {
        return m_path;
    }

    private LogFileStatus(final Path path,
                          final LogFileBlock fullFileBlock,
                          final Instant processedStamp,
                          final Boolean processed)
    {
        super(processedStamp, processed);

        this.m_path = path;
        this.m_fullFileBlock = fullFileBlock;
    }

    public static LogFileStatus from(final Path path,
                                      final LogFileBlock fullFileBlock,
                                      final Instant processedStamp,
                                     final Boolean processed)
    {
        LogFileStatus res = new LogFileStatus(path,
                                                fullFileBlock,
                                                processedStamp,
                                                processed);

        return res;
    }
}
