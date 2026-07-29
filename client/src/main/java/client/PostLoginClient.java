package client;

import model.GameData;
import requests.CreateRequest;
import requests.JoinRequest;
import requests.ListResult;

import java.util.ArrayList;
import java.util.Arrays;

public class PostLoginClient extends LoginClient {

    private ArrayList<GameData> listedGames = new ArrayList<>();

    PostLoginClient(ServerFacade facade) {
        super(facade);
    }

    @Override
    protected String prompt() {
        return "[LOGGED IN] >>> ";
    }

    @Override
    protected String evaluate(String input) {
        try {
            var tokens = input.split(" ");
            var command = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            // depending on the command do a different thing
            switch (command) {
                case "logout":
                    facade.logout();
                    System.out.println("Logout success!");
                    return "quit";
                case "create":
                    if (params.length < 1) {
                        return "Expected: create <NAME>";
                    } else {
                        // to allow multi word names for games join the params by spaces
                        CreateRequest createRequest = new CreateRequest(String.join(" ", params));
                        facade.createGame(createRequest);
                        System.out.print("Create game success!");
                        return "";
                    }
                case "list":
                    ListResult result = facade.listGames();
                    listedGames = new ArrayList<>(result.games());
                    for (int i = 0; i < listedGames.size(); i++) {
                        GameData game = listedGames.get(i);
                        String whitePlayer = (game.whiteUsername() == null) ? "open" :
                                game.whiteUsername();
                        String blackPlayer = (game.blackUsername() == null) ? "open" :
                                game.blackUsername();
                        System.out.printf("%d. %s  (white: %s, black: %s)%n", i + 1, game.gameName(), whitePlayer, blackPlayer);
                    }
                    return "";
                case "join":
                    if (params.length < 2) {
                        return "Expected: join <WHITE|BLACK> <NUMBER>";
                    } else {
                        int inputNumber;
                        try {
                            inputNumber = Integer.parseInt(params[1]);
                        } catch (NumberFormatException e) {
                            return "Game number must be a number. Please run \"list\".";
                        }
                        if (inputNumber < 1 || inputNumber > listedGames.size()) {
                            return "No game with that number. Please run \"list\".";
                        }
                        // the actual gameID is from the game, but need to access it via inputNumber
                        int gameID = listedGames.get(inputNumber - 1).gameID();
                        JoinRequest joinRequest = new JoinRequest(params[0].toUpperCase(), gameID);
                        facade.joinGame(joinRequest);
                        System.out.print("Join game success!");

                        boolean whitePerspective = params[0].equalsIgnoreCase("WHITE");
                        GameData game = listedGames.get(inputNumber - 1);

                        new GameplayClient(facade, whitePerspective, game).run();
                        return "";
                    }
                case "observe":
                    if (params.length < 1) {
                        return "Expected: observe <NUMBER>";
                    } else {
                        int inputNumber;
                        try {
                            inputNumber = Integer.parseInt(params[0]);
                        } catch (NumberFormatException e) {
                            return "Game number must be a number. Please run \"list\".";
                        }
                        if (inputNumber < 1 || inputNumber > listedGames.size()) {
                            return "No game with that number. Please run \"list\".";
                        }

                        GameData game = listedGames.get(inputNumber - 1);
                        // just observe from white perspective
                        new GameplayClient(facade, true, game).run();
                        return "";
                    }
                default:
                    return printHelp();
            }
        } catch (ResponseException exception) {
            return switch (exception.getStatusCode()) {
                case 400 -> "Bad request, please check your input.";
                case 401 -> "Authorization failed.";
                case 403 -> "That color is already taken.";
                default -> "Something went wrong. Please try again.";
            };
        }
    }

    private String printHelp() {
        return """
                Type one of the following options:\s
                "help"  ---> Display actions you can do.
                "logout"  ---> Logs the user out.
                "create <NAME>" ---> Create a new chess game with the given input.
                "list" ---> Numbers a list of all the games that are currently in session.
                "join <WHITE|BLACK> <NUMBER>" ---> join a current game from the given perspective
                "observe <NUMBER>" ---> observe a current game from the given perspective
                """;
    }
}
