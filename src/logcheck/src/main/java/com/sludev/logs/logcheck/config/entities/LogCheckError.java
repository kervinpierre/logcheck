package com.sludev.logs.logcheck.config.entities;

import com.sludev.logs.logcheck.enums.LCLogLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single error in an Error Document.
 *
 * Created by kervin on 2015-10-30.
 */
public final class LogCheckError
{
    private static final Logger log = LogManager.getLogger(LogCheckError.class);

    private final UUID logStateID;
    private final UUID errorID;
    private final LCLogLevel disposition;
    private final Long errorCode;
    private final String errorCodeType;
    private final Path logFilePath;
    private final String text;
    private final String summary;
    private final Instant timestamp;
    private final Exception exception;

    public UUID getErrorID()
    {
        return errorID;
    }

    public UUID getLogStateID()
    {
        return logStateID;
    }

    public LCLogLevel getDisposition()
    {
        return disposition;
    }

    public Long getErrorCode()
    {
        return errorCode;
    }

    public String getErrorCodeType()
    {
        return errorCodeType;
    }

    public Path getLogFilePath()
    {
        return logFilePath;
    }

    public String getText()
    {
        return text;
    }

    public String getSummary()
    {
        return summary;
    }

    public Instant getTimestamp()
    {
        return timestamp;
    }

    public Exception getException()
    {
        return exception;
    }

    private LogCheckError( final UUID logStateID,
                           final UUID errorID,
                           final LCLogLevel disposition,
                           final Long errorCode,
                           final String errorCodeType,
                           final Path logFilePath,
                           final String text,
                           final String summary,
                           final Instant timestamp,
                           final Exception exception )
    {
        this.logStateID = logStateID;
        this.disposition = disposition;
        this.errorCode = errorCode;
        this.errorCodeType = errorCodeType;
        this.logFilePath = logFilePath;
        this.text = text;
        this.summary = summary;
        this.timestamp = timestamp;
        this.exception = exception;
        this.errorID = errorID;
    }

    public static LogCheckError from( final UUID logStateID,
                                      final UUID errorID,
                                      final LCLogLevel disposition,
                                      final Long errorCode,
                                      final String errorCodeType,
                                      final Path logFilePath,
                                      final String text,
                                      final String summary,
                                      final Instant timestamp,
                                      final Exception exception )
    {
        LogCheckError res = new LogCheckError(logStateID,
                errorID,
                disposition,
                errorCode,
                errorCodeType,
                logFilePath,
                text,
                summary,
                timestamp,
                exception);

        return res;
    }
}
