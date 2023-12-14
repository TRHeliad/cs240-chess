package server;

import com.google.gson.Gson;
import dataAccess.MemoryDataAccess;
import dataAccess.SQLDataAccess;
import handler.ApplicationHandler;
import handler.GameHandler;
import handler.UserHandler;
import server.websocket.WebSocketHandler;
import service.ApplicationService;
import service.GameService;
import service.UserService;
import spark.*;

import java.util.Map;

public class Server {
    private WebSocketHandler webSocketHandler;
    public static void main(String[] args) {
        new Server().run();
    }

    private void run() {
        var dataAccess = new SQLDataAccess();
        // init data
        UserService.getInstance().init(dataAccess);
        GameService.getInstance().init(dataAccess);
        webSocketHandler = new WebSocketHandler(dataAccess);
        ApplicationService.getInstance().init(dataAccess, webSocketHandler);

        // Setup spark server
        Spark.port(8080);

        Spark.externalStaticFileLocation("web");

        Spark.webSocket("/connect", webSocketHandler);

        Spark.delete("/db", ApplicationHandler.getInstance()::handleClear);

        Spark.post("/user", UserHandler.getInstance()::handleRegister);
        Spark.post("/session", UserHandler.getInstance()::handleLogin);
        Spark.delete("/session", UserHandler.getInstance()::handleLogout);

        Spark.get("/game", GameHandler.getInstance()::handleListGames);
        Spark.post("/game", GameHandler.getInstance()::handleCreateGame);
        Spark.put("/game", GameHandler.getInstance()::handleJoinGame);

        Spark.notFound((req, res) -> {
            var msg = String.format("[%s] %s not found", req.requestMethod(), req.pathInfo());
            return errorHandler(new Exception(msg), req, res);
        });
    }

    public Object errorHandler(Exception e, Request req, Response res) {
        var body = new Gson().toJson(Map.of("message", String.format("Error: %s", e.getMessage()), "success", false));
        res.type("application/json");
        res.status(500);
        res.body(body);
        return body;
    }
}
