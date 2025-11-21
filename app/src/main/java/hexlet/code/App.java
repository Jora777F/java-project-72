package hexlet.code;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    public static final String DEFAULT_PORT = "7070";

    public static void main(String[] args) {
        DataSource dataSource = DatabaseConfig.getDataSource();
        Javalin app = getApp(dataSource);
        app.start(getPort());
        LOGGER.info("Application configured successfully");
    }

    public static Javalin getApp(DataSource dataSource) {
        return Javalin.create()
                .get("/", ctx -> ctx.result("Hello World"));
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        LOGGER.debug("Using port: {}", port);
        return Integer.parseInt(port);
    }
}
