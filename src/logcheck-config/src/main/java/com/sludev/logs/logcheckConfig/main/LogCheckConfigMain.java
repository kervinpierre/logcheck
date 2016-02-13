package com.sludev.logs.logcheckConfig.main;

import com.sludev.logs.logcheckConfig.controller.LogCheckConfigMainController;
import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;

import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Created by kervin on 2016-02-10.
 */
public class LogCheckConfigMain extends Application
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckConfigMain.class);

    private final LCCAppState appState;

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
        Application.launch(args);
    }

    @Override
    public void init() throws Exception
    {
        super.init();
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

        Scene scene = new Scene(pane, 600, 400);
        primaryStage.setTitle("Log Check Configuration Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();

        LOGGER.debug("Application is stopping...");
    }
}
