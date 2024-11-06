package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TrafficSignType {
    C6("C6"),
    C7("C7"),
    C7B("C7b"),
    C12("C12"),
    C22C("C22c");
    private final String rvvCode;
}
