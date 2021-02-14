package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.db_exceptions.BaseDatabaseClientException;
import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import com.utils.readers.ParameterGetter;
import com.utils.servlet_helpers.BaseServlet;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;


@WebServlet(name = "GetAccountsServlet", urlPatterns = "/getAccountsByCustomerID")
public class GetAccountsServlet extends BaseServlet {
    @Override
    protected JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException {
        Long customerID = ParameterGetter.getCustomerID(jsonObject, "customerID");
        boolean includeBalances = true;
        try {
            includeBalances = jsonObject.getBoolean("includeBalances");
        } catch (JSONException e) {
            System.out.println("Failed to get includeBalances param, using default true: " + e.getMessage());
        }
        JSONObject resultJSON= new JSONObject();
        if (includeBalances) {
            HashMap<String, BigDecimal> result = DatabaseClient.getCustomerBalances(customerID);
            resultJSON.put("result", result);

        } else {
            ArrayList<String> result = DatabaseClient.getCustomerAccounts(customerID);
            resultJSON.put("result", result);
        }
        return resultJSON;
    }


}
