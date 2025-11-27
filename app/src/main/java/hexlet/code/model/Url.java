package hexlet.code.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class Url {

    private Long id;
    private String name;
    private Timestamp createdAt;

    public Url(String name) {
        this.name = name;
    }

    /**
     * Возвращаем дату создания URL в необходимом формате.
     * @return дата создания
     */
    public String getFormattedCreatedAt() {
        return createdAt.toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
