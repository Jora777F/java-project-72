package hexlet.code.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : FORMATTER.format(dateTime);
    }
}
