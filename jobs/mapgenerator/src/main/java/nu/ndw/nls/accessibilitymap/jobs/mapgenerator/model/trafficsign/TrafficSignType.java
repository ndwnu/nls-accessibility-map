package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.model.trafficsign;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TrafficSignType {
    C6("C6"),
    C7("C7"),
    C7B("C7B"),
    C12("C12"),
    C22C("C22C");

    private final String rvvCode;

//    public String getEncodedValueAttribute(boolean isTimeWindowed) {
//        return "car_access_forbidden_windowed"
//    }
}
