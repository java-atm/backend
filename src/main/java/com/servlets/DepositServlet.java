package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.db_exceptions.BaseDatabaseClientException;
import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import com.utils.readers.ParameterGetter;
import com.utils.servlet_helpers.BaseServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import java.math.BigDecimal;


@WebServlet(name = "DepositServlet", urlPatterns = "/deposit")
public class DepositServlet extends BaseServlet {
    @Override
    protected JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException {
        Long accountNumber = ParameterGetter.getAccountNumber(jsonObject, "accountNumber");
        String currency = ParameterGetter.getCurrency(jsonObject, "currency");
        BigDecimal amount = ParameterGetter.getAmount(jsonObject, "amount");

        DatabaseClient.depositToAccount(accountNumber, amount, currency);
        JSONObject resultJSON = new JSONObject();
        resultJSON.put("result", "Deposit completed.");
        return resultJSON;
    }
}
