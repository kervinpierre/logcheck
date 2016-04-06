package com.sludev.logs.logcheckConfig.handler;

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.config.parsers.LogCheckConfigParser;
import com.sludev.logs.logcheck.config.parsers.ParserUtil;
import com.sludev.logs.logcheck.config.writers.LogCheckConfigWriter;
import com.sludev.logs.logcheck.config.writers.LogCheckStateWriter;
import com.sludev.logs.logcheck.enums.LCFileFormat;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import com.sludev.logs.logcheckConfig.enums.LCCDialogAction;
import com.sludev.logs.logcheckConfig.enums.LCCErrorMsgType;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import com.sludev.logs.logcheckConfig.util.LogCheckConfigException;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Optional;

/**
 * Created by kervin on 2016-02-25.
 */
public final class LCCConfigFileHandler
{
    private static final Logger LOGGER = LogManager.getLogger(LCCConfigFileHandler.class);

    public static void doLoadConfigFile(final LogCheckConfigMain app,
                                        final ActionEvent event,
                                        final TextField textField,
                                        final TextFlow validationTextFlow)
    {
        String configFileStr = textField.getText();
        Path confFilePath = null;

        if( StringUtils.isBlank(configFileStr) )
        {
            app.getAppState().addOrReplaceControlErrors(textField,
                    LCCErrorMsgType.LOG_BACKUP_FILE_LOAD_FAILED,
                    "Configuration File Path cannot be empty\n");
        }
        else
        {
            try
            {
                confFilePath = Paths.get(configFileStr);
            }
            catch( Exception ex )
            {
                String errMsg = String.format("Configuration File Path is invalid : '%s'",
                        ex.getMessage());

                LOGGER.debug(errMsg, ex);

                app.getAppState().addOrReplaceControlErrors(textField,
                        LCCErrorMsgType.LOG_BACKUP_FILE_LOAD_FAILED,
                        errMsg + "\n");
            }
        }

        if( confFilePath != null )
        {
            try
            {
                LogCheckConfig config = LogCheckConfigParser.readConfig(
                        ParserUtil.readConfig(confFilePath,
                                LCFileFormat.LCCONFIG));

                app.getAppState().setConfig(config);

                app.getAppState().addOrReplaceControlErrors(textField,
                        LCCErrorMsgType.LOG_BACKUP_FILE_LOAD_FAILED,
                        null);
            }
            catch( LogCheckException ex )
            {
                String errMsg = String.format("Configuration File Parse failed : '%s'",
                        ex.getMessage());

                LOGGER.debug(errMsg, ex);

                app.getAppState().addOrReplaceControlErrors(textField,
                        LCCErrorMsgType.LOG_BACKUP_FILE_LOAD_FAILED,
                        errMsg + "\n");
            }
        }

        app.getAppState().updateValidationTextFlow(validationTextFlow);
    }

    public static void saveConfig(final LogCheckConfig conf,
                                  final String filePath ) throws LogCheckConfigException
    {
        Path path = Paths.get(filePath);

        saveConfig(conf, path);
    }

    public static void saveConfig(final LogCheckConfig conf,
                                  final Path file ) throws LogCheckConfigException
    {
        LOGGER.debug(String.format("saveConfig() : saving to %s", file));

        if( conf == null )
        {
            throw new LogCheckConfigException("Configuration object cannot be null");
        }

        if( file == null )
        {
            throw new LogCheckConfigException("Configuration file path cannot be null");
        }

        if( Files.exists(file) )
        {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    String.format("File already exists!  Should we overwrite? %s", file),
                    ButtonType.YES, ButtonType.NO);

            Optional<ButtonType> result = alert.showAndWait();
            if( result.isPresent() == false || result.get() != ButtonType.YES )
            {
                LOGGER.debug("Chose not to overwrite an existing config file.");

                return;
            }
        }

        try
        {
            LogCheckConfigWriter.write(conf, file);
        }
        catch( LogCheckException ex )
        {
            String errMsg = String.format("Error writing config file %s", file);

            Alert alert = new Alert(Alert.AlertType.ERROR,
                    errMsg,
                    ButtonType.OK);

            Optional<ButtonType> result = alert.showAndWait();

            LOGGER.debug(errMsg, ex);
        }
    }

    public static void saveArgFile(final String argFilePath,
                                  final String confFilePath ) throws LogCheckConfigException
    {
        if( StringUtils.isBlank(argFilePath) )
        {
            return;
        }

        Path argFile = Paths.get(argFilePath);
        if( Files.exists(argFile) )
        {
            String bkFileName = String.format("%s_%s", argFile.getFileName(), Instant.now());
            bkFileName = bkFileName.replaceAll("[^a-zA-Z0-9.-]", "_").concat(".bak");

            Path bkFile = argFile.getParent().resolve(bkFileName);
            try
            {
                Files.move(argFile, bkFile);
            }
            catch( IOException ex )
            {
                LOGGER.warn(String.format("Failed backing up argument file '%s' to '%s'",
                        argFile, bkFile), ex);
            }
        }

        try( BufferedWriter bw = Files.newBufferedWriter(argFile,
                                                StandardOpenOption.WRITE,
                                                StandardOpenOption.CREATE_NEW) )
        {
            String outStr = String.format("--config-file \"%s\"", confFilePath);

            bw.write(outStr);
        }
        catch( IOException ex )
        {
            LOGGER.warn(String.format("Failed writing argument file at '%s'", argFile), ex);
        }
    }
}
