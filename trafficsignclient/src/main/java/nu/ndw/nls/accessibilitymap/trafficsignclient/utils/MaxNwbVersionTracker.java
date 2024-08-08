package nu.ndw.nls.accessibilitymap.trafficsignclient.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;

@Getter
public class MaxNwbVersionTracker {

    private LocalDate maxNwbReferenceDate = LocalDate.MIN;

    public TrafficSignGeoJsonDto updateMaxNwbVersionAndContinue(TrafficSignGeoJsonDto trafficSignData) {
        String nwbVersion = String.valueOf(trafficSignData.getProperties().getNwbVersion());
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
