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

import com.sludev.logs.logcheck.exceptions.LogCheckException;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 *
 * @author Kervin Pierre
 */
@FunctionalInterface
public interface LogCheckFSWatchAction
{
    int apply(WatchEvent<Path> event, Path path ) throws LogCheckException, InterruptedException;
}