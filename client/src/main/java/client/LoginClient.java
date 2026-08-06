package client;

import java.util.Scanner;

public abstract class LoginClient {
    protected final ServerFacade facade;
    protected final Scanner scanner = new Scanner(System.in);

    protected LoginClient(ServerFacade facade) {
        this.facade = facade;
    }

    public void run() {
        var result = "";
        while (!result.equals("quit")) {
            System.out.print(prompt());
            String line = scanner.nextLine();
            try {
                result = evaluate(line);
                if (!result.equals("quit")) {
                    System.out.println(result);
                }
            } catch (Throwable exception) {
                System.out.println("Unexpected error, please try again.");
            }
        }
        System.out.println();
    }

    protected abstract String prompt();

    protected abstract String evaluate(String input);

}
