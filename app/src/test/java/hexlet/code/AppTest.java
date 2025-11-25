package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    private Javalin app;
    private DataSource dataSource;
    private UrlRepository urlRepository;
    private UrlCheckRepository urlCheckRepository;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        dataSource = DatabaseConfig.getDataSource();
        app = App.getApp(dataSource);
        urlRepository = new UrlRepository(dataSource);
        urlCheckRepository = new UrlCheckRepository(dataSource);

        // Initialize database
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            var sql = new String(
                    AppTest.class.getClassLoader()
                            .getResourceAsStream("schema.sql")
                            .readAllBytes()
            );
            statement.execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Start the server
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @DisplayName(value = "Should redirect on main page, when create invalid url.")
    @Test
    void shouldRedirectOnMainPageWhenTestCreateInvalidUrl() {
        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=invalid-url";
            var response = client.post(NamedRoutes.urlsPath(), requestBody);

            // Должен редиректить на главную с ошибкой
            assertThat(response.code()).isEqualTo(200);
        });
    }

    @DisplayName(value = "Should return status code 200, when create url.")
    @Test
    void shouldReturnOkWhenTestCreateValidUrl() {
        JavalinTest.test(app, (server, client) -> {
            String testUrl = "https://example.com";
            var requestBody = "url=" + testUrl;

            var response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);

            // Проверяем, что URL добавлен в БД
            var urls = urlRepository.getEntities();
            assertThat(urls).hasSize(1);
            assertThat(urls.getFirst().getName()).isEqualTo(testUrl);

            // Проверяем, что URL отображается на странице /urls
            var urlsPageResponse = client.get(NamedRoutes.urlsPath());
            assertThat(urlsPageResponse.body().string()).contains(testUrl);
        });
    }

    @DisplayName(value = "Should create one record in the database when we create a duplicate url.")
    @Test
    void shouldCreateOneRecordInDatabaseWhenCreateDuplicateUrl() {
        JavalinTest.test(app, (server, client) -> {
            String testUrl = "https://example.com";

            // Добавляем URL первый раз
            client.post(NamedRoutes.urlsPath(), "url=" + testUrl);

            // Пытаемся добавить второй раз
            var response = client.post(NamedRoutes.urlsPath(), "url=" + testUrl);
            assertThat(response.code()).isEqualTo(200);

            // Проверяем, что в БД только один URL
            var urls = urlRepository.getEntities();
            assertThat(urls).hasSize(1);
        });
    }

    @DisplayName(value = "Should return status code 404, when show non-existent url.")
    @Test
    void shouldReturnStatus404WhenShowNonExistentUrl() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath(999L));
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void shouldCheckContentOfResponseWhenTestMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("<form");
        });
    }

    @Test
    void shouldCheckContentOfResponseWhenTestUrlsPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("<table");
        });
    }

    @DisplayName(value = "Should return status code 200, when test show url.")
    @Test
    void shouldReturnStatusCode200WhenTestShowUrl() {
        JavalinTest.test(app, (server, client) -> {
            // Создаем URL
            String testUrl = "https://example.com";
            Url url = new Url(testUrl, new java.sql.Timestamp(System.currentTimeMillis()));
            urlRepository.save(url);

            // Проверяем страницу URL
            var response = client.get(NamedRoutes.urlPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains(testUrl);
        });
    }

    @DisplayName(value = "Should return status code 200 when create check url with mocking response.")
    @Test
    void shouldReturnStatusOkWhenCreateUrlCheckWithMockResponse() {
        // Мокируем ответ от сайта
        String mockHtml = "<html><head><title>Test Page</title></head>"
                + "<body><h1>Test Header</h1>"
                + "<meta name=\"description\" content=\"Test Description\"></body></html>";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockHtml));

        JavalinTest.test(app, (server, client) -> {
            // Создаем URL с адресом MockWebServer
            String mockUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
            Url url = new Url(mockUrl, new Timestamp(System.currentTimeMillis()));
            urlRepository.save(url);

            // Запускаем проверку
            Response response = client.post(NamedRoutes.urlChecksPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);

            // Проверяем, что проверка добавлена в БД
            List<UrlCheck> checks = urlCheckRepository.getEntitiesByUrlId(url.getId());
            assertThat(checks).hasSize(1);

            UrlCheck check = checks.getFirst();
            assertThat(check.getStatusCode()).isEqualTo(200);
            assertThat(check.getTitle()).isEqualTo("Test Page");
            assertThat(check.getH1()).isEqualTo("Test Header");
            assertThat(check.getDescription()).isEqualTo("Test Description");
        });
    }

    @DisplayName(value = "Should return status code 200 when test urls page shows last check.")
    @Test
    void shouldReturnStatusCodeOkWhenTestUrlsPageShowsLastCheck() {
        String mockHtml = "<html><head><title>Test</title></head><body></body></html>";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(mockHtml));

        JavalinTest.test(app, (server, client) -> {
            // Создаем URL
            String mockUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
            Url url = new Url(mockUrl, new Timestamp(System.currentTimeMillis()));
            urlRepository.save(url);

            // Запускаем проверку
            client.post(NamedRoutes.urlChecksPath(url.getId()));

            // Проверяем, что на странице /urls отображается статус код
            Response response = client.get(NamedRoutes.urlsPath());
            assertThat(response.body().string())
                    .contains(mockUrl)
                    .contains("200");
        });
    }
}
