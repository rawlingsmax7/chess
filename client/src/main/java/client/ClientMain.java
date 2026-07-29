package client;

public class ClientMain {
    public static void main(String[] args) throws ResponseException {

        ServerFacade facade = new ServerFacade("http://localhost:8080");

        System.out.println("♕ 240 Chess Client:");
        new PreLoginClient(facade).run();

    }
}
