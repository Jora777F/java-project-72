package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.UrlCheckController;
import hexlet.code.controller.UrlsController;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    public static final String DEFAULT_PORT = "7070";

    public static void main(String[] args) throws Exception {
        DataSource dataSource = DatabaseConfig.getDataSource();
        initDatabase(dataSource);

        Javalin app = getApp(dataSource);
        app.start(getPort());
        LOGGER.info("Application started successfully.");
    }

    public static Javalin getApp(DataSource dataSource) {
        var templateEngine = createTemplateEngine();
        var urlRepository = new UrlRepository(dataSource);
        var urlCheckRepository = new UrlCheckRepository(dataSource);

        var app = Javalin.create(javalinConfig -> {
            javalinConfig.bundledPlugins.enableDevLogging();
            javalinConfig.fileRenderer(new JavalinJte(templateEngine));
        });

        app.get(NamedRoutes.rootPath(), ctx -> ctx.render("index.jte"));
        app.get(NamedRoutes.urlsPath(), ctx -> UrlsController.index(ctx, urlRepository, urlCheckRepository));
        app.get(NamedRoutes.urlPath("{id}"), ctx -> UrlsController.show(ctx, urlRepository, urlCheckRepository));
        app.post(NamedRoutes.urlsPath(), ctx -> UrlsController.create(ctx, urlRepository));
        app.post(NamedRoutes.urlChecksPath("{id}"),
                ctx -> UrlCheckController.create(ctx, urlRepository, urlCheckRepository));
        return app;
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        LOGGER.debug("Starting server on port: {}", port);
        return Integer.parseInt(port);
    }

    private static void initDatabase(DataSource dataSource) throws SQLException {
        LOGGER.info("Initializing database schema...");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {

            var inputStream = App.class.getClassLoader()
                    .getResourceAsStream("schema.sql");

            if (inputStream == null) {
                throw new RuntimeException("schema.sql not found");
            }

            var sql = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            statement.execute(sql);
            LOGGER.info("Database schema initialized successfully");
        }
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }
}
