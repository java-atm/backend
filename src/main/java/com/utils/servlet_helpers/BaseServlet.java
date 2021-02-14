package com.utils.servlet_helpers;

import com.database_client.DatabaseClient;
import com.utils.exceptions.db_exceptions.BaseDatabaseClientException;
import com.utils.exceptions.readers_exceptions.JSONParsingFailedException;
import com.utils.exceptions.servlet_exceptions.InvalidParameterException;
import com.utils.exceptions.servlet_exceptions.ResponseAlreadySentException;
import com.utils.readers.ParameterGetter;
import com.utils.readers.RequestReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;


public abstract class BaseServlet extends HttpServlet{
    private final Logger LOGGER = LogManager.getLogger(BaseServlet.class);

    protected abstract JSONObject performAction(JSONObject jsonObject) throws BaseDatabaseClientException, InvalidParameterException;

    final protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("Processing POST in '{}' from remote address: '{}'", this.getClass(), request.getRemoteAddr());
        try (PrintWriter pr = response.getWriter()) {
            LOGGER.info("Parsing JSON");
            JSONObject payload = parseJSONFromRequest(request, response, pr);
            JSONObject resultJSON = servletAction(response, pr, payload);
            pr.print(resultJSON);
            pr.flush();
        } catch (ResponseAlreadySentException e) {
            LOGGER.error("Response has been already processed: {}", e.getMessage(), e);
        } catch (Throwable e) {
            LOGGER.error("Something went wrong: {}", e.getMessage(), e);
            response.setStatus(500);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(200);
        LOGGER.info("Processing GET in '{}' from remote address: '{}'", this.getClass(), request.getRemoteAddr());
    }

    protected JSONObject servletAction(HttpServletResponse response, PrintWriter pr, JSONObject jsonObject) throws ResponseAlreadySentException {
        JSONObject resultJson = new JSONObject();
        try {
            validateATMID(response, jsonObject, pr);
            return performAction(jsonObject);
        } catch (BaseDatabaseClientException e) {
            LOGGER.error("Something went wrong during processing: {}", e.getMessage(), e);
            response.setStatus(202);
            resultJson.put("error", e.getMessage());
            pr.print(resultJson);
            pr.flush();
            throw new ResponseAlreadySentException("Database throw an error: " + e.getMessage());
        } catch (InvalidParameterException e) {
            LOGGER.error("Invalid parameters: {}", e.getMessage(), e);
            response.setStatus(400);
            resultJson.put("error", e.getMessage());
            pr.print(resultJson);
            pr.flush();
            throw new ResponseAlreadySentException("Invalid parameters: " + e.getMessage());
        }
    }

    final public JSONObject parseJSONFromRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter pr) throws ResponseAlreadySentException {
        JSONObject resultJson = new JSONObject();
        JSONObject jsonObject;
        try {
            jsonObject = RequestReader.getJSONData(request);
        } catch (JSONParsingFailedException e) {
            response.setStatus(400);
            resultJson.put("error", "Invalid JSON content");
            pr.print(resultJson);
            pr.flush();
            throw new ResponseAlreadySentException("Invalid JSON content");
        }
        LOGGER.info("JSON parsed");
        return jsonObject;
    }

    final public void validateATMID(HttpServletResponse response, JSONObject payload, PrintWriter pr) throws ResponseAlreadySentException, InvalidParameterException, BaseDatabaseClientException{
        LOGGER.info("Verifying ATM ID");
        String atm_id = ParameterGetter.getATM_ID(payload, "atm_id");
        if (!DatabaseClient.verifyATMID(atm_id)) {
            JSONObject resultJSON = new JSONObject();
            LOGGER.error("atm_id {} not validated", atm_id);
            response.setStatus(403);
            resultJSON.put("error", "ATM ID not found");
            pr.print(resultJSON);
            pr.flush();
            throw new ResponseAlreadySentException("ATM ID is not found");
        }
        LOGGER.info("ATM ID verified.");
    }
}
