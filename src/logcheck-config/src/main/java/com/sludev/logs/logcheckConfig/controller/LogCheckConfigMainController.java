package com.sludev.logs.logcheckConfig.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by kervin on 2016-02-11.
 */
public final class LogCheckConfigMainController
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckConfigMainController.class);

    @FXML
    Button buttonNext;

    @FXML
    Button buttonCancel;

    @FXML
    public void onButtonNextAction()
    {
        LOGGER.debug("Action for Button 'Next' pressed.");
    }

    @FXML
    public void onButtonCancelAction()
    {
        LOGGER.debug("Action for Button 'Cancel' pressed.");

        Stage stage = (Stage)buttonCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onMainTopFileQuit()
    {
        LOGGER.debug("Action for 'File > Quit'");

        onButtonCancelAction();
    }
}
