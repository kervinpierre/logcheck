/*
 * SLU Dev Inc. CONFIDENTIAL
 * DO NOT COPY
 *   
 * Copyright (c) [2012] - [2016] SLU Dev Inc. <info@sludev.com>
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of SLU Dev Inc. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to SLU Dev Inc. and its suppliers and
 * may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from SLU Dev Inc.
 */

package com.sludev.logs.logcheck.config.writers;

import com.sludev.logs.logcheck.config.entities.LogCheckConfig;
import com.sludev.logs.logcheck.enums.FSSVerbosityEnum;
import com.sludev.logs.logcheck.enums.LCDeDupeAction;
import com.sludev.logs.logcheck.enums.LCDebugFlag;
import com.sludev.logs.logcheck.enums.LCFileRegexComponent;
import com.sludev.logs.logcheck.enums.LCLogEntryBuilderType;
import com.sludev.logs.logcheck.enums.LCLogEntryStoreType;
import com.sludev.logs.logcheck.exceptions.LogCheckException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by kervin on 2016-03-01.
 */
public final class LogCheckConfigWriter
{
    private static final Logger LOGGER = LogManager.getLogger(LogCheckConfigWriter.class);

    public static void write( final Map<Integer, LogCheckConfig> js,
                              final Path confFile ) throws LogCheckException
    {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;

        try
        {
            docBuilder = docFactory.newDocumentBuilder();
        }
        catch( ParserConfigurationException ex )
        {
            String errMsg = "Error creating document builder.";

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        Document doc = docBuilder.newDocument();

        Element rootElem = doc.createElement("logCheckConfigs");
        for( Integer i : js.keySet() )
        {
            Element currElem = LogCheckConfigWriter.toElement(doc, "logCheckConfig", js.get(i));
            rootElem.appendChild(currElem);
        }
        doc.appendChild(rootElem);
        
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try
        {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
        catch( TransformerConfigurationException ex )
        {
            String errMsg = "Error creating transformer";

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(confFile.toFile());

        try
        {
            transformer.transform(source, result);
        }
        catch( TransformerException | NullPointerException ex )
        {
            String errMsg = String.format("Error writing '%s'", confFile);

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }
    }

    public static Path write( final Map<Integer, LogCheckConfig> js) throws LogCheckException
    {
        Path res = null;

        try
        {
            res = Files.createTempFile("logCheckConfig", ".xml.tmp");
        }
        catch( IOException ex )
        {
            String errMsg = String.format("Error creating temp file LogCheckConfig");

            LOGGER.debug(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }

        write(js, res);

        return res;
    }

    public static Element toElement( final Document doc,
                                     final String elementName,
                                     final LogCheckConfig lcc ) throws LogCheckException
    {
        Element res = null;
        Element currElem = null;

        res = doc.createElement(elementName);
        if( lcc.getId() == null
                || lcc.getId() < 0 )
        {
            throw new LogCheckException("Missing ID value");
        }
        else
        {
            res.setAttribute("id", lcc.getId().toString());
        }

        // service
        Boolean currBoolean = lcc.isService();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <service />");
        }
        else
        {
            currElem = doc.createElement("service");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // holdingDirectory
        Path currPath = lcc.getHoldingDirPath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <holdingDirectory />");
        }
        else
        {
            currElem = doc.createElement("holdingDirectory");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // elasticsearchURL
        URL currURL = lcc.getElasticsearchURL();
        if( currURL == null )
        {
            ; // throw new LogCheckException("Missing <elasticsearchURL />");
        }
        else
        {
            currElem = doc.createElement("elasticsearchURL");
            currElem.appendChild(doc.createTextNode(currURL.toString()));
            res.appendChild(currElem);
        }

        // pollInterval
        Long currLong = lcc.getPollIntervalSeconds();
        if( currLong == null )
        {
            ; // throw new LogCheckException("Missing <pollInterval />");
        }
        else if( currLong > -1 )
        {
            currElem = doc.createElement("pollInterval");
            currElem.appendChild(doc.createTextNode(currLong.toString()));
            res.appendChild(currElem);
        }

        // lockFilePath
        currPath = lcc.getLockFilePath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <lockFilePath />");
        }
        else
        {
            currElem = doc.createElement("lockFilePath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // logFilePath
        currPath = lcc.getLogPath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <logFilePath />");
        }
        else
        {
            currElem = doc.createElement("logFilePath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // statusFilePath
        currPath = lcc.getStatusFilePath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <statusFilePath />");
        }
        else
        {
            currElem = doc.createElement("statusFilePath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // storeLogPath
        currPath = lcc.getStoreLogPath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <storeLogPath />");
        }
        else
        {
            currElem = doc.createElement("storeLogPath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // stateFilePath
        currPath = lcc.getStateFilePath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <stateFilePath />");
        }
        else
        {
            currElem = doc.createElement("stateFilePath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // stateProcessedLogsFilePath
        currPath = lcc.getStateProcessedLogsFilePath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <stateProcessedLogFilePath />");
        }
        else
        {
            currElem = doc.createElement("stateProcessedLogFilePath");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // preferredDir
        currPath = lcc.getPreferredDir();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <preferredDir />");
        }
        else
        {
            currElem = doc.createElement("preferredDir");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // stdOutFile
        currPath = lcc.getStdOutFile();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <stdOutFile />");
        }
        else
        {
            currElem = doc.createElement("stdOutFile");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationDefaultAction
        LCDeDupeAction currDDAction = lcc.getDeDupeDefaultAction();
        if( currDDAction == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationDefaultAction />");
        }
        else if( currDDAction != LCDeDupeAction.NONE )
        {
            currElem = doc.createElement("deDuplicationDefaultAction");
            currElem.appendChild(doc.createTextNode(currDDAction.toString().toLowerCase()));
            res.appendChild(currElem);
        }

        // emailOnError
        String currStr = lcc.getEmailOnError();
        if( StringUtils.isBlank(currStr) )
        {
            ; // throw new LogCheckException("Missing <emailOnError />");
        }
        else
        {
            currElem = doc.createElement("emailOnError");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // tailFromEnd
        currBoolean = lcc.isTailFromEnd();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <tailFromEnd />");
        }
        else
        {
            currElem = doc.createElement("tailFromEnd");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // saveState
        currBoolean = lcc.willSaveState();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <saveState />");
        }
        else
        {
            currElem = doc.createElement("saveState");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // continue
        currBoolean = lcc.willContinueState();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <continue />");
        }
        else
        {
            currElem = doc.createElement("continue");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // collectState
        currBoolean = lcc.willCollectState();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <collectState />");
        }
        else
        {
            currElem = doc.createElement("collectState");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // readOnlyLogFile
        currBoolean = lcc.isReadOnlyFileMode();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <readOnlyLogFile />");
        }
        else
        {
            currElem = doc.createElement("readOnlyLogFile");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // setName
        currStr = lcc.getSetName();
        if( StringUtils.isBlank(currStr) )
        {
            ; // throw new LogCheckException("Missing <setName />");
        }
        else
        {
            currElem = doc.createElement("setName");
            currElem.appendChild(doc.createTextNode(currStr));
            res.appendChild(currElem);
        }

        // stopAfter
        currLong = lcc.getStopAfter();
        if( currLong == null )
        {
            ; // throw new LogCheckException("Missing <stopAfter />");
        }
        else if( currLong > -1 )
        {
            currElem = doc.createElement("stopAfter");
            currElem.appendChild(doc.createTextNode(currLong.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationLogDir
        currPath = lcc.getDeDupeDirPath();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationLogDir />");
        }
        else
        {
            currElem = doc.createElement("deDuplicationLogDir");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationDuration
        Duration currDuration = lcc.getLogDeduplicationDuration();
        if( currDuration == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationDuration />");
        }
        else
        {
            currElem = doc.createElement("deDuplicationDuration");
            currElem.appendChild(doc.createTextNode(currDuration.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationMaxLogsBeforeWrite
        Integer currInt = lcc.getDeDupeMaxLogsBeforeWrite();
        if( currInt == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationMaxLogsBeforeWrite />");
        }
        else if( currInt > -1 )
        {
            currElem = doc.createElement("deDuplicationMaxLogsBeforeWrite");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationMaxLogsPerFile
        currInt = lcc.getDeDupeMaxLogsPerFile();
        if( currInt == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationMaxLogsPerFile />");
        }
        else if( currInt > -1 )
        {
            currElem = doc.createElement("deDuplicationMaxLogsPerFile");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationMaxLogFiles
        currInt = lcc.getDeDupeMaxLogFiles();
        if( currInt == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationMaxLogFiles />");
        }
        else if( currInt > -1 )
        {
            currElem = doc.createElement("deDuplicationMaxLogFiles");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // readMaxDeDupeEntries
        currInt = lcc.getReadMaxDeDupeEntries();
        if( currInt == null )
        {
            ; // throw new LogCheckException("Missing <readMaxDeDupeEntries />");
        }
        else if( currInt > -1 )
        {
            currElem = doc.createElement("readMaxDeDupeEntries");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationIgnoreUntilCount
        currLong = lcc.getDeDupeIgnoreCount();
        if( currLong == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationIgnoreUntilCount />");
        }
        else if( currLong > -1 )
        {
            currElem = doc.createElement("deDuplicationIgnoreUntilCount");
            currElem.appendChild(doc.createTextNode(currLong.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationSkipUntilCount
        currLong = lcc.getDeDupeSkipCount();
        if( currLong == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationSkipUntilCount />");
        }
        else if( currLong > -1 )
        {
            currElem = doc.createElement("deDuplicationSkipUntilCount");
            currElem.appendChild(doc.createTextNode(currLong.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationIgnoreUntilPercent
        currInt = lcc.getDeDupeIgnorePercent();
        if( currInt == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationIgnoreUntilPercent />");
        }
        else if( currInt > -1 )
        {
            currElem = doc.createElement("deDuplicationIgnoreUntilPercent");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // deDuplicationSkipUntilPercent
        currInt = lcc.getDeDupeSkipPercent();
        if( currInt == null )
        {
            ; // throw new LogCheckException("Missing <deDuplicationSkipUntilPercent />");
        }
        else if( currInt > -1 )
        {
            currElem = doc.createElement("deDuplicationSkipUntilPercent");
            currElem.appendChild(doc.createTextNode(currInt.toString()));
            res.appendChild(currElem);
        }

        // stopOnEOF
        currBoolean = lcc.willStopOnEOF();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <stopOnEOF />");
        }
        else
        {
            currElem = doc.createElement("stopOnEOF");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // ignoreStartPosError
        currBoolean = lcc.willIgnoreStartPositionError();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <ignoreStartPosError />");
        }
        else
        {
            currElem = doc.createElement("ignoreStartPosError");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // reOpenLogFile
        currBoolean = lcc.willReadReOpenLogFile();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <reOpenLogFile />");
        }
        else
        {
            currElem = doc.createElement("reOpenLogFile");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // tailerBackupReadPriorLog
        currBoolean = lcc.willTailerBackupReadPriorLog();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <tailerBackupReadPriorLog />");
        }
        else
        {
            currElem = doc.createElement("tailerBackupReadPriorLog");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // validateTailerStats
        currBoolean = lcc.willValidateTailerStats();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <validateTailerStats />");
        }
        else
        {
            currElem = doc.createElement("validateTailerStats");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // tailerBackupReadLog
        currBoolean = lcc.willTailerBackupReadLog();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <tailerBackupReadLog />");
        }
        else
        {
            currElem = doc.createElement("tailerBackupReadLog");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // tailerBackupReadLogReverse
        currBoolean = lcc.willTailerBackupReadLogReverse();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <tailerBackupReadLogReverse />");
        }
        else
        {
            currElem = doc.createElement("tailerBackupReadLogReverse");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // createMissingDirs
        currBoolean = lcc.willCreateMissingDirs();
        if( currBoolean == null )
        {
            ; // throw new LogCheckException("Missing <createMissingDirs />");
        }
        else
        {
            currElem = doc.createElement("createMissingDirs");
            currElem.appendChild(doc.createTextNode(currBoolean.toString()));
            res.appendChild(currElem);
        }

        // tailerLogBackupDir
        currPath = lcc.getTailerLogBackupDir();
        if( currPath == null )
        {
            ; // throw new LogCheckException("Missing <tailerLogBackupDir />");
        }
        else
        {
            currElem = doc.createElement("tailerLogBackupDir");
            currElem.appendChild(doc.createTextNode(currPath.toString()));
            res.appendChild(currElem);
        }

        // tailerBackupLogNameRegex
        Pattern currPattern = lcc.getTailerBackupLogNameRegex();
        if( currPattern == null )
        {
            ; // throw new LogCheckException("Missing <tailerBackupLogNameRegex />");
        }
        else
        {
            currElem = doc.createElement("tailerBackupLogNameRegex");
            if( currPattern.pattern() != null )
            {
                currElem.appendChild(doc.createTextNode(currPattern.pattern()));
            }
            res.appendChild(currElem);
        }

        // verbosity
        FSSVerbosityEnum currVerbosity = lcc.getVerbosity();
        if( currVerbosity == null )
        {
            ; // throw new LogCheckException("Missing <verbosity />");
        }
        else if( currVerbosity != FSSVerbosityEnum.NONE )
        {
            currElem = doc.createElement("verbosity");
            currElem.appendChild(doc.createTextNode(currVerbosity.toString().toLowerCase()));
            res.appendChild(currElem);
        }

        Element stores = null;

        Set<LCLogEntryStoreType> lcleStoreSet = new HashSet<>();
        for( LCLogEntryStoreType currStore : lcc.getLogEntryStores() )
        {
            if( lcleStoreSet.contains(currStore)
                    || (currStore == LCLogEntryStoreType.NONE) )
            {
                continue;
            }

            lcleStoreSet.add(currStore);

            currElem = doc.createElement("store");
            currElem.appendChild(doc.createTextNode(
                    StringUtils.lowerCase(currStore.name())));

            if( stores == null )
            {
                stores = doc.createElement("logEntryStores");
            }
            stores.appendChild(currElem);
        }

        if( stores != null )
        {
            res.appendChild(stores);
        }

        Element builders = null;

        Set<LCLogEntryBuilderType> lcleBuilderSet = new HashSet<>();
        for( LCLogEntryBuilderType currBuilder : lcc.getLogEntryBuilders() )
        {
            if( lcleBuilderSet.contains(currBuilder) || (currBuilder == LCLogEntryBuilderType.NONE) )
            {
                continue;
            }

            lcleBuilderSet.add(currBuilder);

            currElem = doc.createElement("builder");
            currElem.appendChild(doc.createTextNode(
                    StringUtils.lowerCase(currBuilder.name().replace('_', ' '))));

            if( builders == null )
            {
                builders = doc.createElement("logEntryBuilders");
            }
            builders.appendChild(currElem);
        }

        if( builders != null )
        {
            res.appendChild(builders);
        }

        if( (lcc.getTailerBackupLogNameComps() != null)
                && (lcc.getTailerBackupLogNameComps().size() > 0) )
        {
            Element nameComps = doc.createElement("tailerBackupLogNameComps");

            for( LCFileRegexComponent currComp : lcc.getTailerBackupLogNameComps() )
            {
                currElem = doc.createElement("nameComponent");
                currElem.appendChild(doc.createTextNode(
                        StringUtils.lowerCase(currComp.name()
                                .replace('_', '-')
                                .toLowerCase())));
                nameComps.appendChild(currElem);
            }

            res.appendChild(nameComps);
        }

        if( (lcc.getDebugFlags() != null) && (lcc.getDebugFlags().size() > 0))
        {
            Element debugFlags = doc.createElement("debugFlags");

            for( LCDebugFlag currFlag : lcc.getDebugFlags() )
            {
                currElem = doc.createElement("flag");
                currElem.appendChild(doc.createTextNode(
                        StringUtils.lowerCase(currFlag.name()
                                .replace('_', '-')
                                .toLowerCase())));
                debugFlags.appendChild(currElem);
            }

            res.appendChild(debugFlags);
        }

        return res;
    }

}
