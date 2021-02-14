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


@WebServlet(name = "GetCustomerNameServlet", urlPatterns = "/getCustomerName")
public class GetCustomerNameServlet extends BaseServlet {
    private final Logger LOGGER = LogManager.getLogger(GetCustomerNameServlet.class);

    protected JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException {
        JSONObject resultJson = new JSONObject();
        Long accountNumber = ParameterGetter.getAccountNumber(jsonObject, "accountNumber");
        LOGGER.info("accountNumber and atm_id received, processing");

        String customerName= DatabaseClient.getCustomerFullNameByAccountNumber(accountNumber);
        LOGGER.info("Got name: {} for {}", customerName, accountNumber);
        resultJson.put("result", customerName);
        return resultJson;
    }
}
