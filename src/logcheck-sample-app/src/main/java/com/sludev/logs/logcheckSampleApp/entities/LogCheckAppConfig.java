package com.sludev.logs.logcheckSampleApp.entities;

import com.sludev.logs.logcheckSampleApp.enums.LCSAGeneratorType;
import com.sludev.logs.logcheckSampleApp.enums.LCSAOutputType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by kervin on 2015-10-14.
 */
public final class LogCheckAppConfig
{
    private static final Logger log = LogManager.getLogger(LogCheckAppConfig.class);

    private final LCSAOutputType outputType;
    private final Path outputPath;
    private final Long outputFrequency;
    private final LCSAGeneratorType outputGeneratorType;
    private final Boolean truncate;
    private final Boolean append;

    public Path getOutputPath()
    {
        return outputPath;
    }

    public Long getOutputFrequency()
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
                              final Long outputFrequency,
                              final LCSAGeneratorType outputGeneratorType,
                              final Boolean truncate,
                              final Boolean append)
    {
        if( outputType != null )
        {
            this.outputType = outputType;
        }
        else
        {
            this.outputType = LCSAOutputType.BUFFEREDWRITER;
        }

        this.outputPath = outputPath;

        if( outputFrequency != null )
        {
            this.outputFrequency = outputFrequency;
        }
        else
        {
            this.outputFrequency = 1L;
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
                                         final Long outputFrequency,
                                         final LCSAGeneratorType outputGeneratorType,
                                         final Boolean truncate,
                                         final Boolean append)
    {
        LogCheckAppConfig res = new LogCheckAppConfig(outputType,
                                                        outputPath,
                                                        outputFrequency,
                                                        outputGeneratorType,
                                                        truncate,
                                                        append);

        return res;
    }

    public static LogCheckAppConfig from(final String outputType,
                                         final String outputPath,
                                         final String outputFrequency,
                                         final String outputGeneratorType,
                                         final Boolean truncate,
                                         final Boolean append)
    {
        LCSAOutputType ot = null;
        Path op = null;
        Long of = null;
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
            of = Long.parseLong(outputFrequency);
        }

        if( StringUtils.isNoneBlank(outputGeneratorType) )
        {
            gt = LCSAGeneratorType.from(outputGeneratorType);
        }

        LogCheckAppConfig res = from(ot, op, of, gt, truncate, append);

        return res;
    }
}
