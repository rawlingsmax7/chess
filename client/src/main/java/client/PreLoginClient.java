package client;

import requests.LoginRequest;
import requests.RegisterRequest;

import java.util.Arrays;
import java.util.Scanner;

public class PreLoginClient {

    private final ServerFacade facade;

    PreLoginClient(ServerFacade facade) {
        this.facade = facade;
    }

    public void run() {
        System.out.println("Welcome to Chess. Type \"help\" to get started.");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            System.out.print("[LOGGED OUT] >>> ");
            String line = scanner.nextLine();

            try {
                result = evaluate(line);
                System.out.println(result);
            } catch (Throwable exception) {
                var message = exception.getMessage();
                System.out.println(message);
            }
        }
        System.out.println();
    }

    private String evaluate(String input) {
        try {
            var tokens = input.split(" ");
            var command = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            // depending on the command do a different thing
            switch (command) {
                case "quit":
                    return "quit";
                case "login":
                    // params is username, password
                    if (params.length < 2) {
                        return "Expected: login <USERNAME> <PASSWORD>";
                    } else {
                        LoginRequest loginRequest = new LoginRequest(params[0], params[1]);
                        facade.login(loginRequest);
                        System.out.print("Login success!");
                        new PostLoginClient(facade).run();
                        return "";
                    }
                case "register":
                    // params is username, password, email
                    if (params.length < 3) {
                        return "Expected: register <USERNAME> <PASSWORD> <EMAIL>";
                    } else {
                        RegisterRequest registerRequest = new RegisterRequest(params[0], params[1],
                                params[2]);
                        facade.register(registerRequest);
                        System.out.print("Register success!");
                        new PostLoginClient(facade).run();
                        return "";
                    }
                default:
                    return printHelp();
            }
        } catch (ResponseException exception) {
            return switch (exception.getStatusCode()) {
                case 400 -> "Bad request, please check your input.";
                case 401 -> "Invalid username or password.";
                case 403 -> "That username is already taken.";
                default -> "Something went wrong. Please try again.";
            };
        }
    }

    private String printHelp() {
        return """
                Type one of the following options:\s
                "help"  ---> Display actions you can do.
                "quit"  ---> Exits the program.
                "login <USERNAME> <PASSWORD>" ---> Login to the chess server. Expects username\
                 followed by the password
                "register <USERNAME> <PASSWORD> <EMAIL>" ---> Register a new user to the chess\
                 server. Expects username, password, and email.
                """;
    }
}
