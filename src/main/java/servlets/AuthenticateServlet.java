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


@WebServlet(name = "AuthenticateServlet", urlPatterns = "/auth")
public class AuthenticateServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter pr = response.getWriter()) {
            JSONObject jsonObject = new JSONObject(RequestReader.getRequestData(request));
            try {
                String cardNumber = jsonObject.getJSONObject("card_info").get("CARD_NUMBER").toString();
                String pin = jsonObject.get("pin").toString();
                String customerID = DatabaseClient.getCustomerIDByCardID(cardNumber, pin);
                pr.print(customerID);
                pr.flush();
            } catch (CustomerNotFoundException | JSONException | ConnectionFailedException ex) {
                response.setStatus(400);
                pr.flush();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

//        PrintWriter pr = response.getWriter();
//        BufferedReader bufferedReader = new BufferedReader(
//                new InputStreamReader(request.getInputStream())
//        );
//        StringBuilder line = new StringBuilder();
//        String str;
//        while ((str = bufferedReader.readLine()) != null) {
//            System.out.println(line);
//            line.append(str);
//        }
//        System.out.println(line.toString());
//        JSONObject jsonObject = new JSONObject(line.toString());
//        try {
//            String customerID = DatabaseClient
//                    .getCustomerIDByCardID(
//                            jsonObject.getJSONObject("card_info").
//                                    get("CARD_NUMBER").toString(),
//                            jsonObject.get("pin").toString());
//            pr.print(customerID);
//            pr.flush();
//        } catch (CustomerNotFoundException | JSONException ex) {
//            response.setStatus(400);
//            pr.flush();
//        }
//
//        pr.close();
    }
}
