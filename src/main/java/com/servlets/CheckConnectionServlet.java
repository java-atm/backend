package com.servlets;


import com.utils.servlet_helpers.BaseServlet;
import org.json.JSONObject;

import javax.servlet.annotation.WebServlet;


@WebServlet(name = "CheckConnectionServlet", urlPatterns = "/checkConnection")
public class CheckConnectionServlet extends BaseServlet {
    @Override
    protected JSONObject performAction(JSONObject jsonObject) {
        JSONObject resultJSON = new JSONObject();
        resultJSON.put("result", "Connection OK");
        return resultJSON;
    }
}
