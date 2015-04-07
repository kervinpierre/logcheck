/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sludev.logs.logcheck.config;

import com.sludev.logs.logcheck.utils.LogCheckException;
import com.sludev.logs.logcheck.utils.LogCheckLSResourceResolver;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Administrator
 */
public class LogCheckConfigFile
{
    private static final Logger log 
                          = LogManager.getLogger(LogCheckConfigFile.class);
    
    private Path filePath;
    private LogCheckConfig config;

    private Document confDocument;
        
    public LogCheckConfig getConfig()
    {
        return config;
    }

    public void setConfig(LogCheckConfig c)
    {
        this.config = c;
    }

    public Document getConfDocument()
    {
        return confDocument;
    }

    public void setConfDocument(Document confDocument)
    {
        this.confDocument = confDocument;
    }

    public Path getFilePath()
    {
        return filePath;
    }

    public void setFilePath(Path f)
    {
        this.filePath = f;
    }
    
    public void setFilePath(String f)
    {
        Path p = Paths.get(f);
        this.filePath = p;
    }

    public LogCheckConfigFile()
    {
    }
    
    public void parse(Path conf) throws LogCheckException
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
    
    public void read() throws LogCheckException
    {
        parse(filePath);
        
        NodeList currElList;
        Element currEl;
        Element tempEl;
        Schema sch;

        LogCheckConfig res = new LogCheckConfig();
        
        SchemaFactory factory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // associate the schema factory with the resource resolver, which is responsible for resolving the imported XSD's
        factory.setResourceResolver(new LogCheckLSResourceResolver());

        // note that if your XML already declares the XSD to which it has to conform, then there's no need to create a validator from a Schema object
        Source schemaFile = new StreamSource(getClass().getClassLoader()
                                    .getResourceAsStream("logcheckconfig.xsd"));
        
        Schema schema;
        try
        {
            schema = factory.newSchema(schemaFile);
                    Validator validator = schema.newValidator();
            try
            {
                validator.validate(new DOMSource(getConfDocument()));
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
        
        currEl = getConfDocument().getDocumentElement();
        
        XPathFactory currXPathfactory = XPathFactory.newInstance();
        XPath currXPath = currXPathfactory.newXPath();
        String holdFolderStr = null;
        String cronSchedStr = null;
        String smtpServerStr = null;
        String smtpUserStr = null;
        String smtpPassStr = null;
        String smtpPortStr = null;
        String smtpProtocolStr = null;
        String dryRunStr = null;
        String lockFileStr = null;
        String redisHostStr = null;
        String redisPortStr = null;
        String logFileStr = null;
        String statusFileStr = null;
        
        try
        {
            holdFolderStr = currXPath.compile("./holdingFolder").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            cronSchedStr = currXPath.compile("./cronSchedule").evaluate(currEl);
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
            dryRunStr = currXPath.compile("./dryRun").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
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
            redisHostStr = currXPath.compile("./redisHost").evaluate(currEl);
        }
        catch (XPathExpressionException ex)
        {
            log.debug("configuration parsing error.", ex);
        }
        
        try
        {
            redisPortStr = currXPath.compile("./redisPort").evaluate(currEl);
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
        
        if( StringUtils.isNoneBlank(holdFolderStr) )
        {
            config.setHoldingFolderPath(holdFolderStr);
        }
        
        if( StringUtils.isNoneBlank(cronSchedStr) )
        {
            config.setCronScheduleString(cronSchedStr);
        }
        
        
        if( StringUtils.isNoneBlank(smtpServerStr) )
        {
            config.setSmtpServer(smtpServerStr);
        }
        
        if( StringUtils.isNoneBlank(smtpUserStr) )
        {
            config.setSmtpUser(smtpUserStr);
        }
        
        if( StringUtils.isNoneBlank(smtpPassStr) )
        {
            config.setSmtpPass(smtpPassStr);
        }
        
        if( StringUtils.isNoneBlank(smtpPortStr) )
        {
            config.setSmtpPort(smtpPortStr);
        }
        
        if( StringUtils.isNoneBlank(smtpProtocolStr) )
        {
            config.setSmtpProto(smtpProtocolStr);
        }
        
        if( StringUtils.isNoneBlank(dryRunStr) )
        {
            config.setDryRun(dryRunStr);
        }
        
        if( StringUtils.isNoneBlank(lockFileStr) )
        {
            config.setLockFilePath(lockFileStr);
        }
        
        if( StringUtils.isNoneBlank(logFileStr) )
        {
            config.setLogPath(logFileStr);
        }
        
        if( StringUtils.isNoneBlank(redisHostStr) )
        {
            config.setRedisHost(redisHostStr);
        }
        
        if( StringUtils.isNoneBlank(redisPortStr) )
        {
            config.setRedisPort(redisPortStr);
        }
        
        if( StringUtils.isNoneBlank(statusFileStr) )
        {
            config.setStatusFilePath(statusFileStr);
        }
    }
}
