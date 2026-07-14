package server;

import dataaccess.MemoryAuthTokenDao;
import dataaccess.MemoryGameDao;
import dataaccess.MemoryUserDao;
import io.javalin.Javalin;
import service.ClearService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // create the database, DAOs
        MemoryUserDao userDao = new MemoryUserDao();
        MemoryGameDao gameDao = new MemoryGameDao();
        MemoryAuthTokenDao authDao = new MemoryAuthTokenDao();

        ClearService clearService = new ClearService(userDao, gameDao, authDao);

        javalin.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200).result("{}");
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
