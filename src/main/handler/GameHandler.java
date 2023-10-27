package handler;

import com.google.gson.Gson;
import service.GameService;
import spark.Request;
import spark.Response;
import webRequest.CreateGameRequest;
import webRequest.JoinGameRequest;
import webRequest.RegisterRequest;

public class GameHandler {

    private static final GameHandler gameHandler = new GameHandler();

    public static GameHandler getInstance() {
        return gameHandler;
    }

    public Object handleListGames(Request request, Response response) {
        var authToken = request.headers("authorization");
        var listGamesResponse = GameService.getInstance().listGames(authToken);
        if (listGamesResponse.message() != null && listGamesResponse.message().equals("Error: unauthorized"))
            response.status(401);
        return new Gson().toJson(listGamesResponse);
    }

    public Object handleCreateGame(Request request, Response response) {
        var authToken = request.headers("authorization");
        var createGameRequest = new Gson().fromJson(request.body(), CreateGameRequest.class);
        var createGameResult = GameService.getInstance().createGame(createGameRequest, authToken);
        if (createGameResult.message() != null && createGameResult.message().equals("Error: unauthorized"))
            response.status(401);
        return new Gson().toJson(createGameResult);
    }

    public Object handleJoinGame(Request request, Response response) {
        var authToken = request.headers("authorization");
        var joinGameRequest = new Gson().fromJson(request.body(), JoinGameRequest.class);
        var joinGameResult = GameService.getInstance().joinGame(joinGameRequest, authToken);
        if (joinGameResult.message() != null) {
            if (joinGameResult.message().equals("Error: bad request"))
                response.status(400);
            else if (joinGameResult.message().equals("Error: unauthorized"))
                response.status(401);
            else if (joinGameResult.message().equals("Error: already taken"))
                response.status(403);
        }
        return new Gson().toJson(joinGameResult);
    }
}
