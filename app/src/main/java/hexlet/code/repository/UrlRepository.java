package hexlet.code.repository;

import hexlet.code.model.Url;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UrlRepository extends BaseRepository {

    public UrlRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void save(Url url) throws SQLException {
        String sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            Timestamp createdAt = new Timestamp(System.currentTimeMillis());

            preparedStatement.setString(1, url.getName());
            preparedStatement.setTimestamp(2, createdAt);
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                url.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Creating url failed, no ID obtained.");
            }
        }
    }

    public Optional<Url> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM urls WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);
            final ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                final String name = resultSet.getString("name");
                final Timestamp createdAt = resultSet.getTimestamp("created_at");

                Url url = new Url(name);
                url.setCreatedAt(createdAt);
                url.setId(resultSet.getLong("id"));
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public Optional<Url> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM urls WHERE name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                final Timestamp createdAt = resultSet.getTimestamp("created_at");

                Url url = new Url(resultSet.getString("name"));
                url.setCreatedAt(createdAt);
                url.setId(resultSet.getLong("id"));

                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public List<Url> getEntities() throws SQLException {
        String sql = "SELECT * FROM urls ORDER BY created_at DESC";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Url> result = new ArrayList<>();

            while (resultSet.next()) {
                final long id = resultSet.getLong("id");
                final String name = resultSet.getString("name");
                final Timestamp createdAt = resultSet.getTimestamp("created_at");

                Url url = new Url(name);
                url.setCreatedAt(createdAt);
                url.setId(id);
                result.add(url);
            }

            return result;
        }
    }
}
