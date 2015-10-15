package com.sludev.logs.logcheckSampleApp.main;

import com.sludev.logs.logcheckSampleApp.entities.LogCheckAppConfig;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppConstants;
import com.sludev.logs.logcheckSampleApp.utils.LogCheckAppException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by kervin on 2015-10-13.
 */
public final class LogCheckAppInitialize
{
    private static final Logger log = LogManager.getLogger(LogCheckAppInitialize.class);

    public static LogCheckAppConfig initialize(String[] args)
    {
        log.debug(String.format("Initialize() started. %s\n", Arrays.toString(args)));

        CommandLineParser parser = new DefaultParser();
        LogCheckAppConfig resOpts = null;
        String currOutputType = null;
        String currOutputFile = null;
        String currOutputFrequency = null;
        String currOutputGenType = null;
        Boolean currAppend = null;
        Boolean currTruncate = null;

        Options options = ConfigureOptions();

        try
        {
            // Get the command line argument list from the OS
            CommandLine line;
            try
            {
                line = parser.parse(options, args);
            }
            catch( ParseException ex )
            {
                throw new LogCheckAppException(
                        String.format("Error parsing command line.'%s'",
                                ex.getMessage()), ex);
            }

            // First check for an argument file, so it can override command
            // line arguments from above.
            //
            // NB: You can't use command line arguments AND argfile at the same
            //     time
            if( line.hasOption("argfile") )
            {
                String argfile = line.getOptionValue("argfile");

                try
                {
                    BufferedReader reader = new BufferedReader(new FileReader(argfile));
                    String argLine = "";

                    do
                    {
                        argLine = reader.readLine();
                        if( argLine != null )
                        {
                            argLine = argLine.trim();
                        }
                    }
                    while( argLine != null
                            && (argLine.length() < 1 || argLine.startsWith("#")) );

                    String[] argArray = new String[0];

                    if( argLine != null )
                    {
                        argArray = argLine.split("\\s+");
                    }


                    log.debug(String.format("Initialize() : Argfile parsed : %s\n",
                            Arrays.toString(argArray)));

                    try
                    {
                        line = parser.parse(options, argArray);
                    }
                    catch( ParseException ex )
                    {
                        throw new LogCheckAppException(
                                String.format("Error parsing command line.'%s'",
                                        ex.getMessage()), ex);
                    }
                }
                catch( FileNotFoundException ex )
                {
                    log.error("Error : File not found :", ex);
                    throw new LogCheckAppException("ERROR: Argument file not found.", ex);
                }
                catch( IOException ex )
                {
                    log.error("Error : IO : ", ex);
                    throw new LogCheckAppException("ERROR: Argument file can not be read.", ex);
                }
            }

//            if( line.hasOption("conf-file") )
//            {
//                Path confFile = Paths.get(line.getOptionValue("conf-file"));
//
//                backupOpts = BackupConfigParser.readConfig(
//                        ParserUtil.readConfig(confFile, BackupToolFileFormats.BACKUPCONFIGURATION));
//            }

            if( line.getOptions().length < 1 )
            {
                // At least one option is mandatory
                throw new LogCheckAppException("No program arguments were found.");
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
                    case "output-type":
                        currOutputType = currOpt.getValue();
                        break;

                    case "output-file":
                        currOutputFile = currOpt.getValue();
                        break;

                    case "output-frequency":
                        currOutputFrequency = currOpt.getValue();
                        break;

                    case "output-generator-type":
                        currOutputGenType = currOpt.getValue();
                        break;

                    case "append":
                        currAppend = true;
                        break;

                    case "truncate":
                        currTruncate = true;
                        break;

                    case "version":
                        System.out.println(
                                String.format("logcheck-sample-app Version %s",
                                        LogCheckAppConstants.PROD_VERSION) );
                        System.exit(0);
                        break;
                }
            }

            resOpts = LogCheckAppConfig.from(currOutputType,
                    currOutputFile,
                    currOutputFrequency,
                    currOutputGenType,
                    currAppend,
                    currTruncate);
        }
        catch (LogCheckAppException ex)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            pw.append(String.format("Error : '%s'\n\n", ex.getMessage()));

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( pw, 80,
                    String.format("\njava -jar logcheck-sample-app-%s.jar ", LogCheckAppConstants.PROD_VERSION),
                    "\nThe logcheck-sample-app application can be used in a variety of options and modes.\n", options,
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

        options.addOption(Option.builder().longOpt("output-type")
                .desc("Specify the way to open the file. 'bufferedwriter' or 'filechannel'")
                .hasArg()
                .build());

        options.addOption( Option.builder().longOpt( "argfile" )
                .desc( "The name of a file containing"
                        + " the list of command-line arguments.  Only the first uncommented line is read."
                        + "\nAll other command line options are ignored." )
                .hasArg()
                .argName("ARGFILENAME")
                .build() );

        options.addOption(Option.builder().longOpt("output-file")
                .desc("Specify the file to write to.")
                .hasArg()
                .build());

        options.addOption(Option.builder().longOpt("output-frequency")
                .desc("Specify the frequency of output")
                .hasArg()
                .build());

        options.addOption(Option.builder().longOpt("output-generator-type")
                .desc("Specify the type of output. 'randomline'")
                .hasArg()
                .build());

        options.addOption(Option.builder().longOpt("append")
                .desc("Append to existing file")
                .build());

        options.addOption(Option.builder().longOpt("truncate")
                .desc("Truncate the existing file")
                .build());

        options.addOption(Option.builder().longOpt("version")
                .desc("Display version.")
                .build());

        return options;
    }
}
