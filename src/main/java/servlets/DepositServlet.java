package servlets;

import database_client.DatabaseClient;
import org.json.JSONException;
import org.json.JSONObject;
import utils.RequestReader;
import utils.exceptions.AccountNotFoundException;
import utils.exceptions.ConnectionFailedException;

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
            } catch (JSONException ex) {
                response.setStatus(400);
                pr.flush();
            } catch (AccountNotFoundException | ConnectionFailedException e) {
                response.setStatus(400);
                pr.write(e.getMessage());
                pr.flush();
                e.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }
}
