/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *
 * Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
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

package com.sludev.logs.logcheck.enums;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2015-11-24.
 */
public enum LCTailerResult
{
    NONE,
    SUCCESS,
    FAIL,

    /**
     * Validate was skipped
     */
    VALIDATION_SKIPPED,

    /**
     * Validation was possible, but not successful
     */
    VALIDATION_FAIL,

    /**
     * There was an error encountered while validating
     */
    VALIDATION_ERROR,

    /**
     * The file was deleted while we processed it
     */
    FILE_DELETED,

    /**
     * The file was truncated while we processed it
     */
    FILE_TRUNCATED,

    /**
     * Request re-opening the file for further processing
     */
    REOPEN,

    /**
     * The process was interrupted.  You may retry.
     */
    INTERRUPTED,

    DELAY_COMPLETED;

    private static final Logger log = LogManager.getLogger(LCTailerResult.class);
}
