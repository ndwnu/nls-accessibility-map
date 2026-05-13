package nu.ndw.nls.accessibilitymap.accessibility.core.log;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import tools.jackson.core.JacksonException;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public class LogUtil {

    private static final ObjectMapper objectMapper = new JsonMapper();

    static {
    }

    public static @NonNull StructuredArgument keyValueJson(String key, Object value) {
        try {
            return keyValue(key, objectMapper.writeValueAsString(value));
        } catch (JacksonException exception) {
            String message = "Could not create json string from object.";
            log.error(message, exception);
            return keyValue(key, "{\"error\":\"%s\"}".formatted(message));
        }
    }
}
