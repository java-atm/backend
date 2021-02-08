package com.servlets;


import com.database_client.DatabaseClient;
import org.json.JSONException;
import org.json.JSONObject;
import com.utils.readers.RequestReader;
import com.utils.exceptions.ConnectionFailedException;
import com.utils.exceptions.AccountNotFoundException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;


@WebServlet(name = "DepositServlet", urlPatterns = "/deposit")
public class DepositServlet extends HttpServlet {
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
                String accountNumber = jsonObject.get("accountNumber").toString();
                String currency = jsonObject.get("currency").toString();
                BigDecimal amount = new BigDecimal(jsonObject.get("amount").toString());
                if (amount.signum() == -1) {
                    response.setStatus(400);
                    pr.write("Negative deposit rejected.");
                    pr.flush();
                    return;
                }
                DatabaseClient.depositToAccount(accountNumber, amount, currency);
                pr.print("Success");
                pr.flush();
            } catch (JSONException | AccountNotFoundException | ConnectionFailedException e) {
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
