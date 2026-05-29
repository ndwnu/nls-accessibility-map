package nu.ndw.nls.accessibilitymap.accessibility.core.log;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public class LogUtil {

    private static final JsonMapper jsonMapper = new JsonMapper();

    public static @NonNull StructuredArgument keyValueJson(String key, Object value) {
        try {
            return keyValue(key, jsonMapper.writeValueAsString(value));
        } catch (JacksonException exception) {
            String message = "Could not create json string from object.";
            log.error(message, exception);
            return keyValue(key, "{\"error\":\"%s\"}".formatted(message));
        }
    }
}
