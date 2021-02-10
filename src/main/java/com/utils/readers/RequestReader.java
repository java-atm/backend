package com.utils.readers;

import com.utils.exceptions.readers_exceptions.JSONParsingFailedException;
import com.utils.exceptions.readers_exceptions.RequestDataReadingFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public interface RequestReader {
    Logger LOGGER = LogManager.getLogger(RequestReader.class);

    static String getRequestData(HttpServletRequest request) throws RequestDataReadingFailedException {
        try {
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuilder requestData = new StringBuilder();
            String line;
            while ((line = requestReader.readLine()) != null) {
                requestData.append(line);
            }
            LOGGER.info("String reading finished : {}", requestData.toString());
            return requestData.toString();
        } catch (IOException e) {
            LOGGER.error("Failed to read string data: {}", e.getMessage(), e);
            throw new RequestDataReadingFailedException("Failed to read string data: " + e.getMessage());
        }
    }

    static JSONObject getJSONData(HttpServletRequest request) throws JSONParsingFailedException {
        LOGGER.info("Getting request content");
        String requestData;
        try {
            requestData = getRequestData(request);
        } catch ( RequestDataReadingFailedException e) {
            LOGGER.error("Failed to read string data: {}", e.getMessage(), e);
            throw new JSONParsingFailedException(e.getMessage());
        }
        try {
            JSONObject j = new JSONObject(requestData);
            LOGGER.info("JSON created: {}", j.toString());
            return j;
        } catch (JSONException e) {
            LOGGER.error("Failed to parse JSON: {}", e.getMessage(), e);
            throw new JSONParsingFailedException(e.getMessage());
        }
    }

}
