package hexlet.code.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Url {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public Url(String name) {
        this.name = name;
    }
}
