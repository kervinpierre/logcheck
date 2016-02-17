package com.sludev.logs.logcheckConfig.controller;

import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import com.sludev.logs.logcheckConfig.util.LCCConstants;
import com.sludev.logs.logcheckConfig.util.LCCFileChooserHelper;
import com.sludev.logs.logcheckConfig.util.LCCPreferenceHelper;
import javafx.application.Platform;
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
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
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
    private TextField logStoreOutputFIleTextField;

    @FXML
    private Button LogStoreOutputFIleBrowseButton;

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
    public void onButtonCancelAction(ActionEvent event)
    {
        LOGGER.debug("Action for Button 'Cancel' pressed.");

        Stage stage = (Stage) buttonCancel.getScene().getWindow();
        stage.close();
    }


    @FXML
    public void onButtonNextAction(ActionEvent event)
    {
        LOGGER.debug("Action for Button 'Next' pressed.");

        SingleSelectionModel<Tab> currModel = mainTabPane.getSelectionModel();
        Tab lastTab = mainTabPane.getTabs().get(mainTabPane.getTabs().size() - 1);

        if( currModel.getSelectedItem() != lastTab )
        {
            mainTabPane.getSelectionModel().selectNext();
        }
        else
        {
            // Last tab
            // FIXME : Call the save method
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sure you'd like to save?",
                    ButtonType.APPLY);

            Optional<ButtonType> result = alert.showAndWait();
            if( result.isPresent() && result.get() == ButtonType.APPLY )
            {
                LOGGER.debug("Save config");
            }
        }
    }

    @FXML
    void onDedupTabDirAction(ActionEvent event)
    {

    }

    @FXML
    void onDedupTabDirBrowseAction(ActionEvent event)
    {

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
        LCCAppState appState = app.getAppState();

        LCCPreferenceHelper.clearLoadFileHistory(appState.getPreferences());
        refreshLoadHistoryMenu();
    }

    @FXML
    void onGeneralTabArgFileBrowseAction(ActionEvent event)
    {

    }

    @FXML
    void onGeneralTabArgFileTextFieldAction(ActionEvent event)
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
        LOGGER.debug("Action for 'General Tab > Browse...'");

        onMainTopFileLoad(event);
    }

    @FXML
    void onGeneralTabLockFileBrowseAction(ActionEvent event)
    {

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
    void onGeneralTabSetNameValidateAction(ActionEvent event)
    {

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
    void onLogFileTargetFileBrowseButtonAction(ActionEvent event)
    {

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
    void onLogStoreOutputFIleAction(ActionEvent event)
    {

    }

    @FXML
    void onLogStoreOutputFIleBrowseAction(ActionEvent event)
    {

    }

    @FXML
    public void onMainTopFileLoad(ActionEvent event)
    {
        LOGGER.debug("Action for 'File > Load'");

        Stage stage = (Stage) buttonCancel.getScene().getWindow();
        List<Pair<String, String>> exts = new ArrayList<>();

        exts.add(Pair.of(LCCConstants.LCC_DEFAULT_CONFIG_EXT_DESC,
                LCCConstants.LCC_DEFAULT_CONFIG_EXT));

        Path initDir = null;
        String tempVal = generalTabConfigFileTextField.getText();
        if( StringUtils.isNoneBlank(tempVal) )
        {
            initDir = Paths.get(tempVal);
            if( Files.notExists(initDir) )
            {
                initDir = null;
            }
            else if( Files.isDirectory(initDir) == false )
            {
                initDir = initDir.getParent();
            }
        }

        Path file = LCCFileChooserHelper.showFileChooser(stage, exts, "Choose the config file for loading", initDir);

        if( file != null )
        {
            LCCAppState appState = app.getAppState();
            generalTabConfigFileTextField.setText(file.toString());

            LCCPreferenceHelper.addAndRotateLoadFileHistory(appState.getPreferences(), file.toString());

            // Platform.runLater(this::refreshLoadHistoryMenu);
            refreshLoadHistoryMenu();
        }
    }

    @FXML
    public void onMainTopFileQuit(ActionEvent event)
    {
        LOGGER.debug("Action for 'File > Quit'");

        onButtonCancelAction(event);
    }

    @FXML
    void onRotateTabLogBackupDirAction(ActionEvent event)
    {

    }

    @FXML
    void onRotateTabLogBackupDirBrowseAction(ActionEvent event)
    {

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
    void onRotateTabStateFileAction(ActionEvent event)
    {

    }

    @FXML
    void onRotateTabStateFileBrowseAction(ActionEvent event)
    {

    }

    @FXML
    void onTailerGeneralErrorFileAction(ActionEvent event)
    {

    }

    @FXML
    void onTailerGeneralErrorFileBrowseAction(ActionEvent event)
    {

    }

    @FXML
    void onTailerGeneralStateFileAction(ActionEvent event)
    {

    }

    @FXML
    void onTailerGeneralStateFileBrowseAction(ActionEvent event)
    {

    }

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

    public void refreshLoadHistoryMenu()
    {
        fileLoadMenu.setDisable(true);

        final LCCAppState appState = app.getAppState();
        ObservableList<MenuItem> currItems = fileLoadMenu.getItems();

        Iterator<MenuItem> tempIt = currItems.iterator();
        while( tempIt.hasNext() )
        {
            MenuItem mi = tempIt.next();
            if( mi != fileLoadMenuItem && mi != fileLoadClearHistMenuItem )
            {
                tempIt.remove();
            }
        }

        currItems.add(new SeparatorMenuItem());

        LCCPreferenceHelper.rebuildMenu(this,
                appState.getPreferences(),
                currItems,
                generalTabConfigFileTextField);

        fileLoadMenu.setDisable(false);
    }

}