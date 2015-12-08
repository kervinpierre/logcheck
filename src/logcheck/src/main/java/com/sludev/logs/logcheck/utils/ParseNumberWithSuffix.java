package com.sludev.logs.logcheck.utils;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse input strings
 * Created by kervin on 10/17/2015.
 */
public final class ParseNumberWithSuffix
{
    private static final Logger log = LogManager.getLogger(ParseNumberWithSuffix.class);

    public static Pair<Long,TimeUnit> parseIntWithTimeUnits(String n) throws LogCheckException
    {
        Pair<Long,TimeUnit> res;
        Long resLong;

        if( StringUtils.isBlank(n) )
        {
            log.debug(String.format("Invalid number '%s'", n));
            return null;
        }

        Pattern lp = Pattern.compile("(\\d+)(.*)");

        String currLong = StringUtils.upperCase(
                StringUtils.deleteWhitespace(n));

        String lastChar = null;

        Matcher lpm = lp.matcher(currLong);
        if( lpm.matches() )
        {
            currLong = lpm.group(1);
            lastChar = lpm.group(2);
        }
        else
        {
            String errMsg
                    = String.format("Invalid integer with time units '%s'", n);

            log.debug(errMsg);

            throw new LogCheckException(errMsg);
        }

        try
        {
            resLong = Long.parseLong(currLong);
        }
        catch(NumberFormatException ex)
        {
            log.debug(String.format("Invalid number '%s'", n));
            return null;
        }

        if( resLong < 1 )
        {
            log.debug(String.format("Invalid number '%s'", n));
            return null;
        }

        switch(lastChar)
        {

            case "MS":
                lastChar = "MILLISECONDS";
                break;

            case "S":
                lastChar = "SECONDS";
                break;

            case "M":
                lastChar = "MINUTES";
                break;

            case "H":
                lastChar = "HOURS";
                break;

            case "D":
                lastChar = "DAYS";
                break;

            default:
                lastChar = "SECONDS";
                log.warn(String.format(
                        "Invalid time units '%s'. Defaulting to 'S' for seconds",
                        lastChar));
                break;
        }

        TimeUnit resUnit = TimeUnit.SECONDS;

        try
        {
            resUnit = TimeUnit.valueOf(lastChar);
        }
        catch( IllegalArgumentException ex )
        {
            log.debug(String.format("Invalid time units '%s'. Defaulting to 'S' for seconds", lastChar));
        }

        res = Pair.of(resLong, resUnit);

        return res;
    }

    public static Long parseIntWithMagnitude(String n)
    {
        Long res;

        if( StringUtils.isBlank(n) )
        {
            log.debug(String.format("Invalid number '%s'", n));
            return null;
        }

        String currLong = StringUtils.upperCase(
                                StringUtils.deleteWhitespace(n));
        String lastChar = StringUtils.right(currLong, 1);

        if( StringUtils.isNumeric(lastChar) == false )
        {
            currLong = StringUtils.removeEnd(currLong, lastChar);
        }

        try
        {
            res = Long.parseLong(currLong);
        }
        catch(NumberFormatException ex)
        {
            log.debug(String.format("Invalid number '%s'", n));
            return null;
        }

        if( res < 1 )
        {
            log.debug(String.format("Invalid number '%s'", n));
            return null;
        }

        if( StringUtils.isNumeric(lastChar) == false )
        {
            switch(lastChar)
            {
                case "K":
                    res *= 1024;
                    break;

                case "M":
                    res *= 1024 * 1024;
                    break;

                case "G":
                    res *= 1024 * 1024 * 1024;
                    break;

                default:
                {
                    log.debug(String.format("Invalid suffix '%s'", lastChar));
                    return null;
                }
            }
        }

        return res;
    }
}
