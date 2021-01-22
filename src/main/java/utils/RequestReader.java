package utils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public interface RequestReader {
    static String getRequestData(HttpServletRequest request) throws IOException {
        BufferedReader requestReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        StringBuilder requestData = new StringBuilder();
        String line;
        while ((line = requestReader.readLine()) != null) {
            requestData.append(line);
        }
        return requestData.toString();
    }
}
