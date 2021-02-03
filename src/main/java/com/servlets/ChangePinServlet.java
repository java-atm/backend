package com.servlets;


import com.database_client.DatabaseClient;
import com.utils.exceptions.NewPinTooLongException;
import org.json.JSONException;
import org.json.JSONObject;
import com.utils.readers.RequestReader;
import com.utils.exceptions.ConnectionFailedException;
import com.utils.exceptions.PinChangeFailedException;

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
                String atm_id = jsonObject.get("atm_id").toString();
                if (! DatabaseClient.verifyATMID(atm_id)) {
                    response.setStatus(400);
                    pr.write("ATM ID not found");
                    pr.flush();
                    return;
                }
                String cardNumber = jsonObject.get("cardNumber").toString();
                String newPin = jsonObject.get("newPin").toString();

                DatabaseClient.changePin(cardNumber, newPin);

                pr.print("Success");
                pr.flush();
            } catch (JSONException | ConnectionFailedException | PinChangeFailedException | NewPinTooLongException e) {
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
