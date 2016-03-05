package com.sludev.logs.logcheckConfig.entity;

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheckConfig.enums.LCCErrorMsgType;
import javafx.scene.control.Control;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Created by kervin on 2016-02-12.
 */
public final class LCCAppState
{
    private static final Logger LOGGER = LogManager.getLogger(LCCAppState.class);

    private String configFile;
    private String argFile;
    private Preferences preferences;
    private LogCheckConfig config;

    private final Map<Control, List<Pair<LCCErrorMsgType, String>>> controlErrorMap;
    private final Map<Control, List<Pair<LCCErrorMsgType, String>>> controlWarningMap;

    public Preferences getPreferences()
    {
        return preferences;
    }

    public void setPreferences(Preferences preferences)
    {
        this.preferences = preferences;
    }

    public String getConfigFile()
    {
        return configFile;
    }

    public void setConfigFile(String s)
    {
        configFile = s;
    }

    public LogCheckConfig getConfig()
    {
        return config;
    }

    public void setConfig(LogCheckConfig c)
    {
        config = c;
    }

    public String getArgFile()
    {
        return argFile;
    }

    public void setArgFile(String a)
    {
        argFile = a;
    }

    public LCCAppState()
    {
        configFile  = null;
        preferences = null;
        config = null;
        argFile = null;

        controlErrorMap   = new HashMap<>();
        controlWarningMap = new HashMap<>();
    }

    public void addOrReplaceControlErrors(final Control cntrl,
                                         final LCCErrorMsgType type,
                                         final String msg)
    {
        List<Pair<LCCErrorMsgType, String>> errs = getControlErrors(cntrl);
        if( errs == null )
        {
            errs = new ArrayList<>();
            controlErrorMap.put(cntrl, errs);
        }
        else
        {
            Iterator<Pair<LCCErrorMsgType, String>> errsI = errs.iterator();
            while( errsI.hasNext() )
            {
                Pair<LCCErrorMsgType, String> currP = errsI.next();
                if( currP.getLeft().equals(type) )
                {
                    errsI.remove();
                }
            }
        }

        if( msg != null )
        {
            errs.add(Pair.of(type, msg));
        }
    }

    public List<Pair<LCCErrorMsgType, String>> getControlErrors(Control cntrl)
    {
        List<Pair<LCCErrorMsgType, String>> res = controlErrorMap.get(cntrl);

        return res;
    }

    public List<Pair<LCCErrorMsgType, String>> getControlWarnings(Control cntrl)
    {
        List<Pair<LCCErrorMsgType, String>> res = null;

        return res;
    }

    public void updateValidationTextFlow(TextFlow tf)
    {
        tf.getChildren().clear();

        boolean hasErrs = false;
        for( List<Pair<LCCErrorMsgType, String>> errs : controlErrorMap.values() )
        {
            if( errs.isEmpty() == false )
            {
                hasErrs = true;
                break;
            }
        }

        if( hasErrs )
        {
            tf.getStyleClass().clear();
            tf.getStyleClass().add("general-validatation-error-textflow");
        }
        else
        {
            tf.getStyleClass().clear();
            tf.getStyleClass().add("general-validatation-textflow");
        }

        for( Control cntrl : controlErrorMap.keySet() )
        {
            for( Pair<LCCErrorMsgType, String> err : getControlErrors(cntrl) )
            {
                Text t = new Text(err.getRight());
                tf.getChildren().add(t);
            }
        }
    }
}
