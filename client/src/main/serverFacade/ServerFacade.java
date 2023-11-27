package serverFacade;

import com.google.gson.Gson;
import webRequest.*;
import webResult.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class ServerFacade {
    private String address;
    private String port;
    private int responseCode;

    public ServerFacade(String address, String port) {
        this.address = address;
        this.port = port;
    }

    public int getStatusCode() { return responseCode; }

    public RegisterResult register(RegisterRequest request) {
        try {
            var http = createURLConnection("/user", "POST", request, null);

            http.connect();
            responseCode = http.getResponseCode();

            if (responseCode == 200) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                    return new Gson().fromJson(inputStreamReader, RegisterResult.class);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public LoginResult login(LoginRequest request) {
        try {
            var http = createURLConnection("/session", "POST", request, null);

            http.connect();
            responseCode = http.getResponseCode();

            if (responseCode == 200) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                    return new Gson().fromJson(inputStreamReader, LoginResult.class);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public LogoutResult logout(LogoutRequest request) {
        try {
            var http = createURLConnection("/session", "DELETE", null, request.authToken());

            http.connect();
            responseCode = http.getResponseCode();

            if (responseCode == 200) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                    return new Gson().fromJson(inputStreamReader, LogoutResult.class);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public ListGamesResult listGames(String authToken) {
        try {
            var http = createURLConnection("/game", "GET", null, authToken);

            http.connect();
            responseCode = http.getResponseCode();

            if (responseCode == 200) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                    return new Gson().fromJson(inputStreamReader, ListGamesResult.class);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) {
        try {
            var http = createURLConnection("/game", "POST", request, authToken);

            http.connect();
            responseCode = http.getResponseCode();

            if (responseCode == 200) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                    return new Gson().fromJson(inputStreamReader, CreateGameResult.class);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public JoinGameResult joinGame(JoinGameRequest request, String authToken) {
        try {
            var http = createURLConnection("/game", "PUT", request, authToken);

            http.connect();
            responseCode = http.getResponseCode();

            if (responseCode == 200) {
                try (InputStream respBody = http.getInputStream()) {
                    InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                    return new Gson().fromJson(inputStreamReader, JoinGameResult.class);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private <T> HttpURLConnection createURLConnection(String postfix, String requestMethod,
                                                      T request, String authToken) throws Exception {
        URI uri = new URI("http://" + address + ":" + port + postfix);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod(requestMethod);

        if (authToken != null)
            http.setRequestProperty("authorization", authToken);

        if (request != null) {
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");
            try (var outputStream = http.getOutputStream()) {
                var jsonBody = new Gson().toJson(request);
                outputStream.write(jsonBody.getBytes());
            }
        }

        return http;
    }

}
