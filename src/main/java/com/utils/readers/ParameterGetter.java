package com.utils.readers;

import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;


public class ParameterGetter {
    private static final Logger LOGGER = LogManager.getLogger(ParameterGetter.class);
    private static final int ATM_ID_MAX_LENGTH = 100;
    private static final Long MIN_ACCOUNT_NUMBER = Long.valueOf("1000000000000000");
    private static final Long MAX_ACCOUNT_NUMBER = Long.valueOf("9999999999999999");

    private static final Long MIN_CARD_NUMBER = Long.valueOf("1000000000000");
    private static final Long MAX_CARD_NUMBER = Long.valueOf("9999999999999999");

    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 6;

    public static BigDecimal getAmount(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting amount {} from json", fieldName);
        try {
            BigDecimal amount = js.getBigDecimal(fieldName);
            if (amount.signum() == -1) {
                throw new InvalidParameterException("Negative amount not allowed");
            }
            return amount;
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    public static String getCurrency(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting currency {} from json", fieldName);
        try {
            return js.getString(fieldName);
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    public static Long getAccountNumber(JSONObject js, String fieldName) throws InvalidParameterException {
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

    public static Long getCustomerID(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting customerID {} from json", fieldName);
        try {
            return js.getLong(fieldName);
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    public static String getPin(JSONObject js, String fieldName) throws InvalidParameterException {
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

    public static JSONObject getCardInfo(JSONObject js, String fieldName) throws InvalidParameterException {
        LOGGER.info("Getting card_info {} from json", fieldName);
        try {
            return js.getJSONObject(fieldName);
        } catch (JSONException e) {
            LOGGER.error("Failed to get {} from JSON: {}", fieldName, e.getMessage(), e);
            throw new InvalidParameterException(fieldName + " is missing or has wrong type");
        }
    }

    public static Long getCardNumber(JSONObject js, String fieldName) throws InvalidParameterException {
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

    public static String getATM_ID(JSONObject js, String fieldName) throws InvalidParameterException {
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
