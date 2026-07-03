package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.deserializer;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

/**
 * Deserializes a comma separated string (e.g. "car,bus") into a {@link Set} of enum values, for use on
 * {@code Set<SomeEnum>} properties fed with plain strings, such as cucumber data table cells.
 */
public class CommaDelimitedEnumSetDeserializer extends ValueDeserializer<Set<Enum<?>>> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    @SuppressWarnings("unchecked")
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {

        JavaType contentType = property.getType().getContentType();
        CommaDelimitedEnumSetDeserializer deserializer = new CommaDelimitedEnumSetDeserializer();
        deserializer.enumClass = (Class<? extends Enum<?>>) contentType.getRawClass();
        return deserializer;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Set<Enum<?>> deserialize(JsonParser p, DeserializationContext ctxt) {

        String text = p.getString();
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(text.split(","))
                .map(String::trim)
                .map(value -> parseEnum(value.toUpperCase(Locale.US)))
                .collect(Collectors.toSet());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Enum<?> parseEnum(String value) {
        return Enum.valueOf((Class) enumClass, value);
    }
}
