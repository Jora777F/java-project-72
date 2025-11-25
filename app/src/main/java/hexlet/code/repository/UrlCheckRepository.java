package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UrlCheckRepository extends BaseRepository {

    public UrlCheckRepository(DataSource dataSource) {
        super(dataSource);
    }

    public void save(UrlCheck urlCheck) throws SQLException {
        var sql = "INSERT INTO url_checks (url_id, status_code, h1, title, description, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, urlCheck.getUrlId());
            stmt.setInt(2, urlCheck.getStatusCode());
            stmt.setString(3, urlCheck.getH1());
            stmt.setString(4, urlCheck.getTitle());
            stmt.setString(5, urlCheck.getDescription());
            stmt.setTimestamp(6, urlCheck.getCreatedAt());
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public List<UrlCheck> getEntitiesByUrlId(Long urlId) throws SQLException {
        var sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, urlId);

            ResultSet resultSet = preparedStatement.executeQuery();
            ArrayList<UrlCheck> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(mapResultSetToUrlCheck(resultSet));
            }
            return result;
        }
    }

    public Map<Long, UrlCheck> getListOfLastChecks() throws SQLException {
        String sql = "SELECT DISTINCT ON (url_id) * from url_checks ORDER BY url_id DESC, created_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Long, UrlCheck> latestChecks = new HashMap<>();

            while (resultSet.next()) {
                UrlCheck urlCheck = mapResultSetToUrlCheck(resultSet);
                latestChecks.put(urlCheck.getUrlId(), urlCheck);
            }
            return latestChecks;
        }
    }

    private UrlCheck mapResultSetToUrlCheck(ResultSet rs) throws SQLException {
        var id = rs.getLong("id");
        var urlId = rs.getLong("url_id");
        var statusCode = rs.getInt("status_code");
        var title = rs.getString("title");
        var h1 = rs.getString("h1");
        var description = rs.getString("description");
        var createdAt = rs.getTimestamp("created_at");

        var urlCheck = new UrlCheck(statusCode, title, h1, description, urlId, createdAt);
        urlCheck.setId(id);
        return urlCheck;
    }
}
