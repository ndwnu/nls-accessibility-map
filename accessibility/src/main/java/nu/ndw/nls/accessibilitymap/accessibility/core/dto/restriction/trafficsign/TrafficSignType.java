package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TrafficSignType {

    C1("C1"),
    C6("C6"),
    C7("C7"),
    C7A("C7a"),
    C7B("C7b"),
    C8("C8"),
    C9("C9"),
    C10("C10"),
    C11("C11"),
    C12("C12"),
    C17("C17"),
    C18("C18"),
    C19("C19"),
    C20("C20"),
    C21("C21"),
    C22("C22"),
    C22A("C22a"),
    C22C("C22c");

    private final String rvvCode;

    public static TrafficSignType fromRvvCode(String rvvCode) {
        return Arrays.stream(TrafficSignType.values())
                .filter(t -> t.rvvCode.equals(rvvCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid TrafficSignType: " + rvvCode));
    }
}
