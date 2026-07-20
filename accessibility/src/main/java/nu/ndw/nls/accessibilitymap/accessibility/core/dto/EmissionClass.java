package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmissionClass {
    EURO_1("Euro1"),
    EURO_2("Euro2"),
    EURO_3("Euro3"),
    EURO_4("Euro4"),
    EURO_5("Euro5"),
    EURO_6("Euro6"),
    UNKNOWN("Unknown");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EmissionClass fromValue(String value) {
        for (EmissionClass emissionClass : EmissionClass.values()) {
            if (emissionClass.value.equals(value)) {
                return emissionClass;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
