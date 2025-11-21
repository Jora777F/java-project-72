package hexlet.code.repository;

import javax.sql.DataSource;

public class BaseRepository {

    protected DataSource dataSource;

    public BaseRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
