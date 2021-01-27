package servlets;

import database_client.DatabaseClient;
import org.json.JSONException;
import org.json.JSONObject;
import utils.RequestReader;
import utils.exceptions.ConnectionFailedException;
import utils.exceptions.CustomerNotFoundException;

import javax.servlet.ServletException;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (PrintWriter pr = response.getWriter()) {
            JSONObject jsonObject = new JSONObject(RequestReader.getRequestData(request));
            try {
                String customerID = jsonObject.get("customerID").toString();
                HashMap<String, BigDecimal> accounts = DatabaseClient.getCustomerBalances(customerID);
                jsonObject = new JSONObject(accounts);
                pr.print(jsonObject.toString());
                pr.flush();
            } catch (CustomerNotFoundException | JSONException | ConnectionFailedException ex) {
                response.setStatus(400);
                pr.flush();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
