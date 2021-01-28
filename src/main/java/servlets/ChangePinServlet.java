package servlets;


import database_client.DatabaseClient;
import org.json.JSONException;
import org.json.JSONObject;
import utils.RequestReader;
import utils.exceptions.ConnectionFailedException;
import utils.exceptions.PinChangeFailedException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@WebServlet(name = "ChangePinServlet", urlPatterns = "/changePin")
public class ChangePinServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter pr = response.getWriter()) {
            JSONObject jsonObject = new JSONObject(RequestReader.getRequestData(request));
            try {
                String cardNumber = jsonObject.get("cardNumber").toString();
                String newPin = jsonObject.get("newPin").toString();

                DatabaseClient.changePin(cardNumber, newPin);

                pr.print("Success");
                pr.flush();
            } catch (JSONException | ConnectionFailedException | PinChangeFailedException ex) {
                pr.write(ex.getMessage());
                response.setStatus(400);
                pr.flush();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }
}
