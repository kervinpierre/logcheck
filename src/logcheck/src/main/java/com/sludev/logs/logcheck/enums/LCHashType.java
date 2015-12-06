package com.sludev.logs.logcheck.enums;

import com.sludev.logs.logcheck.utils.LogCheckException;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by kervin on 10/27/2015.
 */
public enum LCHashType
{
    NONE,
    SHA1,
    SHA2,
    MD5;

    public static LCHashType from(String s) throws LogCheckException
    {
        LCHashType res = null;

        try
        {
            res = LCHashType.valueOf(StringUtils.upperCase(
                    StringUtils.trim(s)));
        }
        catch( IllegalArgumentException ex )
        {
            throw new LogCheckException(
                    String.format("Error parsing Log Check Hash Type '%s'", s), ex);
        }

        return res;
    }

    public static String toId(LCHashType ht)
    {
        String res = null;

        switch( ht )
        {
            case SHA1:
                res = "SHA-1";
                break;

            case SHA2:
                res = "SHA-256";
                break;
        }

        return res;
    }
}
