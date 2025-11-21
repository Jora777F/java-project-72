package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseConfig {

    private static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static final String DRIVER_CLASS_NAME = "org.h2.Driver";

    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DEFAULT_JDBC_URL);
        config.setDriverClassName(DRIVER_CLASS_NAME);
        config.setUsername("bart");
        config.setPassword("51mp50n");

        return new HikariDataSource(config);
    }
}
