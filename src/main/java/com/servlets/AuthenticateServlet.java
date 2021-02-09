package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.InvalidParameterException;
import com.utils.exceptions.JSONParsingFailedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import com.utils.readers.RequestReader;
import com.utils.exceptions.ConnectionFailedException;
import com.utils.exceptions.CustomerNotFoundException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(name = "AuthenticateServlet", urlPatterns = "/auth")
public class AuthenticateServlet extends HttpServlet {
    private final Logger LOGGER = LogManager.getLogger(AuthenticateServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("Processing authentication from ADDR: {}", request.getRemoteAddr());
        JSONObject resultJson = new JSONObject();
        try (PrintWriter pr = response.getWriter()) {
            LOGGER.info("Parsing JSON");
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
            try {
                String atm_id = RequestReader.getATM_ID(jsonObject, "atm_id");
                JSONObject card_info = RequestReader.getCardInfo(jsonObject, "card_info");
                Long cardNumber = RequestReader.getCardNumber(card_info, "CARD_NUMBER");
                String pin = RequestReader.getPin(jsonObject, "pin");
                LOGGER.info("cardNumber, pin and atm_id received, processing");

                if (! DatabaseClient.verifyATMID(atm_id)) {
                    LOGGER.error("atm_id {} not validated", atm_id);
                    response.setStatus(403);
                    resultJson.put("error", "ATM ID not found");
                    pr.write(resultJson.toString());
                    pr.flush();
                    return;
                }
                LOGGER.info("atm_id validated, processing request");

                String customerID = DatabaseClient.getCustomerIDByCardID(cardNumber, pin);
                resultJson.put("result", customerID);
                pr.write(customerID);
                pr.flush();
                LOGGER.info("Got customerID: {} for {}", customerID, cardNumber);
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
        } catch (IOException e) {
            LOGGER.error("Something went wrong: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }
}
