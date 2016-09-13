package com.sludev.logs.logcheck.log;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * Created by kervin on 10/23/2015.
 */
public interface ILogEntryBuilder
{

    void handleLogLine(String currLineStr) throws InterruptedException;

    public static boolean ignoreLine(List<Pattern> logIngoreLine, String line)
    {
        boolean res = false;

        if( logIngoreLine != null )
        {
            for(Pattern pt : logIngoreLine)
            {
                if(pt.matcher(line).matches())
                {
                    // Ignore this line
                    return true;
                }
            }
        }

        return res;
    }
}
