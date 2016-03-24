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
    private static final Logger LOGGER = LogManager.getLogger(LogCheckAppConfig.class);

    private final LCSAOutputType outputType;
    private final Path outputPath;
    private final Pair<Long,TimeUnit> outputFrequency;
    private final Long rotateAfterCount;
    private final Long stopAfterCount;
    private final Long maxBackups;
    private final Integer randomWaitMin;
    private final Integer randomWaitMax;
    private final Integer startLineNumber;
    private final LCSAGeneratorType outputGeneratorType;
    private final Boolean truncate;
    private final Boolean append;
    private final Boolean deleteLogs;
    private final Boolean confirmDeletes;
    private final Boolean outputToScreen;

    public Integer getStartLineNumber()
    {
        return startLineNumber;
    }

    public Integer getRandomWaitMin()
    {
        return randomWaitMin;
    }

    public Integer getRandomWaitMax()
    {
        return randomWaitMax;
    }

    public Boolean getOutputToScreen()
    {
        return outputToScreen;
    }

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

    public Long getStopAfterCount()
    {
        return stopAfterCount;
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
                              final Long stopAfterCount,
                              final Long maxBackups,
                              final Integer randomWaitMax,
                              final Integer randomWaitMin,
                              final Integer startLineNumber,
                              final Boolean truncate,
                              final Boolean append,
                              final Boolean deleteLogs,
                              final Boolean confirmDeletes,
                              final Boolean outputToScreen)
    {
        if( outputToScreen != null )
        {
            this.outputToScreen = outputToScreen;
        }
        else
        {
            this.outputToScreen= false;
        }

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

        if( stopAfterCount != null )
        {
            this.stopAfterCount = stopAfterCount;
        }
        else
        {
            this.stopAfterCount = null;
        }

        if( maxBackups != null )
        {
            this.maxBackups = maxBackups;
        }
        else
        {
            this.maxBackups = null;
        }

        if( randomWaitMax != null )
        {
            this.randomWaitMax = randomWaitMax;
        }
        else
        {
            this.randomWaitMax = null;
        }

        if( randomWaitMin != null )
        {
            this.randomWaitMin = randomWaitMin;
        }
        else
        {
            this.randomWaitMin = null;
        }

        if( startLineNumber != null )
        {
            this.startLineNumber = startLineNumber;
        }
        else
        {
            this.startLineNumber = null;
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
                                         final Long stopAfterCount,
                                         final Long maxBackups,
                                         final Integer randomWaitMin,
                                         final Integer randomWaitMax,
                                         final Integer startLineNumber,
                                         final Boolean truncate,
                                         final Boolean append,
                                         final Boolean deleteLogs,
                                         final Boolean confirmDeletes,
                                         final Boolean outputToScreen)
    {
        LogCheckAppConfig res = new LogCheckAppConfig(outputType,
                                                        outputPath,
                                                        outputFrequency,
                                                        outputGeneratorType,
                                                        rotateAfterCount,
                                                        stopAfterCount,
                                                        maxBackups,
                                                        randomWaitMin,
                                                        randomWaitMax,
                                                        startLineNumber,
                                                        truncate,
                                                        append,
                                                        deleteLogs,
                                                        confirmDeletes,
                                                        outputToScreen);

        return res;
    }

    public static LogCheckAppConfig from(final String outputType,
                                         final String outputPath,
                                         final String outputFrequency,
                                         final String outputGeneratorType,
                                         final String rotateAfterCount,
                                         final String stopAfterCount,
                                         final String maxBackups,
                                         final String randomWaitMin,
                                         final String randomWaitMax,
                                         final String startLineNumber,
                                         final Boolean truncate,
                                         final Boolean append,
                                         final Boolean deleteLogs,
                                         final Boolean confirmDeletes,
                                         final Boolean outputToScreen) throws LogCheckAppException
    {
        LCSAOutputType ot = null;
        Path op = null;
        Pair<Long,TimeUnit> of = null;
        Long rac = null;
        Long sac = null;
        Long mbu = null;
        Integer rwmin = null;
        Integer rwmax = null;
        Integer sln = null;
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

        if( StringUtils.isNoneBlank(stopAfterCount) )
        {
            sac = ParseNumberWithSuffix.parseIntWithMagnitude(stopAfterCount);
        }

        if( StringUtils.isNoneBlank(maxBackups) )
        {
            try
            {
                mbu = Long.parseLong(maxBackups);
            }
            catch( NumberFormatException ex )
            {
                LOGGER.debug(String.format("Max Backups parse error '%s'", maxBackups));
            }
        }

        if( StringUtils.isNoneBlank(randomWaitMin) )
        {
            try
            {
                rwmin = Integer.parseInt(randomWaitMin);
            }
            catch( NumberFormatException ex )
            {
                LOGGER.debug(String.format("Min Random Wait parse error '%s'", randomWaitMin));
            }
        }

        if( StringUtils.isNoneBlank(randomWaitMax) )
        {
            try
            {
                rwmax = Integer.parseInt(randomWaitMax);
            }
            catch( NumberFormatException ex )
            {
                LOGGER.debug(String.format("Max Random Wait parse error '%s'", randomWaitMax));
            }
        }

        if( StringUtils.isNoneBlank(startLineNumber) )
        {
            try
            {
                sln = Integer.parseInt(startLineNumber);
            }
            catch( NumberFormatException ex )
            {
                LOGGER.debug(String.format("Max Random Wait parse error '%s'", startLineNumber));
            }
        }

        LogCheckAppConfig res = from(ot, op, of, gt, rac, sac, mbu, rwmin, rwmax, sln,
                                        truncate, append, deleteLogs, confirmDeletes,
                                        outputToScreen);

        return res;
    }
}
