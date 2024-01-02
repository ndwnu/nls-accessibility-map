package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;

@Getter
public class MaxNwbVersionTracker {

    private LocalDate maxNwbReferenceDate = LocalDate.MIN;

    public TrafficSignJsonDtoV3 updateMaxNwbVersionAndContinue(TrafficSignJsonDtoV3 trafficSignData) {
        String nwbVersion = trafficSignData.getLocation().getRoad().getNwbVersion();
        maxNwbReferenceDate = max(nwbVersion, maxNwbReferenceDate);
        return trafficSignData;
    }

    private LocalDate max(String nwbVersion, LocalDate currentMax) {
        if (nwbVersion == null) {
            return currentMax;
        }
        LocalDate newVersion;
        try {
            newVersion = LocalDate.parse(nwbVersion);
        } catch (DateTimeParseException ignored) {
            return currentMax;
        }
        return newVersion.isAfter(currentMax) ? newVersion : currentMax;
    }
}
