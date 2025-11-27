package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
public class DatabaseConfig {

    private static final String H2_JDBC_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = System.getenv("JDBC_DATABASE_URL");

        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            log.info("Using H2 in-memory database for local development");
            config.setJdbcUrl(H2_JDBC_URL);
            config.setDriverClassName(H2_DRIVER);
            config.setUsername("bart");
            config.setPassword("51mp50n");
        } else {
            log.info("Using PostgreSQL database for production");
            config.setJdbcUrl(jdbcUrl);
            config.setDriverClassName(POSTGRES_DRIVER);
        }

        HikariDataSource dataSource = new HikariDataSource(config);

        // Проверка подключения
        try (var connection = dataSource.getConnection()) {
            log.info("Database connection successful: {}",
                    connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            log.error("Failed to connect to database", e);
            throw new RuntimeException("Database connection failed", e);
        }

        return dataSource;
    }
}
