package com.sludev.logs.logcheckConfig.controller;

import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import com.sludev.logs.logcheckConfig.handler.LCCBrowseHandler;
import com.sludev.logs.logcheckConfig.handler.LCCTabHandler;
import com.sludev.logs.logcheckConfig.handler.LCCValidateHandler;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import com.sludev.logs.logcheckConfig.util.LCCConstants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        LOGGER.debug("initialize() called.");

        generalTabConfigFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            generalTabConfigFileTextField.setTooltip(new Tooltip(newValue));
        });

        generalTabLockFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            generalTabLockFileTextField.setTooltip(new Tooltip(newValue));
        });

        generalTabArgFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            generalTabArgFileTextField.setTooltip(new Tooltip(newValue));
        });

        logFileTargetFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            logFileTargetFileTextField.setTooltip(new Tooltip(newValue));
        });

        tailerGeneralStateFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            tailerGeneralStateFileTextField.setTooltip(new Tooltip(newValue));
        });

        tailerGeneralErrorFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            tailerGeneralErrorFileTextField.setTooltip(new Tooltip(newValue));
        });

        rotateTabLogBackupDirTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            rotateTabLogBackupDirTextField.setTooltip(new Tooltip(newValue));
        });

        dedupTabDirTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            dedupTabDirTextField.setTooltip(new Tooltip(newValue));
        });

        logStoreOutputFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            logStoreOutputFileTextField.setTooltip(new Tooltip(newValue));
        });

        generalTabSetNameTextField.focusedProperty().addListener((observable, oldValue, newValue) ->
            {
              if(generalTabSetNameTextField.isFocused() == false)
              {
                    LOGGER.debug("set name validate on lost focus");
              }
           });

        debugTabFlagsListView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void internalInit()
    {
        LCCAppState appState = app.getAppState();
        generalTabConfigFileTextField.setText(
                appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST01, ""));

        LCCBrowseHandler.refreshLoadHistoryMenu(app, fileLoadMenu,
                fileLoadMenuItem, fileLoadClearHistMenuItem, generalTabConfigFileTextField);
    }

    @FXML
    private AnchorPane mainPane;

    @FXML
    private MenuBar mainTopMenuBar;

    @FXML
    private Menu mainTopFileMenu;

    @FXML
    private Menu fileLoadMenu;

    @FXML
    private MenuItem fileLoadMenuItem;

    @FXML
    private MenuItem fileLoadClearHistMenuItem;

    @FXML
    private MenuItem fileQuitMenuItem;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab tabWelcome;

    @FXML
    private Label labelWelcome;

    @FXML
    private Tab tabGeneral;

    @FXML
    private TextField generalTabConfigFileTextField;

    @FXML
    private Button generalTabConfigFileBrowseButton;

    @FXML
    private TextField generalTabLockFileTextField;

    @FXML
    private Button generalTabLockFileBrowseButton;

    @FXML
    private CheckBox generalTabServiceCheck;

    @FXML
    private TextField generalTabArgFileTextField;

    @FXML
    private Button generalTabArgFileBrowseButton;

    @FXML
    private Spinner<?> generalTabStopAfterSpinner;

    @FXML
    private ChoiceBox<?> generalTabStopAfterUnitsChoice;

    @FXML
    private TextField generalTabSetNameTextField;

    @FXML
    private Button generalTabSetNameValidateButton;

    @FXML
    private Tab tabLogFile;

    @FXML
    private TextField logFileTargetFileTextField;

    @FXML
    private Button logFileTargetFileBrowseButton;

    @FXML
    private CheckBox logFileReadOnlyCheckbox;

    @FXML
    private TextField logFileCutoffDurationTextField;

    @FXML
    private Button logFileCutoffDurationValidateButton;

    @FXML
    private DatePicker logFileCutoffDate;

    @FXML
    private CheckBox logFileValidateCheckbox;

    @FXML
    private Tab tabTailerGeneral;

    @FXML
    private Spinner<?> tailerGeneralPollIntervalSpinner;

    @FXML
    private ChoiceBox<?> tailerGeneralPollIntervalUnitsChoiceBox;

    @FXML
    private TextField tailerGeneralStateFileTextField;

    @FXML
    private Button tailerGeneralStateFileBrowseButton;

    @FXML
    private CheckBox tailerGeneralFileFromStartCheckbox;

    @FXML
    private CheckBox tailerGeneralContinueCheckbox;

    @FXML
    private CheckBox tailerGeneralReOpenCheckbox;

    @FXML
    private CheckBox tailerGeneralStartPositionIgnoreErrCheckbox;

    @FXML
    private CheckBox tailerGeneralEOFStopCheckbox;

    @FXML
    private TextField tailerGeneralErrorFileTextField;

    @FXML
    private Button tailerGeneralErrorFileBrowseButton;

    @FXML
    private CheckBox tailerGeneralSaveStateCheckbox;

    @FXML
    private Tab tabTailerRotate;

    @FXML
    private TextField rotateTabLogBackupDirTextField;

    @FXML
    private Button rotateTabLogBackupDirBrowseButton;

    @FXML
    private TextField rotateTabStateFileTextField;

    @FXML
    private Button rotateTabStateFileBrowseButton;

    @FXML
    private CheckBox rotateTabValidateLogCheckbox;

    @FXML
    private CheckBox rotateTabReadBackupLogsCheckbox;

    @FXML
    private CheckBox rotateTabReadPriorLogsCheckbox;

    @FXML
    private TextField rotateTabLogFileRegexTextField;

    @FXML
    private Button rotateTabLogFileRegexValidateButton;

    @FXML
    private TextField rotateTabLogFileCompTextField;

    @FXML
    private Button rotateTabLogFileCompValidateButton;

    @FXML
    private Tab tabDuplication;

    @FXML
    private TextField dedupTabDirTextField;

    @FXML
    private Button dedupTabDirBrowseButton;

    @FXML
    private TextField dedupTabDurationTextField;

    @FXML
    private Button dedupTabDurationValidateButton;

    @FXML
    private Spinner<?> dedupTabLogsPerFileSpinner;

    @FXML
    private Tab tabLogStore;

    @FXML
    private ChoiceBox<?> logStoreTabLogStore01Choicebox;

    @FXML
    private ChoiceBox<?> logStoreTabLogStore02Choicebox;

    @FXML
    private ChoiceBox<?> logStoreTabLogStore03Choicebox;

    @FXML
    private TextField logStoreOutputFileTextField;

    @FXML
    private TextFlow mainValidationTextFlow;

    @FXML
    private Button LogStoreOutputFileBrowseButton;

    @FXML
    private TextField logStoreElastisSearchTextField;

    @FXML
    private Button logStoreElastiSearchCheckButton;

    @FXML
    private Tab tabLogBuilder;

    @FXML
    private ChoiceBox<?> logBuilderTabChoicebox;

    @FXML
    private Tab tabDebugging;

    @FXML
    private ListView<?> debugTabFlagsListView;

    @FXML
    private Button buttonNext;

    @FXML
    private Button buttonCancel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void onButtonCancelAction(ActionEvent event)
    {
        LCCTabHandler.doButtonCancel(app, event, buttonCancel);
    }

    @FXML
    public void onButtonNextAction(ActionEvent event)
    {
        LCCTabHandler.doButtonNext(app, event, buttonNext, mainTabPane);
    }

    @FXML
    void onDedupTabDirAction(ActionEvent event)
    {

    }

    @FXML
    public void onDedupTabDirBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Deduplicate Tab > DeDupe Backup Dir Browse...'");

        LCCBrowseHandler.doGenericDirBrowse(app, event, dedupTabDirTextField,
                "Please choose the name or full path of the Log Deduplication Directory");
    }

    @FXML
    void onDedupTabDurationAction(ActionEvent event)
    {

    }

    @FXML
    void onDedupTabDurationValidateAction(ActionEvent event)
    {

    }

    @FXML
    public void onFileLoadClearHistMenuItemAction(ActionEvent event)
    {
        LCCBrowseHandler.onFileLoadClearHist(app, event,
                generalTabConfigFileTextField, fileLoadMenu, fileLoadMenuItem,
                fileLoadClearHistMenuItem);
    }

    @FXML
    public void onGeneralTabArgFileBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'General Tab > Arg File Browse...'");

        LCCBrowseHandler.doGenericFileBrowse(app, event, generalTabArgFileTextField,
                "Please choose the name or full path of the optional argument File",
                LCCConstants.LCC_DEFAULT_ARG_EXT_DESC,
                LCCConstants.LCC_DEFAULT_ARG_EXT);
    }

    @FXML
    public void onGeneralTabArgFileTextFieldAction(ActionEvent event)
    {

    }

    @FXML
    void onGeneralTabConfigFileAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'General Tab > Log Config Text Field'");
    }

    @FXML
    public void onGeneralTabConfigFileBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'General Tab > Config Browse...'");

        LCCBrowseHandler.doConfigBrowse(app, event, generalTabConfigFileTextField,
                fileLoadMenu, fileLoadMenuItem, fileLoadClearHistMenuItem);
    }

    @FXML
    public void onGeneralTabLockFileBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'General Tab > Lock File Browse...'");

        LCCBrowseHandler.doGenericFileBrowse(app, event, generalTabLockFileTextField,
                "Please choose the name or full path of the Lock File",
                LCCConstants.LCC_DEFAULT_LOCK_EXT_DESC,
                LCCConstants.LCC_DEFAULT_ALLFILES_EXT);
    }

    @FXML
    void onGeneralTabLockFileTextFieldAction(ActionEvent event)
    {

    }

    @FXML
    void onGeneralTabServiceCheckAction(ActionEvent event)
    {

    }

    @FXML
    void onGeneralTabSetNameAction(ActionEvent event)
    {

    }

    @FXML
    public void onGeneralTabSetNameValidateAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'General Tab > Set Name Validate...'");

        LCCValidateHandler.doValidateSetName(app, event,
                            generalTabSetNameTextField,
                            mainValidationTextFlow,
                            null);
    }

    @FXML
    void onLogFileCutoffDate(ActionEvent event)
    {

    }

    @FXML
    void onLogFileCutoffDurationAction(ActionEvent event)
    {

    }

    @FXML
    void onLogFileCutoffDurationValidateAction(ActionEvent event)
    {

    }

    @FXML
    void onLogFileReadOnlyAction(ActionEvent event)
    {

    }

    @FXML
    void onLogFileTargetFileAction(ActionEvent event)
    {

    }

    @FXML
    public void onLogFileTargetFileBrowseButtonAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Log File Tab > Target Log File Browse...'");

        LCCBrowseHandler.doGenericFileBrowse(app, event, logFileTargetFileTextField,
                "Please choose the name or full path of the Log File target",
                LCCConstants.LCC_DEFAULT_LOG_EXT_DESC,
                LCCConstants.LCC_DEFAULT_LOG_EXT);
    }

    @FXML
    void onLogFileValidateAction(ActionEvent event)
    {

    }

    @FXML
    void onLogStoreElastiSearchCheckButtonAction(ActionEvent event)
    {

    }

    @FXML
    void onLogStoreElastisSearchAction(ActionEvent event)
    {

    }

    @FXML
    void onLogStoreOutputFileAction(ActionEvent event)
    {

    }

    @FXML
    public void onLogStoreOutputFileBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Log Store Tab > Output File Browse...'");

        LCCBrowseHandler.doGenericFileBrowse(app, event, logStoreOutputFileTextField,
                "Please choose the name or full path of the Log Store Output File",
                LCCConstants.LCC_DEFAULT_LOG_EXT_DESC,
                LCCConstants.LCC_DEFAULT_LOG_EXT);
    }

    @FXML
    public void onMainTopFileLoad(ActionEvent event)
    {
        LCCBrowseHandler.doConfigBrowse(app, event, generalTabConfigFileTextField,
                fileLoadMenu, fileLoadMenuItem, fileLoadClearHistMenuItem);
    }

    @FXML
    public void onMainTopFileQuit(ActionEvent event)
    {
        LOGGER.debug("Action for 'File > Quit'");

        LCCTabHandler.doButtonCancel(app, event, buttonCancel);
    }

    @FXML
    void onRotateTabLogBackupDirAction(ActionEvent event)
    {

    }

    @FXML
    public void onRotateTabLogBackupDirBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Tailer Rotate Tab > Backup Dir Browse...'");

        LCCBrowseHandler.doGenericDirBrowse(app, event, rotateTabLogBackupDirTextField,
                "Please choose the name or full path of the Log Backup Directory");
    }

    @FXML
    void onRotateTabLogFileCompAction(ActionEvent event)
    {

    }

    @FXML
    void onRotateTabLogFileCompValidateAction(ActionEvent event)
    {

    }

    @FXML
    void onRotateTabLogFileRegexAction(ActionEvent event)
    {

    }

    @FXML
    void onRotateTabLogFileRegexValidateAction(ActionEvent event)
    {

    }

    @FXML
    void onTailerGeneralErrorFileAction(ActionEvent event)
    {

    }

    @FXML
    public void onTailerGeneralErrorFileBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Tailer General Tab > Error File Browse...'");

        LCCBrowseHandler.doGenericFileBrowse(app, event, tailerGeneralErrorFileTextField,
                "Please choose the name or full path of the Log Check Error File",
                LCCConstants.LCC_DEFAULT_ERROR_EXT_DESC,
                LCCConstants.LCC_DEFAULT_ERROR_EXT);
    }

    @FXML
    void onTailerGeneralStateFileAction(ActionEvent event)
    {

    }

    @FXML
    public void onTailerGeneralStateFileBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Tailer General Tab > State File Browse...'");

        LCCBrowseHandler.doGenericFileBrowse(app, event, tailerGeneralStateFileTextField,
                "Please choose the name or full path of the Log Check Save State File",
                LCCConstants.LCC_DEFAULT_STATE_EXT_DESC,
                LCCConstants.LCC_DEFAULT_STATE_EXT);
    }
}