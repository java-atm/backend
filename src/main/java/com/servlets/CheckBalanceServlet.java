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
import java.math.BigDecimal;
import java.util.HashMap;


@WebServlet(name = "CheckBalanceServlet", urlPatterns = "/checkBalance")
public class CheckBalanceServlet extends HttpServlet {
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
                String customerID = jsonObject.get("customerID").toString();
                HashMap<String, BigDecimal> accounts = DatabaseClient.getCustomerBalances(customerID);
                jsonObject = new JSONObject(accounts);
                pr.print(jsonObject.toString());
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
