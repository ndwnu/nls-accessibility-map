package nu.ndw.nls.accessibilitymap.trafficsignclient.utils;

import java.time.LocalDate;
import java.util.Optional;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;

@Getter
public class MaxNwbVersionTracker {

    private LocalDate maxNwbReferenceDate = LocalDate.MIN;

    public TrafficSignGeoJsonDtoV5Json updateMaxNwbVersionAndContinue(TrafficSignGeoJsonDtoV5Json trafficSignData) {
        getNwbVersion(trafficSignData)
                .ifPresent(nwbVersion -> maxNwbReferenceDate = max(nwbVersion, maxNwbReferenceDate));

        return trafficSignData;
    }

    private LocalDate max(LocalDate nwbVersion, LocalDate currentMax) {
        return nwbVersion.isAfter(currentMax) ? nwbVersion : currentMax;
    }

    private Optional<LocalDate> getNwbVersion(TrafficSignGeoJsonDtoV5Json trafficSignData) {
        if (trafficSignData.getProperties() == null ||
            trafficSignData.getProperties().getNwbVersion() == null) {
            return Optional.empty();
        }

        return Optional.of(trafficSignData.getProperties().getNwbVersion());
    }
}
