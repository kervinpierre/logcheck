package com.sludev.logs.logcheckConfig.controller;

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.enums.LCLogEntryStoreType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import com.sludev.logs.logcheckConfig.enums.LCCDialogAction;
import com.sludev.logs.logcheckConfig.handler.LCCBrowseHandler;
import com.sludev.logs.logcheckConfig.handler.LCCConfigFileHandler;
import com.sludev.logs.logcheckConfig.handler.LCCTabHandler;
import com.sludev.logs.logcheckConfig.handler.LCCValidateHandler;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import com.sludev.logs.logcheckConfig.util.LCCConstants;
import com.sludev.logs.logcheckConfig.util.LogCheckConfigException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

        tailerGeneralStateProcessedLogsFileTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            tailerGeneralStateProcessedLogsFileTextField.setTooltip(new Tooltip(newValue));
        });

        tailerGeneralPreferredDirTextField.textProperty().addListener((observable, oldValue, newValue) ->
        {
            tailerGeneralPreferredDirTextField.setTooltip(new Tooltip(newValue));
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
        String currConfigFile = null;
        String currArgFile = null;

        if( appState == null )
        {
            LOGGER.error("internalInit() : app.getAppState() returned null.");
            return;
        }

        if( StringUtils.isNoneBlank(appState.getConfigFile()) )
        {
            currConfigFile = appState.getConfigFile();
        }
        else if( appState.getPreferences() != null )
        {
            currConfigFile
                    = appState.getPreferences().get(LCCConstants.LCC_CONFIG_FILE_HIST01, "");
        }

        if( StringUtils.isNoneBlank(appState.getArgFile()) )
        {
            currArgFile = appState.getArgFile();
        }

        if( StringUtils.isNoneBlank(currConfigFile) )
        {
            generalTabConfigFileTextField.setText(currConfigFile);

            Path tempConfFile = Paths.get(currConfigFile);
            if( Files.exists(tempConfFile) )
            {
                LCCConfigFileHandler.doLoadConfigFile(app, null,
                        generalTabConfigFileTextField,
                        mainValidationTextFlow);

                refreshStateToControls(false, false);
            }

            LCCBrowseHandler.refreshLoadHistoryMenu(app, fileLoadMenu,
                    fileLoadMenuItem, fileLoadClearHistMenuItem, generalTabConfigFileTextField);
        }


        if( StringUtils.isNoneBlank(currArgFile) )
        {
            generalTabArgFileTextField.setText(currArgFile);
        }
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
    private Button generalTabConfigFileLoadButton;

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
    private Spinner<Long> generalTabStopAfterSpinner;

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
    private TextField tailerGeneralPreferredDirTextField;

    @FXML
    private Button logFileCutoffDurationValidateButton;

    @FXML
    private Button tailerGeneralPreferredDirBrowseButton;

    @FXML
    private DatePicker logFileCutoffDate;

    @FXML
    private CheckBox logFileValidateCheckbox;

    @FXML
    private Tab tabTailerGeneral;

    @FXML
    private Spinner<?> tailerGeneralPollIntervalSpinner;

    @FXML
    private Spinner<?> dedupTabIgnoreUntilCountSpinner;

    @FXML
    private Spinner<?> dedupTabSkipUntilCountSpinner;

    @FXML
    private ChoiceBox<?> tailerGeneralPollIntervalUnitsChoiceBox;

    @FXML
    private ChoiceBox<?> dedupTabDefaultActionChoicebox;

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
    private CheckBox rotateTabReadBackupLogsReverseCheckbox;

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
    private TextField tailerGeneralStateProcessedLogsFileTextField;

    @FXML
    private ChoiceBox<?> debugVerbosityChoiceBox;

    @FXML
    private TextFlow mainValidationTextFlow;

    @FXML
    private Button LogStoreOutputFileBrowseButton;

    @FXML
    private Button tailerGeneralStateProcessedLogsFileBrowseButton;

    @FXML
    private TextField logStoreElasticSearchTextField;

    @FXML
    private Button logStoreElasticSearchCheckButton;

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
    private Spinner<?> dedupTabMaxFilesSpinner;

    @FXML
    private Spinner<?> dedupTabMaxLogsBeforeWriteSpinner;

    @FXML
    private Spinner<?> dedupTabMaxEntriesReadSpinner;

    @FXML
    public void onButtonCancelAction(ActionEvent event)
    {
        LCCTabHandler.doButtonCancel(app, event, buttonCancel);
    }

    @FXML
    public void onButtonNextAction(ActionEvent event)
    {
        LCCDialogAction res = LCCTabHandler.doButtonNext(app, event, buttonNext, mainTabPane);
        if( res != LCCDialogAction.APPLY )
        {
            return;
        }

        try
        {
            LogCheckConfig currConf = refreshControlsToState(false, false);
            LCCConfigFileHandler.saveConfig(currConf,
                    generalTabConfigFileTextField.getText());
        }
        catch( LogCheckConfigException ex )
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    ex.getMessage(),
                    ButtonType.OK);

            Optional<ButtonType> result = alert.showAndWait();

            LOGGER.debug("Error saving configuration", ex);
        }

        try
        {
            LCCConfigFileHandler.saveArgFile(generalTabArgFileTextField.getText(),
                        generalTabConfigFileTextField.getText());
        }
        catch( LogCheckConfigException ex )
        {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    ex.getMessage(),
                    ButtonType.OK);

            Optional<ButtonType> result = alert.showAndWait();

            LOGGER.debug("Error saving argument file", ex);
        }

        Stage stage = (Stage) buttonNext.getScene().getWindow();
        stage.close();
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
    public void onDedupTabDurationValidateAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Deduplication Tab > Deduplication Duration Validate...'");

        LCCValidateHandler.doValidateDedupeDuration(app, event,
                dedupTabDurationTextField,
                mainValidationTextFlow,
                null);
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
    public void onGeneralTabConfigFileLoadAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'General Tab > Config File Load...'");

        LCCConfigFileHandler.doLoadConfigFile(app, event,
                generalTabConfigFileTextField,
                mainValidationTextFlow);

        refreshStateToControls(false, false);
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
                LCCConstants.LCC_DEFAULT_LOCK_EXT);
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
    public void onLogFileCutoffDurationValidateAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Log File Tab > Cut-off Duration Validate...'");

        LCCValidateHandler.doValidateCutOffDuration(app, event,
                logFileCutoffDurationTextField,
                mainValidationTextFlow,
                null);
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
    public void onLogStoreElasticSearchCheckButtonAction(ActionEvent event)
    {
        LCCValidateHandler.doValidateElasticSearchServer(app, event,
                logStoreElasticSearchTextField,
                mainValidationTextFlow);
    }

    @FXML
    public void onLogStoreElasticSearchAction(ActionEvent event)
    {
        LCCValidateHandler.doValidateElasticSearchServer(app, event,
                logStoreElasticSearchTextField,
                mainValidationTextFlow);
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

        LCCConfigFileHandler.doLoadConfigFile(app, event, generalTabLockFileTextField,
                mainValidationTextFlow);
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
    public void onRotateTabLogFileCompValidateAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Log Rotate Tab > File Regex Components Validate...'");

        LCCValidateHandler.doValidateLogRegexComponents(app, event,
                rotateTabLogFileCompTextField,
                mainValidationTextFlow,
                null);
    }

    @FXML
    void onRotateTabLogFileRegexAction(ActionEvent event)
    {

    }

    @FXML
    public void onRotateTabLogFileRegexValidateAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Log Rotate Tab > Log File Backup Regex Validate...'");

        LCCValidateHandler.doValidateLogFileBackupRegex(app, event,
                rotateTabLogFileRegexTextField,
                mainValidationTextFlow,
                null);
    }

    @FXML
    void onTailerGeneralErrorFileAction(ActionEvent event)
    {

    }

    @FXML
    void onTailerGeneralPrefferedDirBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Tailer General > Preffered Directory Browse...'");

        LCCBrowseHandler.doGenericDirBrowse(app, event, tailerGeneralPreferredDirTextField,
                "Please choose the name or full path of the Preferred Directory");
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
    public void onTailerGeneralStateProcessedLogsFileTextFieldAction(ActionEvent event)
    {
        ;
    }

    @FXML
    public void onTailerGeneralStateProcessedLogsFileBrowseAction(ActionEvent event)
    {
        LOGGER.debug("Action for 'Tailer General Tab > State Processed Logs File Browse...'");

        LCCBrowseHandler.doGenericFileBrowse(app, event, tailerGeneralStateProcessedLogsFileTextField,
                "Please choose the name or full path of the State Processed Logs File",
                LCCConstants.LCC_DEFAULT_STATE_EXT_DESC,
                LCCConstants.LCC_DEFAULT_STATE_EXT);
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

    public void refreshStateToControls(final boolean resetConfigFile,
                                       final boolean resetArgFile)
    {
        LogCheckConfig lcc = app.getAppState().getConfig();
        if( lcc == null )
        {
            return;
        }

        if( resetConfigFile
                && app.getAppState().getConfigFile() != null)
        {
            generalTabConfigFileTextField.setText(app.getAppState().getConfigFile());
        }

        if( resetArgFile
                && app.getAppState().getArgFile() != null )
        {
            generalTabArgFileTextField.setText(app.getAppState().getArgFile());
        }

        if( lcc.getLockFilePath() != null )
        {
            generalTabLockFileTextField.setText(lcc.getLockFilePath().toString());
        }

        if( lcc.isService() != null )
        {
            generalTabServiceCheck.setSelected(lcc.isService());
        }

        if( StringUtils.isNoneBlank(lcc.getSetName()) )
        {
            generalTabSetNameTextField.setText(lcc.getSetName());
        }

        if( lcc.getStopAfter() != null )
        {
            generalTabStopAfterSpinner.getEditor().setText(lcc.getStopAfter().toString());
        }

        if( lcc.getLogPath() != null )
        {
            logFileTargetFileTextField.setText(lcc.getLogPath().toString());
        }

        if( lcc.getStoreLogPath() != null )
        {
            logStoreOutputFileTextField.setText(lcc.getStoreLogPath().toString());
        }

        if( lcc.isReadOnlyFileMode() != null )
        {
            logFileReadOnlyCheckbox.setSelected(lcc.isReadOnlyFileMode());
        }

        if( lcc.willValidateTailerStats() != null )
        {
            logFileValidateCheckbox.setSelected(lcc.willValidateTailerStats());
        }

        if( lcc.getLogCutoffDuration() != null )
        {
            logFileCutoffDurationTextField
                    .setText(lcc.getLogCutoffDuration().toString());
        }

        if( lcc.getLogCutoffDate() != null )
        {
            logFileCutoffDate.setValue(LocalDate.from(lcc.getLogCutoffDate()));
        }

        if( lcc.getPollIntervalSeconds() != null )
        {
            tailerGeneralPollIntervalSpinner
                    .getEditor().setText(lcc.getPollIntervalSeconds().toString());
        }

        if( lcc.getStateFilePath() != null )
        {
            tailerGeneralStateFileTextField.setText(lcc.getStateFilePath().toString());
        }

        if( lcc.getStateProcessedLogsFilePath() != null )
        {
            tailerGeneralStateProcessedLogsFileTextField
                    .setText(lcc.getStateProcessedLogsFilePath().toString());
        }

        if( lcc.willSaveState() != null )
        {
            tailerGeneralSaveStateCheckbox.setSelected(lcc.willSaveState());
        }

        if( lcc.isTailFromEnd() != null )
        {
            tailerGeneralFileFromStartCheckbox.setSelected(lcc.isTailFromEnd()==false);
        }

        if( lcc.willContinueState() != null )
        {
            tailerGeneralContinueCheckbox.setSelected(lcc.willContinueState());
        }

        if( lcc.willReadReOpenLogFile() != null )
        {
            tailerGeneralReOpenCheckbox.setSelected(lcc.willReadReOpenLogFile());
        }

        if( lcc.willIgnoreStartPositionError() != null )
        {
            tailerGeneralStartPositionIgnoreErrCheckbox
                    .setSelected(lcc.willIgnoreStartPositionError());
        }

        if( lcc.willStopOnEOF() != null )
        {
            tailerGeneralEOFStopCheckbox
                    .setSelected(lcc.willStopOnEOF());
        }

        if( lcc.getErrorFilePath() != null )
        {
            tailerGeneralErrorFileTextField.setText(lcc.getErrorFilePath().toString());
        }

        if( lcc.getPreferredDir() != null )
        {
            tailerGeneralPreferredDirTextField.setText(lcc.getPreferredDir().toString());
        }

        if( lcc.getTailerLogBackupDir() != null )
        {
            rotateTabLogBackupDirTextField.setText(lcc.getTailerLogBackupDir().toString());
        }

        if( lcc.willTailerBackupReadLog() != null )
        {
            rotateTabReadBackupLogsCheckbox
                    .setSelected(lcc.willTailerBackupReadLog());
        }

        if( lcc.willTailerBackupReadLogReverse() != null )
        {
            rotateTabReadBackupLogsReverseCheckbox
                    .setSelected(lcc.willTailerBackupReadLogReverse());
        }

        if( lcc.willTailerBackupReadPriorLog() != null )
        {
            rotateTabReadPriorLogsCheckbox
                    .setSelected(lcc.willTailerBackupReadPriorLog());
        }

        if( lcc.getTailerBackupLogNameRegex() != null )
        {
            rotateTabLogFileRegexTextField
                    .setText(lcc.getTailerBackupLogNameRegex().toString());
        }

        if( lcc.getTailerBackupLogNameComps() != null )
        {
            rotateTabLogFileCompTextField
                    .setText(StringUtils.join(lcc.getTailerBackupLogNameComps(), ','));
        }

        if( lcc.getLogDeduplicationDuration() != null )
        {
            dedupTabDurationTextField
                    .setText(lcc.getLogDeduplicationDuration().toString());
        }

        if( lcc.getDeDupeDirPath() != null )
        {
            dedupTabDirTextField
                    .setText(lcc.getDeDupeDirPath().toString());
        }

        if( lcc.getDeDupeMaxLogsPerFile() != null )
        {
            dedupTabLogsPerFileSpinner
                    .getEditor().setText(lcc.getDeDupeMaxLogsPerFile().toString());
        }

        if( lcc.getDeDupeIgnoreCount() != null )
        {
            dedupTabIgnoreUntilCountSpinner
                    .getEditor().setText(lcc.getDeDupeIgnoreCount().toString());
        }

        if( lcc.getDeDupeSkipCount() != null )
        {
            dedupTabSkipUntilCountSpinner
                    .getEditor().setText(lcc.getDeDupeSkipCount().toString());
        }

        if( lcc.getDeDupeMaxLogFiles() != null )
        {
            dedupTabMaxFilesSpinner
                    .getEditor().setText(lcc.getDeDupeMaxLogFiles().toString());
        }

        if( lcc.getDeDupeMaxLogsBeforeWrite() != null )
        {
            dedupTabMaxLogsBeforeWriteSpinner
                    .getEditor().setText(lcc.getDeDupeMaxLogsBeforeWrite().toString());
        }

        if( lcc.getReadMaxDeDupeEntries() != null )
        {
            dedupTabMaxEntriesReadSpinner
                    .getEditor().setText(lcc.getReadMaxDeDupeEntries().toString());
        }

        if( lcc.getDeDupeDefaultAction() != null )
        {
            int currInt = 0;
            switch( lcc.getDeDupeDefaultAction() )
            {
                case NONE:
                    currInt = 0;
                    break;

                case IGNORE:
                    currInt = 1;
                    break;

                case SKIP:
                    currInt = 3;
                    break;

                case BREAK:
                    currInt = 2;
                    break;
            }

            dedupTabDefaultActionChoicebox.getSelectionModel().select(currInt);
        }

        if( lcc.getVerbosity() != null )
        {
            int currInt = 0;
            switch( lcc.getVerbosity() )
            {
                case NONE:
                    currInt = 0;
                    break;

                case ALL:
                    currInt = 1;
                    break;

                case MINIMUM:
                    currInt = 2;
                    break;

                case MAXIMUM:
                    currInt = 3;
                    break;

                case DEBUG:
                    currInt = 4;
                    break;

                case INFO:
                    currInt = 5;
                    break;

                case WARN:
                    currInt = 6;
                    break;

                case ERROR:
                    currInt = 7;
                    break;
            }

            debugVerbosityChoiceBox.getSelectionModel().select(currInt);
        }

        if( lcc.getLogEntryStores() != null && lcc.getLogEntryStores().size() > 0 )
        {
            LCLogEntryStoreType currType = lcc.getLogEntryStores().get(0);
            int currInt = 0;
            switch( currType )
            {
                case SIMPLEFILE:
                    currInt = 1;
                    break;

                case CONSOLE:
                    currInt = 2;
                    break;

                case ELASTICSEARCH:
                    currInt = 3;
                    break;
            }

            logStoreTabLogStore01Choicebox.getSelectionModel().select(currInt);

            if( lcc.getLogEntryStores().size() > 1 )
            {
                currType = lcc.getLogEntryStores().get(1);
                currInt = 0;
                switch( currType )
                {
                    case SIMPLEFILE:
                        currInt = 1;
                        break;

                    case CONSOLE:
                        currInt = 2;
                        break;

                    case ELASTICSEARCH:
                        currInt = 3;
                        break;
                }

                logStoreTabLogStore02Choicebox.getSelectionModel().select(currInt);

                if( lcc.getLogEntryStores().size() > 2 )
                {
                    currType = lcc.getLogEntryStores().get(2);
                    currInt = 0;
                    switch( currType )
                    {
                        case SIMPLEFILE:
                            currInt = 1;
                            break;

                        case CONSOLE:
                            currInt = 2;
                            break;

                        case ELASTICSEARCH:
                            currInt = 3;
                            break;
                    }

                    logStoreTabLogStore03Choicebox.getSelectionModel().select(currInt);
                }
            }
        }

        if( lcc.getStoreLogPath() != null )
        {
            logStoreOutputFileTextField
                    .setText(lcc.getStoreLogPath().toString());
        }

        if( lcc.getElasticsearchURL() != null )
        {
            logStoreElasticSearchTextField
                    .setText(lcc.getElasticsearchURL().toString());
        }

        if( lcc.getLogEntryBuilders() != null && lcc.getLogEntryBuilders().size() > 0 )
        {
            LCLogEntryBuilderType currType = lcc.getLogEntryBuilders().get(0);
            int currInt = 0;
            switch( currType )
            {
                case SINGLELINE:
                    currInt = 1;
                    break;

                case MULTILINE_DELIMITED:
                    currInt = 2;
                    break;

                case NCSA_COMMON_LOG:
                    currInt = 3;
                    break;
            }

            logBuilderTabChoicebox.getSelectionModel().select(currInt);
        }

        if( lcc.getDebugFlags() != null && lcc.getDebugFlags().size() > 0 )
        {
            for( LCDebugFlag flag : lcc.getDebugFlags() )
            {
                ObservableList<?> currList = debugTabFlagsListView.getItems();
                int i=0;
                for( Object o : currList )
                {
                    if( o.toString().equals(flag.toString()) )
                    {
                        debugTabFlagsListView.getSelectionModel().select(i);
                    }
                    i++;
                }
            }
        }
    }


    public LogCheckConfig refreshControlsToState(final boolean resetConfigFile,
                                       final boolean resetArgFile)
    {
        LogCheckConfig res = null;

        Boolean currService = null;
        Boolean currDryRun = null;
        Boolean currShowVersion = null;
        Boolean currTailFromEnd = null;
        Boolean currPrintLogs = null;
        Boolean currSaveState = null;
        Boolean currContinue = null;
        Boolean currReadReOpenLogFile = null;
        Boolean currStoreReOpenLogFile = null;
        Boolean currStartPositionIgnoreError = null;
        Boolean currValidateTailerStats = null;
        Boolean currTailerBackupReadLogs = null;
        Boolean currTailerBackupReadPriorLogs = null;
        Boolean currStopOnEOF = null;
        Boolean currReadOnlyFileMode = null;
        Boolean currTailerBackupReadLogReverse = null;
        String currPollIntervalSeconds = null;
        String currEmailOnError = null;
        String currSmtpServer = null;
        String currSmtpPort = null;
        String currSmtpUser = null;
        String currSmtpPass = null;
        String currSmtpProto = null;
        String currLogDeduplicationDuration = null;
        String currLockFile = null;
        String currLogPath = null;
        String currLogCutoffDuration = null;
        String currLogCutoffDate = null;
        String currElasticsearchUrl = null;
        String currStatusFile = null;
        String currStateFile = null;
        String currErrorFile = null;
        String currIdBlockHashtype = null;
        String currIdBlockSize = null;
        String currSetName = null;
        String currDeDupeDirPath = null;
        String currDeDupeMaxLogsPerFile = null;
        String currDeDupeMaxLogFiles = null;
        String currDeDupeMaxLogsBeforeWrite = null;
        String currStopAfter = null;
        String currReadLogFileCount = null;
        String currReadMaxDeDupeEntries = null;
        String currStoreLogFile = null;
        String currTailerBackupLogNameRegex = null;
        String currTailerBackupLogCompression = null;
        String currTailerBackupLogDir = null;
        String currDebugVerbosity = null;
        String currProcessedLogsFile = null;
        String currPreferredDir = null;
        String currDeDupeIgnoreCountStr = null;
        String currDeDupeSkipCountStr = null;
        String currDeDupeDefaultAction = null;
        String[] currLEBuilderType = null;
        String[] currLEStoreType = null;
        String[] currTailerBackupLogNameComps = null;
        String[] currDebugFlags = null;

        Path configFile = null;
        String configFileStr = generalTabConfigFileTextField.getText();
        if( StringUtils.isNoneBlank(configFileStr) )
        {
            configFile = Paths.get(configFileStr);
        }
        else
        {
            LOGGER.warn("Configuration File cannot be blank.");
        }

        currLockFile = generalTabLockFileTextField.getText();
        currService = generalTabServiceCheck.isSelected();
        currSetName = generalTabSetNameTextField.getText();
        currStopAfter = generalTabStopAfterSpinner.getEditor().getText();
        currStoreLogFile = logStoreOutputFileTextField.getText();
        currReadOnlyFileMode = logFileReadOnlyCheckbox.isSelected();
        currValidateTailerStats = logFileValidateCheckbox.isSelected();
        currLogCutoffDuration = logFileCutoffDurationTextField.getText();
        currLogCutoffDate = logFileCutoffDate.getEditor().getText();
        currPollIntervalSeconds = tailerGeneralPollIntervalSpinner.getEditor().getText();
        currStateFile = tailerGeneralStateFileTextField.getText();
        currSaveState = tailerGeneralSaveStateCheckbox.isSelected();
        currTailFromEnd = !tailerGeneralFileFromStartCheckbox.isSelected();
        currContinue = tailerGeneralContinueCheckbox.isSelected();
        currReadReOpenLogFile = tailerGeneralReOpenCheckbox.isSelected();
        currStartPositionIgnoreError = tailerGeneralStartPositionIgnoreErrCheckbox.isSelected();
        currStopOnEOF = tailerGeneralEOFStopCheckbox.isSelected();
        currErrorFile = tailerGeneralErrorFileTextField.getText();
        currTailerBackupLogDir = rotateTabLogBackupDirTextField.getText();
        currTailerBackupReadLogs = rotateTabReadBackupLogsCheckbox.isSelected();
        currTailerBackupReadPriorLogs = rotateTabReadPriorLogsCheckbox.isSelected();
        currTailerBackupReadLogReverse = rotateTabReadBackupLogsReverseCheckbox.isSelected();
        currTailerBackupLogNameRegex = rotateTabLogFileRegexTextField.getText();
        currTailerBackupLogNameComps = StringUtils.split(rotateTabLogFileCompTextField.getText(), ", ");
        currDeDupeDirPath = dedupTabDirTextField.getText();
        currDeDupeMaxLogsPerFile = dedupTabLogsPerFileSpinner.getEditor().getText();
        currDeDupeMaxLogFiles = dedupTabMaxFilesSpinner.getEditor().getText();
        currDeDupeMaxLogsBeforeWrite = dedupTabMaxLogsBeforeWriteSpinner.getEditor().getText();
        currReadMaxDeDupeEntries = dedupTabMaxEntriesReadSpinner.getEditor().getText();
        currLogDeduplicationDuration = dedupTabDurationTextField.getText();
        currLogPath = logFileTargetFileTextField.getText();
        currElasticsearchUrl = logStoreElasticSearchTextField.getText();
        currProcessedLogsFile = tailerGeneralStateProcessedLogsFileTextField.getText();
        currPreferredDir = tailerGeneralPreferredDirTextField.getText();
        currDeDupeIgnoreCountStr = dedupTabIgnoreUntilCountSpinner.getEditor().getText();
        currDeDupeSkipCountStr = dedupTabSkipUntilCountSpinner.getEditor().getText();

        currDebugVerbosity = debugVerbosityChoiceBox.getSelectionModel().getSelectedItem().toString();

        List<String> currLEStoreTypeList = new ArrayList<>();
        List<String> currLEBuilderList = new ArrayList<>();

        if( logStoreTabLogStore01Choicebox.getSelectionModel().getSelectedItem() != null )
        {
            currDeDupeDefaultAction =
                    logStoreTabLogStore01Choicebox
                            .getSelectionModel().getSelectedItem().toString();
        }

        if( dedupTabDefaultActionChoicebox.getSelectionModel().getSelectedItem() != null )
        {
            currLEStoreTypeList.add(
                    dedupTabDefaultActionChoicebox
                            .getSelectionModel().getSelectedItem().toString());
        }

        if( logStoreTabLogStore02Choicebox.getSelectionModel().getSelectedItem() != null )
        {
            currLEStoreTypeList.add(
                    logStoreTabLogStore02Choicebox
                            .getSelectionModel().getSelectedItem().toString());
        }

        if( logStoreTabLogStore03Choicebox.getSelectionModel().getSelectedItem() != null )
        {
            currLEStoreTypeList.add(
                    logStoreTabLogStore03Choicebox
                            .getSelectionModel().getSelectedItem().toString());
        }

        currLEStoreType = currLEStoreTypeList.toArray(new String[currLEStoreTypeList.size()]);

        currLEBuilderList.add(
            logBuilderTabChoicebox.getSelectionModel().getSelectedItem().toString());

        if( logBuilderTabChoicebox.getSelectionModel().getSelectedItem() != null  )
        {
            String selectedBuilder = logBuilderTabChoicebox.getSelectionModel()
                                                .getSelectedItem().toString();

            if( StringUtils.isNoneBlank(selectedBuilder))
            {
                currLEBuilderType = new String[1];
                currLEBuilderType[0] = selectedBuilder;
            }
        }

        ObservableList<?> currList = debugTabFlagsListView.getSelectionModel().getSelectedItems();
        if( currList != null && currList.size() > 0 )
        {
            currDebugFlags = new String[currList.size()];

            for( int i=0; i<currList.size(); i++)
            {
                currDebugFlags[i] = currList.get(i).toString();
            }
        }

        try
        {
            res = LogCheckConfig.from(null,
                    currService, // service,
                    currEmailOnError, // emailOnError,
                    currSmtpServer,
                    currSmtpPort,
                    currSmtpPass,
                    currSmtpUser,
                    currSmtpProto,
                    currSetName,
                    currDryRun,
                    currShowVersion, // showVersion,
                    currPrintLogs, // printLog,
                    currTailFromEnd, // tailFromEnd,
                    currReadReOpenLogFile, // reOpenLogFile
                    currStoreReOpenLogFile, // --store-reopen-log-file
                    currSaveState,  // saveState
                    null, // collectState
                    currContinue,
                    currStartPositionIgnoreError,
                    currValidateTailerStats,
                    currTailerBackupReadLogs,
                    currTailerBackupReadLogReverse,
                    currTailerBackupReadPriorLogs,
                    currStopOnEOF,
                    currReadOnlyFileMode,
                    null, // createMissingDirs
                    currLockFile,
                    currLogPath,
                    currStoreLogFile,
                    currStatusFile,
                    currStateFile,
                    currProcessedLogsFile,
                    currErrorFile,
                    null, // configFilePath,
                    null, // holdingDir
                    currDeDupeDirPath,
                    currTailerBackupLogDir,
                    currPreferredDir,
                    currElasticsearchUrl,
                    null, // elasticsearchIndexName,
                    null, // elasticsearchIndexPrefix,
                    null, // elasticsearchLogType,
                    null, // elasticsearchIndexNameFormat,
                    currLogCutoffDate, // logCutoffDate,
                    currLogCutoffDuration, // logCutoffDuration,
                    currLogDeduplicationDuration, // logDeduplicationDuration,
                    currPollIntervalSeconds,
                    currStopAfter,
                    currDeDupeIgnoreCountStr,
                    currDeDupeSkipCountStr,
                    currReadLogFileCount,
                    currReadMaxDeDupeEntries,
                    currIdBlockSize,
                    currDeDupeMaxLogsBeforeWrite,
                    currDeDupeMaxLogsPerFile,
                    currDeDupeMaxLogFiles,
                    null, // deDupeIgnorePercentStr
                    null, // deDupeSkipPercentStr
                    currDebugVerbosity,
                    currDeDupeDefaultAction,
                    currLEBuilderType,
                    currLEStoreType,
                    currTailerBackupLogNameComps,
                    currIdBlockHashtype,
                    currTailerBackupLogCompression,
                    currTailerBackupLogNameRegex,
                    currDebugFlags);
        }
        catch( LogCheckException ex )
        {
            LOGGER.debug("Error creating state object.", ex);
        }

        return res;
    }
}