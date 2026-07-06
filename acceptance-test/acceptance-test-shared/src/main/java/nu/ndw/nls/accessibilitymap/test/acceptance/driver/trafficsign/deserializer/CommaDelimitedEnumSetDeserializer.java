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
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType contentType = property.getType().getContentType();
        CommaDelimitedEnumSetDeserializer deserializer = new CommaDelimitedEnumSetDeserializer();
        deserializer.enumType = contentType;
        deserializer.enumDeserializer = ctxt.findContextualValueDeserializer(contentType, property);
        return deserializer;
    }

    @Override
    public Set<Enum<?>> deserialize(JsonParser p, DeserializationContext ctxt) {
        String text = p.getString();
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(token -> (Enum<?>) deserializeToken(token, ctxt))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Object deserializeToken(String token, DeserializationContext ctxt) {

        try (JsonParser tokenParser = ctxt.createParser(wrapInQuotes(escapeQuotes(token)))) {
            tokenParser.nextToken();
            return enumDeserializer.deserialize(tokenParser, ctxt);
        } catch (Exception e) {
            log.error("Failed to deserialize enum value '{}'.", token, e);
            return ctxt.handleWeirdStringValue(enumType.getRawClass(), token, e.getMessage());
        }
    }

    private String wrapInQuotes(String text) {
        return "\"" + text + "\"";
    }

    private String escapeQuotes(String text) {
        return text.replace("\"", "\\\"");
    }
}
