package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.Javalin;
import requests.*;
import service.ClearService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // create the database, DAOs
        MemoryUserDao userDao = new MemoryUserDao();
        MemoryGameDao gameDao = new MemoryGameDao();
        MemoryAuthTokenDao authDao = new MemoryAuthTokenDao();

        ClearService clearService = new ClearService(userDao, gameDao, authDao);
        UserService userService = new UserService(userDao, authDao);

        Gson gson = new Gson();

        // exception handlers
        javalin.exception(BadRequestException.class, (exception, ctx) -> ctx.status(400).result(gson.toJson(new ErrorResult(exception.getMessage()))));
        javalin.exception(UnauthorizedException.class, (exception, ctx) -> ctx.status(401).result(gson.toJson(new ErrorResult(exception.getMessage()))));
        javalin.exception(AlreadyTakenException.class, (exception, ctx) -> ctx.status(403).result(gson.toJson(new ErrorResult(exception.getMessage()))));
        javalin.exception(Exception.class, (exception, ctx) -> ctx.status(500).result(gson.toJson(new ErrorResult("Error: " + exception.getMessage()))));

        // clear endpoint
        javalin.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200).result("{}");
        });

        // register endpoint
        javalin.post("/user", ctx -> {
            // ctx.body() is the raw JSON the client sent
            RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
            RegisterResult result = userService.register(request);
            ctx.result(gson.toJson(result));
        });

        // login endpoint
        javalin.post("/session", ctx -> {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = userService.login(request);
            ctx.result(gson.toJson(result));
        });

        // logout endpoint
        javalin.delete("/session", ctx -> {
            // the authToken is in a header, not a body this time
            String authToken = ctx.header("authorization");
            LogoutRequest request = new LogoutRequest(authToken);
            LogoutResult result = userService.logout(request);
            ctx.result(gson.toJson(result));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
