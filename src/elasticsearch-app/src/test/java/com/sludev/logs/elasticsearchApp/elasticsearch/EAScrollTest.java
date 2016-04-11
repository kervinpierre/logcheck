package com.sludev.logs.elasticsearchApp.elasticsearch;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;
import org.junit.rules.TestWatcher;
import org.junit.runners.MethodSorters;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 4/11/2016.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EAScrollTest
{
    private static final Logger LOGGER
            = LogManager.getLogger(EAScrollTest.class);

    private Properties m_testProperties;
    private Path outFileDir = null;

    @Before
    public void setUp()
            throws Exception
    {

    }

    @After
    public void tearDown()
            throws Exception
    {
        if( outFileDir != null )
        {
            FileUtils.deleteDirectory(outFileDir.toFile());
        }
    }

    @Test
    public void A001_doScroll()
            throws Exception
    {
        URL url = new URL("http://sludev01:9200");
        List<String> index = new ArrayList<>();
        outFileDir = Files.createTempDirectory("logcheck-unit-tests");

        index.add("logstash-*");

        EAScroll.doScroll(url,
                index,
                outFileDir.resolve("elasticSearch-out.txt"),
                false);
    }

}