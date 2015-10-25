/*
 *   SLU Dev Inc. CONFIDENTIAL
 *   DO NOT COPY
 *
 *  Copyright (c) [2012] - [2015] SLU Dev Inc. <info@sludev.com>
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *   the property of SLU Dev Inc. and its suppliers,
 *   if any.  The intellectual and technical concepts contained
 *   herein are proprietary to SLU Dev Inc. and its suppliers and
 *   may be covered by U.S. and Foreign Patents, patents in process,
 *   and are protected by trade secret or copyright law.
 *   Dissemination of this information or reproduction of this material
 *   is strictly forbidden unless prior written permission is obtained
 *   from SLU Dev Inc.
 */
package com.sludev.logs.logcheck.config;

import com.sludev.logs.logcheck.utils.FSSConfigurationFile;
import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckLSResourceResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Administrator
 */
public class LogCheckConfigFile
{
    private static final Logger log 
                          = LogManager.getLogger(LogCheckConfigFile.class);

    public static void parse(Document confDocument, Path conf) throws LogCheckException
    {
        log.debug( String.format( "Configuration FilePath : '%s'\n", conf ) );
        
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder currDocBuilder;
        
        try
        {
            currDocBuilder = dbFactory.newDocumentBuilder();
            try
            {
                confDocument = currDocBuilder.parse( conf.toFile() );
            }
            catch (SAXException ex)
            {
                String errMsg = String.format("Error parsing XML Configuration file syntax.");
                
                log.error(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
            catch (IOException ex)
            {
                String errMsg = String.format("Error reading the XML Configuration file on disk.");
                
                log.error(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
        catch (ParserConfigurationException ex)
        {
            String errMsg = String.format("Error parsing XML Configuration file");
                
            log.error(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }
 
        confDocument.getDocumentElement().normalize();
    }
    
    public static LogCheckConfig read(Path confPath) throws LogCheckException
    {
        LogCheckConfig res;

        FSSConfigurationFile conf = new FSSConfigurationFile();
        Document doc = conf.read(confPath);

        parse(doc, confPath);
        
        NodeList currElList;
        Element currEl;
        Element tempEl;
        Schema sch;
        
        SchemaFactory factory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // associate the schema factory with the resource resolver, which is responsible for resolving the imported XSD's
        factory.setResourceResolver(new LogCheckLSResourceResolver());

        // note that if your XML already declares the XSD to which it has to conform, then there's no need to create a validator from a Schema object
        Source schemaFile = new StreamSource(LogCheckConfigFile.class.getClassLoader()
                                    .getResourceAsStream("logcheckconfig.xsd"));
        
        Schema schema;
        try
        {
            schema = factory.newSchema(schemaFile);
                    Validator validator = schema.newValidator();
            try
            {
                validator.validate(new DOMSource(doc));
            }
            catch (SAXException ex)
            {
                String errMsg = String.format("Error parsing configuration document.");

                log.error(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
            catch (IOException ex)
            {
                String errMsg = String.format("Error reading configuration document on disk.");

                log.error(errMsg, ex);
                throw new LogCheckException(errMsg, ex);
            }
        }
        catch (SAXException ex)
        {
            String errMsg = String.format("Error parsing schema document.");
            
            log.error(errMsg, ex);
            throw new LogCheckException(errMsg, ex);
        }
        
        currEl = doc.getDocumentElement();
        
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        String holdingDirStr = null;
        String pollIntervalStr = null;
        String smtpServerStr = null;
        String smtpUserStr = null;
        String smtpPassStr = null;
        String smtpPortStr = null;
        String smtpProtocolStr = null;
        Boolean dryRun = null;
        String lockFileStr = null;
        String elasticsearchURLStr = null;
        String logFileStr = null;
        String statusFileStr = null;
        String leBuilderType = null;
        
        try
        {
            holdingDirStr = currXPath.compile("./holdingFolder").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            leBuilderType = currXPath.compile("./logEntryBuilderType").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            pollIntervalStr = currXPath.compile("./pollInterval").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpServerStr = currXPath.compile("./smtpServer").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpUserStr = currXPath.compile("./smtpUser").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpPassStr = currXPath.compile("./smtpPass").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpPortStr = currXPath.compile("./smtpPort").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            smtpProtocolStr = currXPath.compile("./smtpProtocol").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            String tempStr = currXPath.compile("./dryRun").evaluate(currEl);
            dryRun = Boolean.parseBoolean(tempStr);
        }
        catch(Exception ex)
        {
            log.debug("configuration parsing error 'dryRun'.", ex);
        }
        
        try
        {
            lockFileStr = currXPath.compile("./lockFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            logFileStr = currXPath.compile("./logFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            elasticsearchURLStr = currXPath.compile("./elasticsearchURL").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        try
        {
            statusFileStr = currXPath.compile("./statusFilePath").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }

        res = LogCheckConfig.from(null,
            null, // service,
            null, // emailOnError,
            smtpServerStr,
            smtpPortStr,
            smtpPassStr,
            smtpUserStr,
            smtpProtocolStr,
            dryRun,
            null, // showVersion,
            null, // printLog,
            null, // tailFromEnd,
            lockFileStr,
            logFileStr,
            statusFileStr,
            null, // configFilePath,
            holdingDirStr,
            elasticsearchURLStr,
            null, // elasticsearchIndexName,
            null, // elasticsearchIndexPrefix,
            null, // elasticsearchLogType,
            null, // elasticsearchIndexNameFormat,
            null, // logCutoffDate,
            null, // logCutoffDuration,
            null, // logDeduplicationDuration,
            pollIntervalStr,
            leBuilderType);

        return res;
    }
}
