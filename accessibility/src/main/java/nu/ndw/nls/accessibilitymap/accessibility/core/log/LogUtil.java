package nu.ndw.nls.accessibilitymap.accessibility.core.log;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArgument;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j
public class LogUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static @NonNull StructuredArgument keyValueJson(String key, Object value) {
        try {
            return keyValue(key, objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException exception) {
            String message = "Could not create json string from object.";
            log.error(message, exception);
            return keyValue(key, "{\"error\":\"%s\"}".formatted(message));
        }
    }
}
