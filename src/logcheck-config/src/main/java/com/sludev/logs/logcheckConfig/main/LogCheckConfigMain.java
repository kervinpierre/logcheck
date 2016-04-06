package com.sludev.logs.logcheckConfig.main;

import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheckConfig.controller.LogCheckConfigMainController;
import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.prefs.Preferences;

/**
 * Created by kervin on 2016-02-10.
 */
public class LogCheckConfigMain extends Application
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckConfigMain.class);

    private final LCCAppState appState;
    private static String[] cmdLine = null;

    public LCCAppState getAppState()
    {
        return appState;
    }

    public LogCheckConfigMain()
    {
        this.appState = new LCCAppState();

        Preferences prefs = Preferences.userRoot().node(LogCheckConfigMain.class.getName());
        this.appState.setPreferences(prefs);
    }

    public static void main(String[] args)
    {
        cmdLine = args;
        Application.launch(args);
    }

    @Override
    public void init() throws Exception
    {
        super.init();

        initialize(appState, cmdLine);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader fxmlLoader = new FXMLLoader();

        URL loc =
                LogCheckConfigMain.class.getClassLoader().getResource("LogCheckConfigMainWindow.fxml");

        fxmlLoader.setLocation(loc);

        final Pane pane = fxmlLoader.load();

        LogCheckConfigMainController controller = fxmlLoader.getController();
        controller.setApp(this);
        controller.internalInit();

        Scene scene = new Scene(pane, 900, 700);
        primaryStage.setTitle("Log Check Configuration Application");
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(700);
        primaryStage.setMinWidth(900);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();

        LOGGER.debug("Application is stopping...");
    }

    private static Options configureOptions()
    {
        Options options = new Options();

        options.addOption(Option.builder().longOpt("load-config-file")
                .desc("Configuration file.")
                .hasArg()
                .argName("CONFFILE")
                .build());

        options.addOption(Option.builder().longOpt("load-arg-file")
                .desc("Argument file.")
                .hasArg()
                .argName("ARGFILE")
                .build());

        return options;
    }

    public static void initialize(final LCCAppState appState,
                                  final String[] args)
    {
        CommandLineParser parser = new DefaultParser();
        Options options = configureOptions();

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
                throw new LogCheckException(
                        String.format("Error parsing command line.'%s'",
                                ex.getMessage()), ex);
            }
            Iterator cmdI = line.iterator();
            while( cmdI.hasNext() )
            {
                Option currOpt = (Option) cmdI.next();
                String currOptName = currOpt.getLongOpt();

                switch( currOptName )
                {
                    case "load-config-file":
                        appState.setConfigFile(currOpt.getValue());
                        break;

                    case "load-arg-file":
                        appState.setArgFile(currOpt.getValue());
                        break;
                }
            }
        }
        catch (LogCheckException ex)
        {

            try( StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw))
            {
                pw.append(String.format("Error : '%s'\n\n", ex.getMessage()));

                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(pw, 80, "\njava -jar logcheckConfig-0.9.jar ",
                        "\nThe logcheckConfig application can be used in a variety of options and modes.\n", options,
                        0, 2, "© All Rights Reserved.",
                        true);

                System.out.println(sw.toString());
            }
            catch( IOException iex )
            {
                LOGGER.debug("", iex);
            }

            System.exit(1);
        }
    }
}
