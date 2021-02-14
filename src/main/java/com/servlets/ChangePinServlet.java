package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.db_exceptions.BaseDatabaseClientException;
import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import com.utils.readers.ParameterGetter;
import com.utils.servlet_helpers.BaseServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;


@WebServlet(name = "ChangePinServlet", urlPatterns = "/changePin")
public class ChangePinServlet extends BaseServlet {

    @Override
    protected JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException {
        Long cardNumber = ParameterGetter.getCardNumber(jsonObject, "cardNumber");
        String newPin = ParameterGetter.getPin(jsonObject, "newPin");
        DatabaseClient.changePin(cardNumber, newPin);
        JSONObject resultJson = new JSONObject();
        resultJson.put("result", "Pin change successful");
        return resultJson;
    }
}
