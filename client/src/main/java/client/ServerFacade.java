package client;

import com.google.gson.Gson;
import requests.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;
    private String authToken;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(RegisterRequest request) throws ResponseException {
        RegisterResult result = makeRequest("POST", "/user", request, RegisterResult.class);
        this.authToken = result.authToken();

        return result;
    }

    public LoginResult login(LoginRequest request) throws ResponseException {
        LoginResult result = makeRequest("POST", "/session", request, LoginResult.class);
        this.authToken = result.authToken();

        return result;
    }

    public LogoutResult logout() throws ResponseException {
        // the Server builds own LogoutRequest from the header
        LogoutResult result = makeRequest("DELETE", "/session", null, LogoutResult.class);
        this.authToken = null;

        return result;
    }

    public void clear() throws ResponseException {
        makeRequest("DELETE", "/db", null, null);
    }

    public ListResult listGames() throws ResponseException {
        return makeRequest("GET", "/game", null, ListResult.class);
    }

    public CreateResult createGame(CreateRequest request) throws ResponseException {
        return makeRequest("POST", "/game", request, CreateResult.class);
    }

    public JoinResult joinGame(JoinRequest request) throws ResponseException {
        return makeRequest("PUT", "/game", request, JoinResult.class);
    }


    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        // path will be /user or like /session
        try {
            // create URL object
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            // method is the "POST", "GET", sort of sql format
            http.setRequestMethod(method);
            http.setDoOutput(true);

            // if there's an authToken then we need to add it as part of the request
            if (authToken != null) {
                http.addRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    // puts the JSON body into the request
    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        try (InputStream responseBody = http.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(responseBody);
            if (responseClass != null) {
                response = new Gson().fromJson(reader, responseClass);
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException(status, "failure: " + status);
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getServerUrl() {
        return serverUrl;
    }
}
