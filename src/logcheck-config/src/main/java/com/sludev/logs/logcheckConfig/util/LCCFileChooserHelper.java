package com.sludev.logs.logcheckConfig.util;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
}
