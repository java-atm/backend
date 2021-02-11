package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.db_exceptions.BaseDatabaseClientException;
import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import com.utils.readers.ParameterGetter;
import com.utils.servlet_helpers.BaseServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import java.math.BigDecimal;
import java.util.HashMap;


@WebServlet(name = "CheckBalanceServlet", urlPatterns = "/checkBalance")
public class CheckBalanceServlet extends BaseServlet {

    @Override
    protected JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException {
        Long customerID = ParameterGetter.getCustomerID(jsonObject, "customerID");
        HashMap<String, BigDecimal> accounts = DatabaseClient.getCustomerBalances(customerID);
        JSONObject resultJSON = new JSONObject();
        resultJSON.put("result", accounts);
        return resultJSON;
    }
}
