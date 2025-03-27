package util;
import org.tinylog.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logAction(LocalDateTime time, String userId, String action, String input, String status) {
        String timestamp = formatter.format(time);
        String message = String.format("%s - %s - %s - %s - %s", timestamp, userId, action, input, status);
        Logger.info(message);
    }
}
