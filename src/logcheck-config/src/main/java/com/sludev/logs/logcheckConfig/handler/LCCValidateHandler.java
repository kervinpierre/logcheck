package com.sludev.logs.logcheckConfig.handler;

import com.sludev.logs.logcheckConfig.enums.LCCErrorMsgType;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by kervin on 2016-02-19.
 */
public final class LCCValidateHandler
{
    private static final Logger LOGGER = LogManager.getLogger(LCCValidateHandler.class);

    public static void doValidateSetName(final LogCheckConfigMain app,
                                         final ActionEvent event,
                                         final TextField textField,
                                         final TextFlow validationTextFlow,
                                         final String oldValue)
    {
        String currValue = textField.getText();
        boolean isValid = false;

        Pattern setNamePat = Pattern.compile("[a-zA-Z0-9_ -]+");

        if( StringUtils.isBlank(currValue) )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.SET_NAME_INVALID,
                    "Set Name Error : Value cannot be blank.\n");
        }
        else if( setNamePat.matcher(currValue).matches() == false )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.SET_NAME_INVALID,
                    "Set Name Error : Value can only be letters, numbers and spaces.\n");
        }
        else
        {
            isValid = true;
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.SET_NAME_INVALID, null);
        }

        app.getAppState().updateValidationTextFlow(validationTextFlow);
    }
}
