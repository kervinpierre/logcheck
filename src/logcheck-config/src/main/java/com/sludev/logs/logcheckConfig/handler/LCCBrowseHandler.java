package com.sludev.logs.logcheckConfig.handler;

import com.sludev.logs.logcheckConfig.entity.LCCAppState;
import com.sludev.logs.logcheckConfig.main.LogCheckConfigMain;
import com.sludev.logs.logcheckConfig.util.LCCConstants;
import com.sludev.logs.logcheckConfig.util.LCCFileChooserHelper;
import com.sludev.logs.logcheckConfig.util.LCCPreferenceHelper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Created by kervin on 2016-02-18.
 */
public final class LCCBrowseHandler
{
    private static final Logger LOGGER = LogManager.getLogger(LCCBrowseHandler.class);

    public static void doConfigBrowse(final LogCheckConfigMain app,
                                      final ActionEvent event,
                                      final TextField textField,
                                      final Menu fileLoadMenu,
                                      final MenuItem fileLoadMenuItem,
                                      final MenuItem fileLoadClearHistMenuItem)
    {
        List<Pair<String, String>> exts = new ArrayList<>();
        exts.add(Pair.of(LCCConstants.LCC_DEFAULT_CONFIG_EXT_DESC,
                LCCConstants.LCC_DEFAULT_CONFIG_EXT));
        exts.add(Pair.of(LCCConstants.LCC_DEFAULT_ALLFILES_EXT_DESC,
                LCCConstants.LCC_DEFAULT_ALLFILES_EXT));

        Path file = LCCFileChooserHelper.showBrowse(textField,
                exts, "Choose the config file for loading", null);

        if( file != null )
        {
            LCCAppState appState = app.getAppState();
            LCCPreferenceHelper.addAndRotateLoadFileHistory(appState.getPreferences(), file.toString());

            // Platform.runLater(this::refreshLoadHistoryMenu);
            refreshLoadHistoryMenu(app, fileLoadMenu,
                    fileLoadMenuItem, fileLoadClearHistMenuItem, textField);
        }
    }

    public static void doLockFileBrowse(final LogCheckConfigMain app,
                                      final ActionEvent event,
                                      final TextField textField,
                                      final String title)
    {
        List<Pair<String, String>> exts = new ArrayList<>();
        exts.add(Pair.of(LCCConstants.LCC_DEFAULT_LOCK_EXT_DESC,
                LCCConstants.LCC_DEFAULT_LOCK_EXT));
        exts.add(Pair.of(LCCConstants.LCC_DEFAULT_ALLFILES_EXT_DESC,
                LCCConstants.LCC_DEFAULT_ALLFILES_EXT));

        Path file = LCCFileChooserHelper.showBrowse(textField,
                exts, title, null);

        if( file != null )
        {
            textField.setText(file.toString());
        }
    }

    public static void onFileLoadClearHist(final LogCheckConfigMain app,
                                           final ActionEvent event,
                                           final TextField textField,
                                           final Menu fileLoadMenu,
                                           final MenuItem fileLoadMenuItem,
                                           final MenuItem fileLoadClearHistMenuItem)
    {
        LCCAppState appState = app.getAppState();

        LCCPreferenceHelper.clearLoadFileHistory(appState.getPreferences());
        refreshLoadHistoryMenu(app, fileLoadMenu,
                fileLoadMenuItem, fileLoadClearHistMenuItem, textField);
    }

    public static void refreshLoadHistoryMenu(final LogCheckConfigMain app,
                                              final Menu fileLoadMenu,
                                              final MenuItem fileLoadMenuItem,
                                              final MenuItem fileLoadClearHistMenuItem,
                                              final TextField textField)
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

        rebuildConfigFileHistMenu( app,
                appState.getPreferences(),
                fileLoadMenu,
                fileLoadMenuItem,
                fileLoadClearHistMenuItem,
                currItems,
                textField);

        fileLoadMenu.setDisable(false);
    }

    public static void rebuildConfigFileHistMenu(final LogCheckConfigMain app,
                                                 final Preferences pref,
                                                 final Menu fileLoadMenu,
                                                 final MenuItem fileLoadMenuItem,
                                                 final MenuItem fileLoadClearHistMenuItem,
                                                 final List<MenuItem> resItems,
                                                 final TextField textField)
    {

        String currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST01, null);
        if( currPrefValue != null )
        {
            MenuItem tempItem;

            {
                final String tempVal = currPrefValue;
                tempItem = new MenuItem(currPrefValue);
                tempItem.setOnAction( event ->
                {
                    textField.setText(tempVal);
                    LCCPreferenceHelper.addAndRotateLoadFileHistory(pref, tempVal);
                    refreshLoadHistoryMenu(app, fileLoadMenu, fileLoadMenuItem,
                            fileLoadClearHistMenuItem, textField);
                });
                resItems.add(tempItem);
            }

            currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST02, null);
            if( currPrefValue != null )
            {
                {
                    final String tempVal = currPrefValue;
                    tempItem = new MenuItem(currPrefValue);
                    tempItem.setOnAction( event ->
                    {
                        textField.setText(tempVal);
                        LCCPreferenceHelper.addAndRotateLoadFileHistory(pref, tempVal);
                        refreshLoadHistoryMenu(app, fileLoadMenu, fileLoadMenuItem,
                                fileLoadClearHistMenuItem, textField);
                    });
                    resItems.add(tempItem);
                }

                currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST03, null);
                if( currPrefValue != null )
                {
                    {
                        final String tempVal = currPrefValue;
                        tempItem = new MenuItem(currPrefValue);
                        tempItem.setOnAction( event ->
                        {
                            textField.setText(tempVal);
                            LCCPreferenceHelper.addAndRotateLoadFileHistory(pref, tempVal);
                            refreshLoadHistoryMenu(app, fileLoadMenu, fileLoadMenuItem,
                                    fileLoadClearHistMenuItem, textField);
                        });
                        resItems.add(tempItem);
                    }

                    currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST04, null);
                    if( currPrefValue != null )
                    {
                        {
                            final String tempVal = currPrefValue;
                            tempItem = new MenuItem(currPrefValue);
                            tempItem.setOnAction( event ->
                            {
                                textField.setText(tempVal);
                                LCCPreferenceHelper.addAndRotateLoadFileHistory(pref, tempVal);
                                refreshLoadHistoryMenu(app, fileLoadMenu, fileLoadMenuItem,
                                        fileLoadClearHistMenuItem, textField);
                            });
                            resItems.add(tempItem);
                        }

                        currPrefValue = pref.get(LCCConstants.LCC_CONFIG_FILE_HIST05, null);
                        if( currPrefValue != null )
                        {
                            {
                                final String tempVal = currPrefValue;
                                tempItem = new MenuItem(currPrefValue);
                                tempItem.setOnAction( event ->
                                {
                                    textField.setText(tempVal);
                                    LCCPreferenceHelper.addAndRotateLoadFileHistory(pref, tempVal);
                                    refreshLoadHistoryMenu(app, fileLoadMenu, fileLoadMenuItem,
                                            fileLoadClearHistMenuItem, textField);
                                });
                                resItems.add(tempItem);
                            }
                        }
                    }
                }
            }
        }
    }
}
