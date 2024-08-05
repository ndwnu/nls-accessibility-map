package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers;

import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import org.springframework.stereotype.Component;

@Component
public class NoEntrySignWindowedMapper {

    public TrafficSignJsonDtoV3 map(TrafficSignJsonDtoV3 trafficSignJsonDtoV3) {
        if (trafficSignJsonDtoV3.getTextSigns().stream().anyMatch(sign -> "TIJD".equals(sign.getType()))) {
            return trafficSignJsonDtoV3.toBuilder()
                    .rvvCode(trafficSignJsonDtoV3.getRvvCode().concat("T"))
                    .build();
        }
        return trafficSignJsonDtoV3;
    }
}