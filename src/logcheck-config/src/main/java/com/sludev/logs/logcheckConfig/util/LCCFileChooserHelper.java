package com.sludev.logs.logcheckConfig.util;

import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kervin on 2016-02-12.
 */
public final class LCCFileChooserHelper
{
    private static final Logger LOGGER = LogManager.getLogger(LCCFileChooserHelper.class);

    public static Path showFileChooser( final Stage stage,
                                        final List<Pair<String,String>> extensions,
                                        final String title,
                                        final Path initialPath)
    {
        FileChooser fileChooser = new FileChooser();

        for( Pair<String, String> ext : extensions )
        {
            FileChooser.ExtensionFilter extFilter
                    = new FileChooser.ExtensionFilter(ext.getLeft(),
                                                        ext.getRight());

            fileChooser.getExtensionFilters().add(extFilter);
        }

        File initDir = null;
        String initFileName = LCCConstants.LCC_DEFAULT_CONFIG_FILENAME;

        if( initialPath != null )
        {
            if( Files.isDirectory(initialPath) )
            {
                initDir = initialPath.toFile();
            }
            else
            {
                if( initialPath.isAbsolute() )
                {
                    Path tempDir = initialPath.getParent();
                    initDir = tempDir.toFile();
                }

                initFileName = initialPath.getFileName().toString();
            }
        }

        if( StringUtils.isNoneBlank(title) )
        {
            fileChooser.setTitle(title);
        }


        File file = fileChooser.showOpenDialog(stage);
        Path res = null;

        if( file != null )
        {
            res = file.toPath();
        }

        return res;
    }

    public static Path showBrowse(final TextField textField,
                                  final List<Pair<String,String>> exts,
                                  final String title,
                                  final Path initialDir)
    {
        Path res = null;

        Stage stage = (Stage) textField.getScene().getWindow();

        Path initDir = initialDir;
        String tempVal = textField.getText();
        if( StringUtils.isNoneBlank(tempVal)
                            && initDir == null )
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

        res = LCCFileChooserHelper.showFileChooser(stage, exts, title, initDir);
        if( res != null )
        {
            textField.setText(res.toString());
        }

        return res;
    }
}
