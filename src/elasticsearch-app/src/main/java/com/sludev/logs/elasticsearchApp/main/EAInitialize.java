package com.sludev.logs.elasticsearchApp.main;

import com.sludev.logs.elasticsearchApp.entities.ESAppConfig;
import com.sludev.logs.elasticsearchApp.utils.EAConstants;
import com.sludev.logs.elasticsearchApp.utils.ESAException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;

/**
 * Created by kervin on 2016-03-29.
 */
public final class EAInitialize
{
    private static final Logger LOGGER = LogManager.getLogger(EAInitialize.class);

    public static ESAppConfig initialize(String[] args) throws ESAException
    {
        LOGGER.debug(String.format("Initialize() started. %s\n", Arrays.toString(args)));

        CommandLineParser parser = new DefaultParser();
        ESAppConfig resOpts = null;
        
        String currOutputFilePathStr = null;
        String currElasticsearchURLStr = null;
        Boolean currLogOuput = null;
        String[] currElasticsearchIndexStr = null;
        String[] currActionStr = null;

        Options options = ConfigureOptions();

        try
        {
            CommandLine line;
            try
            {
                line = parser.parse(options, args);
            }
            catch( ParseException ex )
            {
                throw new ESAException(
                        String.format("Error parsing command line.'%s'",
                                ex.getMessage()), ex);
            }
            
            if( line.getOptions().length < 1 )
            {
                // At least one option is mandatory
                throw new ESAException("No program arguments were found.");
            }

            // Argument order can be important. We may be creating THEN changing a folder's attributes.
            // It would be important to from the folder first.
            Iterator cmdI = line.iterator();
            while( cmdI.hasNext() )
            {
                Option currOpt = (Option) cmdI.next();
                String currOptName = currOpt.getLongOpt();

                switch( currOptName )
                {
                    case "elasticsearch-url":
                        currElasticsearchURLStr = currOpt.getValue();
                        break;

                    case "output-file":
                        currOutputFilePathStr = currOpt.getValue();
                        break;

                    case "elasticsearch-index":
                        currElasticsearchIndexStr = currOpt.getValues();
                        break;

                    case "action":
                        currActionStr = currOpt.getValues();
                        break;

                    case "log-output":
                        currLogOuput = true;
                        break;
                }
            }

            resOpts = ESAppConfig.from(currElasticsearchURLStr,
                    currElasticsearchIndexStr,
                    currActionStr,
                    currOutputFilePathStr,
                    currLogOuput);
        }
        catch (ESAException ex)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            pw.append(String.format("Error : '%s'\n\n", ex.getMessage()));

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( pw, 80,
                    String.format("\njava -jar elasticsearch-app-%s.jar ", EAConstants.PROD_VERSION),
                    "\nThe elasticsearch-app application can be used in a variety of options and modes.\n", options,
                    0, 2, "© SLU Dev Inc.  All Rights Reserved.",
                    true);

            System.out.println(sw.toString());
            System.exit(1);
        }

        return resOpts;
    }

    private static Options ConfigureOptions()
    {
        Options options = new Options();

        options.addOption(Option.builder().longOpt("output-file")
                .desc("Path to output file")
                .hasArg()
                .build());

        options.addOption(Option.builder().longOpt("elasticsearch-url")
                .desc("Elasticsearch URL, including protocol and port.")
                .hasArg()
                .argName("ELASTICSEARCHURL")
                .build());

        options.addOption(Option.builder().longOpt("elasticsearch-index")
                .desc("The Elasticsearch index to act on.")
                .hasArgs()
                .build());

        options.addOption(Option.builder().longOpt("action")
                .desc("The Elasticsearch action including 'scroll'.")
                .hasArgs()
                .build());

        options.addOption(Option.builder().longOpt("log-output")
                .desc("Log all server output to the screen")
                .build());

        return options;
    }
}
