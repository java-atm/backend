package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.ConnectionFailedException;
import com.utils.exceptions.CustomerNotFoundException;
import com.utils.exceptions.InvalidParameterException;
import com.utils.exceptions.JSONParsingFailedException;
import com.utils.readers.RequestReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(name = "GetCustomerNameServlet", urlPatterns = "/getCustomerName")
public class GetCustomerNameServlet extends HttpServlet {
    private final Logger LOGGER = LogManager.getLogger(GetCustomerNameServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("Processing getCustomerName from ADDR: {}", request.getRemoteAddr());
        JSONObject resultJson = new JSONObject();
        try (PrintWriter pr = response.getWriter()) {
            LOGGER.info("Parsing JSON");
            someFunction(request, response, pr);
        } catch (IOException e) {
            LOGGER.error("Something went wrong: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    private void someFunction(HttpServletRequest request, HttpServletResponse response, PrintWriter pr) {
        JSONObject resultJson = new JSONObject();
        JSONObject jsonObject;
        try {
            jsonObject = RequestReader.getJSONData(request);
        } catch (JSONParsingFailedException e) {
            response.setStatus(400);
            resultJson.put("error", "Invalid JSON content");
            pr.write(resultJson.toString());
            pr.flush();
            return;
        }
        LOGGER.info("JSON parsed, getting params: {}", jsonObject.toString());
        someFunction2(request, response, pr, jsonObject);
    }

    private void someFunction2(HttpServletRequest request, HttpServletResponse response, PrintWriter pr, JSONObject jsonObject) {
        JSONObject resultJson = new JSONObject();
        try {
            String atm_id = RequestReader.getATM_ID(jsonObject, "atm_id");
            Long accountNumber = RequestReader.getAccountNumber(jsonObject, "accountNumber");
            LOGGER.info("accountNumber and atm_id received, processing");
            if (! DatabaseClient.verifyATMID(atm_id)) {
                LOGGER.error("atm_id {} not validated", atm_id);
                response.setStatus(403);
                resultJson.put("error", "ATM ID not found");
                pr.write(resultJson.toString());
                pr.flush();
                return;
            }
            LOGGER.info("atm_id validated, processing request");

            String customerName= DatabaseClient.getCustomerFullNameByAccountNumber(accountNumber);
            LOGGER.info("Got name: {} for {}", customerName, accountNumber);
            resultJson.put("result", customerName);
            pr.write(resultJson.toString());
            pr.flush();
        } catch (ConnectionFailedException | CustomerNotFoundException e) {
            LOGGER.error("Something went wrong during processing: {}", e.getMessage(), e);
            response.setStatus(202);
            resultJson.put("error", e.getMessage());
            pr.write(resultJson.toString());
            pr.flush();
        } catch (InvalidParameterException e) {
            LOGGER.error("Invalid parameters: {}", e.getMessage(), e);
            response.setStatus(400);
            resultJson.put("error", e.getMessage());
            pr.write(resultJson.toString());
            pr.flush();
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("GET from ADDR: {}", request.getRemoteAddr());
    }
}
