package nu.ndw.nls.accessibilitymap.trafficsignclient.utils;

import java.time.Instant;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;

@Getter
public class MaxEventTimestampTracker {

    private Instant maxEventTimestamp = Instant.MIN;

    public TrafficSignJsonDtoV3 updateMaxEventTimeStampAndContinue(TrafficSignJsonDtoV3 trafficSignData) {
        maxEventTimestamp = max(trafficSignData.getPublicationTimestamp(), maxEventTimestamp);
        return trafficSignData;
    }

    private Instant max(Instant publicationTimestamp, Instant currentMax) {
        if (publicationTimestamp == null) {
            return currentMax;
        }

        return publicationTimestamp.isAfter(currentMax) ? publicationTimestamp : currentMax;
    }
}
