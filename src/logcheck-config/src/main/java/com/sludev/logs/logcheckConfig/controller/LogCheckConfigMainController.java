package com.sludev.logs.logcheckConfig.controller;

import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import com.sludev.logs.logcheckConfig.util.LCCConstants;
import com.sludev.logs.logcheckConfig.util.LCCFileChooserHelper;
import com.sludev.logs.logcheckConfig.util.LCCPreferenceHelper;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
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
    MenuItem fileLoadMenuItem;

    @FXML
    MenuItem fileLoadClearHistMenuItem;

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

        refreshLoadHistoryMenu();
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

            LCCPreferenceHelper.addAndRotateLoadFileHistory(appState.getPreferences(), file.toString());

           // Platform.runLater(this::refreshLoadHistoryMenu);
            refreshLoadHistoryMenu();
        }
    }

    @FXML
    public void onFileLoadClearHistMenuItemAction()
    {
        LCCAppState appState = app.getAppState();

        LCCPreferenceHelper.clearLoadFileHistory(appState.getPreferences());
        refreshLoadHistoryMenu();
    }

    public void refreshLoadHistoryMenu()
    {
        fileLoadMenu.setDisable(true);

        LCCAppState appState = app.getAppState();
        ObservableList<MenuItem> currItems = fileLoadMenu.getItems();

        Iterator<MenuItem> tempIt = currItems.iterator();
        while( tempIt.hasNext() )
        {
            MenuItem mi = tempIt.next();
            if( mi != fileLoadMenuItem && mi != fileLoadClearHistMenuItem)
            {
                tempIt.remove();
            }
        }

        currItems.add(new SeparatorMenuItem());

        String currPrefValue = appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST01, null);
        if( currPrefValue != null )
        {
            MenuItem tempItem = new MenuItem(currPrefValue);
            currItems.add(tempItem);

            currPrefValue = appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST02, null);
            if( currPrefValue != null )
            {
                tempItem = new MenuItem(currPrefValue);
                currItems.add(tempItem);

                currPrefValue = appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST03, null);
                if( currPrefValue != null )
                {
                    tempItem = new MenuItem(currPrefValue);
                    currItems.add(tempItem);

                    currPrefValue = appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST04, null);
                    if( currPrefValue != null )
                    {
                        tempItem = new MenuItem(currPrefValue);
                        currItems.add(tempItem);

                        currPrefValue = appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST05, null);
                        if( currPrefValue != null )
                        {
                            tempItem = new MenuItem(currPrefValue);
                            currItems.add(tempItem);
                        }
                    }
                }
            }
        }


        fileLoadMenu.setDisable(false);
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