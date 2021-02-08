package com.servlets;


import com.database_client.DatabaseClient;
import org.json.JSONException;
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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter pr = response.getWriter()) {
            JSONObject jsonObject = new JSONObject(RequestReader.getRequestData(request));
            try {
                String atm_id = jsonObject.get("atm_id").toString();
                if (! DatabaseClient.verifyATMID(atm_id)) {
                    response.setStatus(400);
                    pr.write("ATM ID not found");
                    pr.flush();
                    return;
                }
                String cardNumber = jsonObject.getJSONObject("card_info").get("CARD_NUMBER").toString();
                String pin = jsonObject.get("pin").toString();
                String customerID = DatabaseClient.getCustomerIDByCardID(cardNumber, pin);
                pr.print(customerID);
                pr.flush();
            } catch (CustomerNotFoundException | JSONException | ConnectionFailedException e) {
                response.setStatus(400);
                pr.write(e.getMessage());
                e.printStackTrace();
                pr.flush();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }
}
