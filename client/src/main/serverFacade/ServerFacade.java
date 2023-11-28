package serverFacade;

import chess.ChessGameImpl;
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
    private static Gson gameAdapter = ChessGameImpl.getGsonAdapter();

    public ServerFacade(String address, String port) {
        this.address = address;
        this.port = port;
    }

    public int getStatusCode() { return responseCode; }

    public ClearResult clear() {
        try {
            var http = createURLConnection("/db", "DELETE", null, null);
            http.connect();
            return readResponse(http, ClearResult.class);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public RegisterResult register(RegisterRequest request) {
        try {
            var http = createURLConnection("/user", "POST", request, null);
            http.connect();
            return readResponse(http, RegisterResult.class);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public LoginResult login(LoginRequest request) {
        try {
            var http = createURLConnection("/session", "POST", request, null);
            http.connect();
            return readResponse(http, LoginResult.class);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public LogoutResult logout(LogoutRequest request) {
        try {
            var http = createURLConnection("/session", "DELETE", null, request.authToken());
            http.connect();
            return readResponse(http, LogoutResult.class);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public ListGamesResult listGames(String authToken) {
        try {
            var http = createURLConnection("/game", "GET", null, authToken);
            http.connect();
            return readResponse(http, ListGamesResult.class);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public CreateGameResult createGame(CreateGameRequest request, String authToken) {
        try {
            var http = createURLConnection("/game", "POST", request, authToken);
            http.connect();
            return readResponse(http, CreateGameResult.class);
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
                    return gameAdapter.fromJson(inputStreamReader, JoinGameResult.class);
                }
            } else {
                try (InputStream respBody = http.getErrorStream()) {
                    InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                    return gameAdapter.fromJson(inputStreamReader, JoinGameResult.class);
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

    private <T> T readResponse(HttpURLConnection http, Class<T> resultClass) throws Exception {
        responseCode = http.getResponseCode();

        if (responseCode == 200) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                return new Gson().fromJson(inputStreamReader, resultClass);
            }
        } else {
            try (InputStream respBody = http.getErrorStream()) {
                InputStreamReader inputStreamReader = new InputStreamReader(respBody);
                return new Gson().fromJson(inputStreamReader, resultClass);
            }
        }
    }

}
