package handler;

import com.google.gson.Gson;
import service.UserService;
import spark.*;
import webRequest.LoginRequest;
import webRequest.RegisterRequest;

public class UserHandler {

    private static final UserHandler userHandler = new UserHandler();

    public static UserHandler getInstance() {
        return userHandler;
    }

    public Object handleRegister(Request request, Response response) {
        var registerRequest = new Gson().fromJson(request.body(), RegisterRequest.class);
        var registerResponse = UserService.getInstance().register(registerRequest);
        if (registerResponse.message() != null) {
            if (registerResponse.message().equals("Error: bad request"))
                response.status(400);
            else if (registerResponse.message().equals("Error: already taken"))
                response.status(403);
        }
        return new Gson().toJson(registerResponse);
    }

    public Object handleLogin(Request request, Response response) {
        var loginRequest = new Gson().fromJson(request.body(), LoginRequest.class);
        var loginResponse = UserService.getInstance().login(loginRequest);
        if (loginResponse.message() != null && loginResponse.message().equals("Error: unauthorized"))
            response.status(401);
        return new Gson().toJson(loginResponse);
    }

    public Object handleLogout(Request request, Response response) {
        var authToken = request.headers("authorization");
        var logoutResponse = UserService.getInstance().logout(authToken);
        if (logoutResponse.message() != null && logoutResponse.message().equals("Error: unauthorized"))
            response.status(401);
        return new Gson().toJson(logoutResponse);
    }
}
