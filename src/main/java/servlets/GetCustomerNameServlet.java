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


@WebServlet(name = "GetCustomerNameServlet", urlPatterns = "/getCustomerName")
public class GetCustomerNameServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter pr = response.getWriter()) {
            JSONObject jsonObject = new JSONObject(RequestReader.getRequestData(request));
            try {
                String accountNumber = jsonObject.get("accountNumber").toString();

                String customerName= DatabaseClient.getCustomerFullNameByAccountNumber(accountNumber);

                pr.print(customerName);
                pr.flush();
            } catch (JSONException | ConnectionFailedException | CustomerNotFoundException ex) {
                pr.write(ex.getMessage());
                response.setStatus(400);
                pr.flush();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }
}
