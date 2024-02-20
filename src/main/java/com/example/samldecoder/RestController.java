package com.example.samldecoder;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.keycloak.saml.SAMLRequestParser;
import org.keycloak.saml.SamlProtocolExtensionsAwareBuilder;
import org.opensaml.xml.io.UnmarshallingException;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@org.springframework.web.bind.annotation.RestController
public class RestController {
    @PostMapping("/sendSaml")
    public String sendResponse(@RequestBody String Request) {
        return RestImpl.response(Request);
    }

    @PostMapping("/sendFile")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        return RestImpl.responseForFileData(file);
    }

}
