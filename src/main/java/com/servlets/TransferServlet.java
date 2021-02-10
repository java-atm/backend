package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.db_exceptions.BaseDatabaseClientException;
import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import com.utils.readers.ParameterGetter;
import com.utils.servlet_helpers.BaseServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import java.math.BigDecimal;


@WebServlet(name = "TransferServlet", urlPatterns = "/transfer")
public class TransferServlet extends BaseServlet {
    @Override
    protected JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException {
        Long fromAccount = ParameterGetter.getAccountNumber(jsonObject, "from");
        Long toAccount = ParameterGetter.getAccountNumber(jsonObject, "to");
        String currency = ParameterGetter.getCurrency(jsonObject, "currency");
        BigDecimal amount = ParameterGetter.getAmount(jsonObject, "amount");
        if (fromAccount.equals(toAccount)) {
            throw new InvalidParameterException("From and to accounts are the same");
        }
        DatabaseClient.transfer(fromAccount, toAccount, amount, currency);
        JSONObject resultJSON = new JSONObject();
        resultJSON.put("result", "Transfer completed.");
        return resultJSON;
    }
}
