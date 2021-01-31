package servlets;


import database_client.DatabaseClient;
import org.json.JSONException;
import org.json.JSONObject;
import utils.RequestReader;
import utils.exceptions.ConnectionFailedException;
import utils.exceptions.CustomerNotFoundException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;


@WebServlet(name = "GetAccountsServlet", urlPatterns = "/getAccountsByCustomerID")
public class GetAccountsServlet extends HttpServlet {
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
                boolean includeBalances = true;
                try {
                    includeBalances = jsonObject.getBoolean("includeBalances");
                } catch (JSONException e) {
                    System.out.println("Failed to get includeBalances param, using default true: " + e.getMessage());
                }
                JSONObject result_json = new JSONObject();
                if (includeBalances) {
                    HashMap<String, BigDecimal> result = DatabaseClient.getCustomerBalances(customerID);
                    result_json = new JSONObject(result);

                } else {
                    ArrayList<String> result = DatabaseClient.getCustomerAccounts(customerID);
                    result_json.put("accountNumbers", result);
                }
                pr.print(result_json.toString());
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
