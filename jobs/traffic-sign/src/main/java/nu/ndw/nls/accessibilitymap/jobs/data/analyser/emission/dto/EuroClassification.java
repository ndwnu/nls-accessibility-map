package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EuroClassification {
    EURO_1("EURO_1"),
    EURO_2("EURO_2"),
    EURO_3("EURO_3"),
    EURO_4("EURO_4"),
    EURO_5("EURO_5"),
    EURO_6("EURO_6"),
    UNKNOWN("UNKNOWN");

    private final String value;

    @JsonCreator
    public static EuroClassification fromValue(String value) {

        for (EuroClassification status : EuroClassification.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
