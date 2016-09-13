package com.sludev.logs.logcheck.log.impl.builder;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 8/25/2016.
 */
public class WindowsEventBuilderTest
{
    @Before
    public void setUp()
            throws Exception
    {

    }

    @After
    public void tearDown()
            throws Exception
    {

    }

    @Test
    @Ignore
    public void handleLogLine01()
            throws Exception
    {
        List<Pattern> ignore = new ArrayList<>();

        WindowsEventBuilder bld = WindowsEventBuilder.from(null, null);

        String json = "";

        bld.handleLogLine(json);
    }

}