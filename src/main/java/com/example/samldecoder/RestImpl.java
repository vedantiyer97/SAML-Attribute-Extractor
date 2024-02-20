package com.example.samldecoder;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HttpMethod;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.ResponseUnmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.opensaml.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RestImpl {
    static Logger logger = LoggerFactory.getLogger(RestImpl.class);
    static org.apache.commons.codec.binary.Base64 b64 = new Base64();

    public static String response(String requestBody) {
        String decodedXML = convertToRawXML(requestBody);
        Map<String, String> attrMap = attributeMap(decodedXML);
        return attrMap.toString();
    }

    public static String convertToRawXML(String input) {
        JSONObject json = new JSONObject(input);
        String SAMLResponse = json.toMap().get("inputField").toString().trim();
        return new String(b64.decode(SAMLResponse));
    }

    public static Map<String, String> attributeMap(String xml) {
        Map<String, String> attrmap = new HashMap<>();
        try {
            //init the saml library, very important you were stuck here for 1 full day :D
            DefaultBootstrap.bootstrap();
            // Parse the SAML response
            ResponseUnmarshaller responseUnmarshaller = new ResponseUnmarshaller();
            BasicParserPool parserPool = new BasicParserPool();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            Element element = document.getDocumentElement();
            Response samlResponseObj = (Response) responseUnmarshaller.unmarshall(element);
            // Extract and print assertion attributes
            for (Assertion assertion : samlResponseObj.getAssertions()) {
                for (org.opensaml.saml2.core.AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
                    for (org.opensaml.saml2.core.Attribute attribute : attributeStatement.getAttributes()) {
                        attrmap.put(attribute.getName(), attribute.getAttributeValues().get(0).getDOM().getTextContent());
                        logger.info(attribute.getName() + "=" + attribute.getAttributeValues().get(0).getDOM().getTextContent());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return attrmap;
    }

    public static String responseForFileData(MultipartFile file) {
        String SAMLResponse = null;
        if (file.isEmpty()) {
            return "Please select a file to upload";
        }
        try {
            // Check if the file has .har extension
            if (!file.getOriginalFilename().endsWith(".har")) {
                return "Uploaded file is not a .har file";
            }

            // Save the file temporarily
            File tempFile = File.createTempFile("temp", ".har");
            file.transferTo(tempFile);
            HarReader harReader = new HarReader();
            Har har = harReader.readFromFile(tempFile);
            logger.info((har.getLog().getCreator().getName()));
            logger.info((har.getLog().getEntries().toString()));
            for(HarEntry entry : har.getLog().getEntries()){
                if((entry.getRequest().getUrl().startsWith("https://login-uat.fisglobal.com/idp/MemoCBAUAT/"))||entry.getRequest().getUrl().startsWith("https://login10.fisglobal.com/idp/CBA/")){
                   if((entry.getRequest().getMethod().equals(HttpMethod.POST))&&(entry.getRequest().getPostData().getText().startsWith("SAMLResponse"))){
                       SAMLResponse = entry.getRequest().getPostData().getText().substring(13);
                       SAMLResponse = URLDecoder.decode(SAMLResponse, "UTF-8");
                       SAMLResponse = new String(b64.decode(SAMLResponse));
                       logger.info(SAMLResponse);
                   }
                }
            }

        }
        catch (Exception ignored){
        }
        return attributeMap(SAMLResponse).toString();
    }
}


