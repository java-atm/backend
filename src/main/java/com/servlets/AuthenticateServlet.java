package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.db_exceptions.BaseDatabaseClientException;
import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import com.utils.readers.ParameterGetter;
import com.utils.servlet_helpers.BaseServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;


@WebServlet(name = "AuthenticateServlet", urlPatterns = "/auth")
public class AuthenticateServlet extends BaseServlet {
    private final Logger LOGGER = LogManager.getLogger(AuthenticateServlet.class);

    @Override
    protected JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException {
        JSONObject resultJson = new JSONObject();
        JSONObject card_info = ParameterGetter.getCardInfo(jsonObject, "card_info");
        Long cardNumber = ParameterGetter.getCardNumber(card_info, "CARD_NUMBER");
        String pin = ParameterGetter.getPin(jsonObject, "pin");
        LOGGER.info("cardNumber and pin received, processing");

        Long customerID = DatabaseClient.getCustomerIDByCardID(cardNumber, pin);
        resultJson.put("result", customerID);
        return resultJson;
    }
}
