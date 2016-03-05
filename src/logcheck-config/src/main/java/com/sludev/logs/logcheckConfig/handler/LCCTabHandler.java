package com.sludev.logs.logcheckConfig.handler;

import com.sludev.logs.logcheckConfig.controller.LogCheckConfigMainController;
import com.sludev.logs.logcheckConfig.enums.LCCDialogAction;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Created by kervin on 2016-02-18.
 */
public final class LCCTabHandler
{
    private static final Logger LOGGER = LogManager.getLogger(LCCTabHandler.class);

    public static void doButtonCancel(final LogCheckConfigMain app,
                                    final ActionEvent event,
                                    final Button buttonCancel)
    {
        LOGGER.debug("Action for Button 'Cancel' pressed.");

        Stage stage = (Stage) buttonCancel.getScene().getWindow();
        stage.close();
    }

    public static LCCDialogAction doButtonNext(final LogCheckConfigMain app,
                                               final ActionEvent event,
                                               final Button buttonNext,
                                               final TabPane mainTabPane)
    {
        LCCDialogAction res = LCCDialogAction.NONE;

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
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you'd like to save to this configuration file?",
                    ButtonType.YES, ButtonType.NO);

            Optional<ButtonType> result = alert.showAndWait();
            if( result.isPresent() && result.get() == ButtonType.YES )
            {
                LOGGER.debug("Saving config...");

                return LCCDialogAction.APPLY;
            }
        }

        return res;
    }
}
