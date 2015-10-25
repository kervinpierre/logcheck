/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *  
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *  
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.model;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.Serializable;


/**
 * Value-Object for the LogEntry class.
 *
 * @author kervin
 */
public final class LogEntryVO implements Serializable
{
    public String level;
    public String logger;
    public String message;
    public String exception;
    public String timeStamp;
    public String type;
    public String host;

    public static String toJSON(LogEntryVO vo)
    {
        String res;
        StringBuilder js = new StringBuilder();

        js.append("{");
        js.append( String.format("\"@timestamp\":\"%s\",", vo.timeStamp) );
        js.append( String.format("\"level\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.level)) );
        js.append( String.format("\"type\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.type)) );
        js.append( String.format("\"logger\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.logger)) );
        js.append( String.format("\"host\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.host)) );
        js.append( String.format("\"message\":\"%s\",",
                StringEscapeUtils.escapeJson(vo.message)) );
        js.append( String.format("\"exception\":\"%s\"",
                StringEscapeUtils.escapeJson(vo.exception)) );
        js.append("}");

        res = js.toString();

        return res;
    }
}
