package com.sludev.logs.logcheckConfig.enums;

/**
 * Created by kervin on 2016-02-19.
 */
public enum LCCErrorMsgType
{
    NONE,
    SET_NAME_INVALID,
    CUT_OFF_DURATION_INVALID,
    LOG_BACKUP_FILE_REGEX_INVALID,
    LOG_BACKUP_FILE_REGEX_COMPS_INVALID,
    LOG_STORE_ELASTICSEARCH_CHECK_INVALID,
    DEDUPE_DURATION_INVALID,
    LOG_BACKUP_FILE_LOAD_FAILED;
}
