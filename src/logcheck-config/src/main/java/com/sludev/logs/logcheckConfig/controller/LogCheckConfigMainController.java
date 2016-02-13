package com.sludev.logs.logcheckConfig.controller;

import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import com.sludev.logs.logcheckConfig.util.LCCConstants;
import com.sludev.logs.logcheckConfig.util.LCCFileChooserHelper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by kervin on 2016-02-11.
 */
public final class LogCheckConfigMainController implements Initializable
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckConfigMainController.class);

    private LogCheckConfigMain app;

    public LogCheckConfigMain getApp()
    {
        return app;
    }

    public void setApp(LogCheckConfigMain app)
    {
        this.app = app;
    }

    public LogCheckConfigMainController()
    {
        app = null;
    }

    @FXML
    TextField generalTabConfigFileTextField;

    @FXML
    Button buttonNext;

    @FXML
    Button buttonCancel;

    @FXML
    Menu fileLoadMenu;

    @FXML
    Button generalTabConfigFileBrowseButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        LOGGER.debug("initialize() called.");

        generalTabConfigFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
            {
                generalTabConfigFileTextField.setTooltip(new Tooltip(newValue));
            });
    }

    public void internalInit()
    {

        LCCAppState appState = app.getAppState();
        generalTabConfigFileTextField.setText(
                appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST01, ""));
    }

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

    @FXML
    public void onFileLoadMenuShowingAction()
    {
        ;
    }

    @FXML
    public void onMainTopFileLoad()
    {
        LOGGER.debug("Action for 'File > Load'");

        Stage stage = (Stage)buttonCancel.getScene().getWindow();
        List<Pair<String,String>> exts = new ArrayList<>();

        exts.add(Pair.of(LCCConstants.LCC_DEFAULT_CONFIG_EXT_DESC,
                                LCCConstants.LCC_DEFAULT_CONFIG_EXT));

        LCCAppState appState = app.getAppState();

        Path file = LCCFileChooserHelper.showFileChooser(stage, exts, "Choose the config file for loading", null);

        if( file != null )
        {
            generalTabConfigFileTextField.setText(file.toString());

            appState.getPreferences().put(LCCConstants.LCC_CONFIG_FILE_HIST01, file.toString());
        }
    }

    @FXML
    public void generalTabConfigFileBrowseButtonAction()
    {
        LOGGER.debug("Action for 'General Tab > Browse...'");

        onMainTopFileLoad();
    }

    @FXML
    public void onGeneralTabConfigFileTextFieldAction()
    {
        LOGGER.debug("Action for 'General Tab > Log Config Text Field'");
    }
}