package servlets;


import database_client.DatabaseClient;
import org.json.JSONException;
import org.json.JSONObject;
import utils.RequestReader;
import utils.exceptions.ConnectionFailedException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(name = "CheckConnectionServlet", urlPatterns = "/checkConnection")
public class CheckConnectionServlet extends HttpServlet {
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
                pr.print("Success");
                pr.flush();
            } catch (ConnectionFailedException | JSONException e) {
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
