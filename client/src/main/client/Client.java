package client;

import serverFacade.ServerFacade;

import java.util.*;

enum ClientContext {
    PRELOGIN,
    POSTLOGIN
}
public class Client {

    ServerFacade serverFacade = new ServerFacade("localhost", "8080");
    private final Map<String, Command> commands = new HashMap<>();
    private boolean clientQuit = false;

    public static void main(String[] args) { new Client().run(); }

    private ClientContext clientContext = ClientContext.PRELOGIN;
    private void run() {
        // Add commands
        var helpCommand = new Command("help - with possible commands") {
            public void run(String[] args) {
                for (Map.Entry<String, Command> entry : commands.entrySet()) {
                    System.out.println("\t" + entry.getValue().getHelpText());
                }
            }
        };
        helpCommand.addValidStatus(ClientContext.POSTLOGIN);
        helpCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("help", helpCommand);

        var quitCommand = new Command("quit - playing chess") {
            public void run(String[] args) {
                clientQuit = true;
            }
        };
        quitCommand.addValidStatus(ClientContext.POSTLOGIN);
        quitCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("quit", quitCommand);

        var registerCommand = new Command("register <USERNAME> <PASSWORD> <EMAIL> - to create account") {
            public void run(String[] args) {

            }
        };
        registerCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("register", registerCommand);

        var loginCommand = new Command("login <USERNAME> <PASSWORD> - to play chess") {
            public void run(String[] args) {

            }
        };
        loginCommand.addValidStatus(ClientContext.PRELOGIN);
        commands.put("login", loginCommand);

        var createGameCommand = new Command("create <NAME> - a chess game") {
            public void run(String[] args) {

            }
        };
        createGameCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("create", createGameCommand);

        var listGamesCommand = new Command("list - games") {
            public void run(String[] args) {

            }
        };
        listGamesCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("list", listGamesCommand);

        var joinGameCommand = new Command("join [WHITE|BLACK|<empty>] - a game") {
            public void run(String[] args) {

            }
        };
        joinGameCommand.addValidStatus(ClientContext.POSTLOGIN);
        commands.put("join", joinGameCommand);

        runRepl();

//        var testRegisterRequest = new RegisterRequest("john", "abc123", "abc@gmail.com");
//        var result = serverFacade.register(testRegisterRequest);
//
//        var createGameRequest = new CreateGameRequest("testGame");
//        var createGameResult = serverFacade.createGame(createGameRequest, result.authToken());
//
//        var createGameRequest2 = new CreateGameRequest("testGame2");
//        var createGameResult2 = serverFacade.createGame(createGameRequest2, result.authToken());
//
//        var listGamesResult = serverFacade.listGames(result.authToken());
    }

    private void runRepl() {
        System.out.println("Welcome to the chess game! Type 'help' to list available commands.");
        while (!clientQuit)  {
            System.out.print("[" + clientContext.toString() + "] >>> ");
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            var args = line.split(" ");
            var commandText = args[0].toLowerCase();
            var commandArgs = Arrays.copyOfRange(args, 1, args.length);
            var command = commands.get(commandText);

            if (command != null) {
                command.run(commandArgs);
            } else {
                System.out.println("Invalid command");
            }
        }
    }
}
