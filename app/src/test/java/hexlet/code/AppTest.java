package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    private Javalin app;
    private DataSource dataSource;
    private UrlRepository urlRepository;

    @BeforeEach
    void setUp() {
        dataSource = DatabaseConfig.getDataSource();
        app = App.getApp(dataSource);
        urlRepository = new UrlRepository(dataSource);

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
}
