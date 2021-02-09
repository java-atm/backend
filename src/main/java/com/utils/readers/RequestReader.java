package com.utils.readers;

import com.utils.exceptions.InvalidParameterException;
import com.utils.exceptions.JSONParsingFailedException;
import com.utils.exceptions.RequestDataReadingFailedException;
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
    int ATM_ID_MAX_LENGTH = 100;
    Long MIN_ACCOUNT_NUMBER = Long.valueOf("1000000000000000");
    Long MAX_ACCOUNT_NUMBER = Long.valueOf("9999999999999999");

    Long MIN_CARD_NUMBER = Long.valueOf("1000000000000");
    Long MAX_CARD_NUMBER = Long.valueOf("9999999999999999");

    int MIN_PIN_LENGTH = 4;
    int MAX_PIN_LENGTH = 6;


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

    static JSONObject getJSONData(HttpServletRequest request) throws JSONParsingFailedException{
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

    static Long getAccountNumber(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting AccountNumber {} from json", fieldName);
        try {
            Long accountNumber = js.getLong(fieldName);
            if (accountNumber.compareTo(MIN_ACCOUNT_NUMBER) < 0 || accountNumber.compareTo(MAX_ACCOUNT_NUMBER) > 0) {
                LOGGER.error("{} not within proper range: {}", fieldName, accountNumber.toString());
                throw new InvalidParameterException(String.format("%s not within proper range: %s", fieldName, accountNumber));
            }
            return accountNumber;
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    static String getPin(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting pin {} from json", fieldName);
        try {
            String pin = js.getString(fieldName);
            if (pin.length() < MIN_PIN_LENGTH || pin.length() > MAX_PIN_LENGTH) {
                LOGGER.error("{} length is invalid: {}", fieldName, pin);
                throw new InvalidParameterException(String.format("%s invalid length: %s", fieldName, pin));
            }
            for (int i = 0; i < pin.length(); i ++) {
                if (!Character.isLetterOrDigit(pin.charAt(i))) {
                    LOGGER.error("Pin contains illegal character");
                    throw new InvalidParameterException(fieldName + " contains invalid character");
                }
            }
            return pin;
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    static JSONObject getCardInfo(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting card_info {} from json", fieldName);
        try {
            return js.getJSONObject(fieldName);
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    static Long getCardNumber(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting CardNumber {} from json", fieldName);
        try {
            Long cardNumber = js.getLong(fieldName);
            if (cardNumber.compareTo(MIN_CARD_NUMBER) < 0 || cardNumber.compareTo(MAX_CARD_NUMBER) > 0) {
                LOGGER.error("{} not within proper range: {}", fieldName, cardNumber.toString());
                throw new InvalidParameterException(String.format("%s not within proper range: %s", fieldName, cardNumber));
            }
            return cardNumber;
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    static String getATM_ID(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting ATM ID {} from json", fieldName);
        try {
            String atm_id = js.getString("atm_id");
            if (atm_id.length() > ATM_ID_MAX_LENGTH) {
                LOGGER.error("ATM ID too long: {}", atm_id.length());
                throw new InvalidParameterException(String.format("Too long ATM ID(%s)", atm_id.length()));
            }
            LOGGER.info("Got {} from json", fieldName);
            return atm_id;
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }
}
