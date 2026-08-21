package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.deserializer;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

@Slf4j
public class CommaDelimitedEnumSetDeserializer extends ValueDeserializer<Set<Enum<?>>> {

    private JavaType enumType;

    private ValueDeserializer<?> enumDeserializer;

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        JavaType contentType = property.getType().getContentType();
        CommaDelimitedEnumSetDeserializer deserializer = new CommaDelimitedEnumSetDeserializer();
        deserializer.enumType = contentType;
        deserializer.enumDeserializer = context.findContextualValueDeserializer(contentType, property);
        return deserializer;
    }

    @Override
    public Set<Enum<?>> deserialize(JsonParser p, DeserializationContext context) {
        String text = p.getString();
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(token -> (Enum<?>) deserializeToken(token, context))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Object deserializeToken(String token, DeserializationContext context) {

        try (JsonParser tokenParser = context.createParser(wrapInQuotes(escapeQuotes(token)))) {
            tokenParser.nextToken();
            return enumDeserializer.deserialize(tokenParser, context);
        } catch (Exception e) {
            log.error("Failed to deserialize enum value '{}'.", token, e);
            return context.handleWeirdStringValue(enumType.getRawClass(), token, e.getMessage());
        }
    }

    private String wrapInQuotes(String text) {
        return "\"" + text + "\"";
    }

    private String escapeQuotes(String text) {
        return text.replace("\"", "\\\"");
    }
}
