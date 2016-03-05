package com.sludev.logs.logcheckConfig.handler;

import com.sludev.logs.logcheck.enums.LCResultStatus;
import com.sludev.logs.logcheck.store.impl.LogEntryElasticSearch;
import com.sludev.logs.logcheckConfig.enums.LCCErrorMsgType;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

/**
 * Created by kervin on 2016-02-19.
 */
public final class LCCValidateHandler
{
    private static final Logger LOGGER = LogManager.getLogger(LCCValidateHandler.class);

    /**
     * From http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/share/classes/java/time/Duration.java#l151
     */
    public static final Pattern DURATION_PATTERN =
            Pattern.compile("([-+]?)P(?:([-+]?[0-9]+)D)?" +
                            "(T(?:([-+]?[0-9]+)H)?(?:([-+]?[0-9]+)M)?" +
                            "(?:([-+]?[0-9]+)(?:[.,]([0-9]{0,9}))?S)?)?",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern SETNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_ -]+");

    public static final Pattern REGEX_PATTERN = Pattern.compile(".*?[*+].*?");

    public static final Pattern LOG_FILE_REGEX_COMPS_PATTERN
            = Pattern.compile("((FILENAME_PREFIX|INTEGER_INC|TIMESTAMP)(?:[;,\\s]*))+",
            Pattern.CASE_INSENSITIVE);

    public static void doValidateSetName(final LogCheckConfigMain app,
                                         final ActionEvent event,
                                         final TextField textField,
                                         final TextFlow validationTextFlow,
                                         final String oldValue)
    {
        String currValue = textField.getText();
        boolean isValid = false;

        if( StringUtils.isBlank(currValue) )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.SET_NAME_INVALID,
                    "Set Name Error : Value cannot be blank.\n");
        }
        else if( SETNAME_PATTERN.matcher(currValue).matches() == false )
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

    public static void doValidateCutOffDuration(final LogCheckConfigMain app,
                                                final ActionEvent event,
                                                final TextField textField,
                                                final TextFlow validationTextFlow,
                                                final String oldValue)
    {
        String currValue = textField.getText();
        boolean isValid = false;
        ;

        if( StringUtils.isBlank(currValue) )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.CUT_OFF_DURATION_INVALID,
                    "Cut-Off Period Duration Error : Value cannot be blank.\n");
        }
        else if( DURATION_PATTERN.matcher(currValue).matches() == false )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.CUT_OFF_DURATION_INVALID,
                    "Cut-Off Period Duration Error : Should be in the form of...\n    "
                            + "\"P2DT3H4M\"  -- parses as \"2 days, 3 hours and 4 minutes\".\n");
        }
        else
        {
            isValid = true;
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.CUT_OFF_DURATION_INVALID, null);
        }

        app.getAppState().updateValidationTextFlow(validationTextFlow);
    }


    public static void doValidateDedupeDuration(final LogCheckConfigMain app,
                                                final ActionEvent event,
                                                final TextField textField,
                                                final TextFlow validationTextFlow,
                                                final String oldValue)
    {
        String currValue = textField.getText();
        boolean isValid = false;
        ;

        if( StringUtils.isBlank(currValue) )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.DEDUPE_DURATION_INVALID,
                    "Deduplication Duration Error : Value cannot be blank.\n");
        }
        else if( DURATION_PATTERN.matcher(currValue).matches() == false )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.DEDUPE_DURATION_INVALID,
                    "Deduplication Duration Error : Should be in the form of...\n    "
                            + "\"P2DT3H4M\"  -- parses as \"2 days, 3 hours and 4 minutes\".\n");
        }
        else
        {
            isValid = true;
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.DEDUPE_DURATION_INVALID, null);
        }

        app.getAppState().updateValidationTextFlow(validationTextFlow);
    }

    public static void doValidateLogFileBackupRegex(final LogCheckConfigMain app,
                                                    final ActionEvent event,
                                                    final TextField textField,
                                                    final TextFlow validationTextFlow,
                                                    final String oldValue)
    {
        String currValue = textField.getText();
        boolean isValid = false;
        ;

        if( StringUtils.isBlank(currValue) )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_BACKUP_FILE_REGEX_INVALID,
                    "Log Backup Regex Error : Value cannot be blank.\n");
        }
        else if( REGEX_PATTERN.matcher(currValue).matches() == false )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_BACKUP_FILE_REGEX_INVALID,
                    "Log Backup Regex Error : The value does not look like a valid regular expression.\n");
        }
        else
        {
            isValid = true;
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_BACKUP_FILE_REGEX_INVALID, null);
        }

        app.getAppState().updateValidationTextFlow(validationTextFlow);
    }

    public static void doValidateLogRegexComponents(final LogCheckConfigMain app,
                                                    final ActionEvent event,
                                                    final TextField textField,
                                                    final TextFlow validationTextFlow,
                                                    final String oldValue)
    {
        String currValue = textField.getText();
        boolean isValid = false;
        ;

        if( StringUtils.isBlank(currValue) )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_BACKUP_FILE_REGEX_COMPS_INVALID,
                    "Log Backup Regex Components Error : Value cannot be blank.\n");
        }
        else if( LOG_FILE_REGEX_COMPS_PATTERN.matcher(currValue).matches() == false )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_BACKUP_FILE_REGEX_COMPS_INVALID,
                    "Log Backup Regex Components Error : Valid value includes"
                            + " a list of the following... FILENAME_PREFIX, INTEGER_INC, TIMESTAMP.\n");
        }
        else
        {
            isValid = true;
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_BACKUP_FILE_REGEX_COMPS_INVALID, null);
        }

        app.getAppState().updateValidationTextFlow(validationTextFlow);
    }

    public static void doValidateElasticSearchServer(final LogCheckConfigMain app,
                                                     final ActionEvent event,
                                                     final TextField textField,
                                                     final TextFlow validationTextFlow)
    {
        String currValue = textField.getText();
        boolean isValid = false;
        String errMsg = "";

        if( StringUtils.isBlank(currValue) )
        {
            errMsg = "ElasticSearch URL cannot be empty.";
        }
        else
        {
            try
            {
                LogEntryElasticSearch instance = LogEntryElasticSearch.from(currValue, null);
                instance.init();

                LCResultStatus result = instance.testConnection();
                if( result == LCResultStatus.SUCCESS )
                {
                    isValid = true;
                }
            }
            catch( Exception ex )
            {
                errMsg = ex.getMessage();
                LOGGER.debug("doValidateElasticSearchServer() : Error checking", ex);
            }
        }

        if( isValid )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_STORE_ELASTICSEARCH_CHECK_INVALID, null);
        }
        else
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_STORE_ELASTICSEARCH_CHECK_INVALID, errMsg);
        }

        app.getAppState().updateValidationTextFlow(validationTextFlow);
    }
}
