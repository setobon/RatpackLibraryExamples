import ratpack.error.ServerErrorHandler;
import ratpack.server.RatpackServer;
import ratpack.server.ServerConfig;

import java.net.URI;

public class Main {
    public static void main(String... args) throws Exception {
        ratpackQuickStartExample();
        ratpackServerLaunchingExample();
    }


    private static void ratpackServerLaunchingExample() throws Exception {
        RatpackServer.start(server -> server
                .serverConfig(ServerConfig.embedded().publicAddress(new URI("http://company.org")))
                .registryOf(registry -> registry.add("World!"))
                .handlers(chain -> chain
                        .get(ctx -> ctx.render("Hello " + ctx.get(String.class)))
                        .get(":name", ctx -> ctx.render("Hello " + ctx.getPathTokens().get("name") + "!"))
                )
        );
    }

    private static void ratpackQuickStartExample() throws Exception {
        RatpackServer.start(server -> server
                .handlers(chain -> chain
                        .get(ctx -> ctx.render("Hello World!"))
                        .get(":name", ctx -> ctx.render("Hello " + ctx.getPathTokens().get("name") + "!"))
                )
        );
    }
}