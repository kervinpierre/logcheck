package com.sludev.logs.logcheckSampleApp.entities;

import com.sludev.logs.logcheckSampleApp.enums.LCSAGeneratorType;
import com.sludev.logs.logcheckSampleApp.enums.LCSAOutputType;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import com.sludev.logs.logcheckSampleApp.utils.ParseNumberWithSuffix;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Log Check Testing Applications configuration class.
 *
 * Created by kervin on 2015-10-14.
 */
public final class LogCheckAppConfig
{
    private static final Logger log = LogManager.getLogger(LogCheckAppConfig.class);

    private final LCSAOutputType outputType;
    private final Path outputPath;
    private final Pair<Long,TimeUnit> outputFrequency;
    private final Long rotateAfterCount;
    private final Long maxBackups;
    private final LCSAGeneratorType outputGeneratorType;
    private final Boolean truncate;
    private final Boolean append;
    private final Boolean deleteLogs;
    private final Boolean confirmDeletes;

    public Boolean getConfirmDeletes()
    {
        return confirmDeletes;
    }

    public Path getOutputPath()
    {
        return outputPath;
    }

    public Boolean getDeleteLogs()
    {
        return deleteLogs;
    }

    public Long getRotateAfterCount()
    {
        return rotateAfterCount;
    }

    public Long getMaxBackups()
    {
        return maxBackups;
    }

    public Pair<Long,TimeUnit> getOutputFrequency()
    {
        return outputFrequency;
    }

    public LCSAGeneratorType getOutputGeneratorType()
    {
        return outputGeneratorType;
    }

    public Boolean getTruncate()
    {
        return truncate;
    }

    public Boolean getAppend()
    {
        return append;
    }

    public LCSAOutputType getOutputType()
    {
        return outputType;
    }

    private LogCheckAppConfig(final LCSAOutputType outputType,
                              final Path outputPath,
                              final Pair<Long,TimeUnit> outputFrequency,
                              final LCSAGeneratorType outputGeneratorType,
                              final Long rotateAfterCount,
                              final Long maxBackups,
                              final Boolean truncate,
                              final Boolean append,
                              final Boolean deleteLogs,
                              final Boolean confirmDeletes)
    {
        if( outputType != null )
        {
            this.outputType = outputType;
        }
        else
        {
            this.outputType = LCSAOutputType.BUFFEREDWRITER;
        }

        if( rotateAfterCount != null )
        {
            this.rotateAfterCount = rotateAfterCount;
        }
        else
        {
            this.rotateAfterCount = null;
        }

        if( maxBackups != null )
        {
            this.maxBackups = maxBackups;
        }
        else
        {
            this.maxBackups = null;
        }

        if( deleteLogs != null )
        {
            this.deleteLogs = deleteLogs;
        }
        else
        {
            this.deleteLogs = null;
        }

        this.outputPath = outputPath;

        if( outputFrequency != null )
        {
            this.outputFrequency = outputFrequency;
        }
        else
        {
            this.outputFrequency = Pair.of(1L, TimeUnit.SECONDS);
        }

        if( outputGeneratorType != null )
        {
            this.outputGeneratorType = outputGeneratorType;
        }
        else
        {
            this.outputGeneratorType = LCSAGeneratorType.RANDOMLINE;
        }

        if( truncate != null )
        {
            this.truncate = truncate;
        }
        else
        {
            this.truncate = false;
        }

        if( confirmDeletes != null )
        {
            this.confirmDeletes = confirmDeletes;
        }
        else
        {
            this.confirmDeletes = false;
        }

        if( append != null )
        {
            this.append = append;
        }
        else if( this.truncate )
        {
            this.append = false;
        }
        else
        {
            this.append = true;
        }
    }

    public static LogCheckAppConfig from(final LCSAOutputType outputType,
                                         final Path outputPath,
                                         final Pair<Long,TimeUnit> outputFrequency,
                                         final LCSAGeneratorType outputGeneratorType,
                                         final Long rotateAfterCount,
                                         final Long maxBackups,
                                         final Boolean truncate,
                                         final Boolean append,
                                         final Boolean deleteLogs,
                                         final Boolean confirmDeletes)
    {
        LogCheckAppConfig res = new LogCheckAppConfig(outputType,
                                                        outputPath,
                                                        outputFrequency,
                                                        outputGeneratorType,
                                                        rotateAfterCount,
                                                        maxBackups,
                                                        truncate,
                                                        append,
                                                        deleteLogs,
                                                        confirmDeletes);

        return res;
    }

    public static LogCheckAppConfig from(final String outputType,
                                         final String outputPath,
                                         final String outputFrequency,
                                         final String outputGeneratorType,
                                         final String rotateAfterCount,
                                         final String maxBackups,
                                         final Boolean truncate,
                                         final Boolean append,
                                         final Boolean deleteLogs,
                                         final Boolean confirmDeletes) throws LogCheckAppException
    {
        LCSAOutputType ot = null;
        Path op = null;
        Pair<Long,TimeUnit> of = null;
        Long rac = null;
        Long mbu = null;
        LCSAGeneratorType gt = null;

        if( StringUtils.isNoneBlank(outputType) )
        {
            ot = LCSAOutputType.from(outputType);
        }

        if( StringUtils.isNoneBlank(outputPath) )
        {
            op = Paths.get(outputPath);
        }

        if( StringUtils.isNoneBlank(outputFrequency) )
        {
            of = ParseNumberWithSuffix.parseIntWithTimeUnits(outputFrequency);
        }

        if( StringUtils.isNoneBlank(outputGeneratorType) )
        {
            gt = LCSAGeneratorType.from(outputGeneratorType);
        }

        if( StringUtils.isNoneBlank(rotateAfterCount) )
        {
            rac = ParseNumberWithSuffix.parseIntWithMagnitude(rotateAfterCount);
        }

        LogCheckAppConfig res = from(ot, op, of, gt, rac, mbu,
                                        truncate, append, deleteLogs, confirmDeletes);

        return res;
    }
}
