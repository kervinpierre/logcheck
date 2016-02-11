package com.sludev.logs.logcheckConfig.main;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.application.Application;

import java.net.URL;

/**
 * Created by kervin on 2016-02-10.
 */
public class LogCheckConfigMain extends Application
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckConfigMain.class);

    public static void main(String[] args)
    {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader fxmlLoader = new FXMLLoader();

        URL loc =
                LogCheckConfigMain.class.getClassLoader().getResource("LogCheckConfigMainWindow.fxml");

        fxmlLoader.setLocation(loc);

        final TitledPane titlePane = fxmlLoader.load();
        Scene scene = new Scene(titlePane, 600, 400);
        primaryStage.setTitle("Log Check Configuration Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
