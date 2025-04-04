package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers;

import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto.TrafficSignPropertiesDtoBuilder;
import org.springframework.stereotype.Component;

@Component
public class NoEntrySignWindowedMapper {

    public TrafficSignGeoJsonDto map(TrafficSignGeoJsonDto trafficSignJsonDto) {
        TrafficSignPropertiesDto properties = trafficSignJsonDto.getProperties();
        TrafficSignPropertiesDtoBuilder builder = properties.toBuilder();
        if (properties.getTextSigns().stream().anyMatch(sign -> TextSignType.TIME_PERIOD == sign.type())) {
            String rvvWindowed = properties.getRvvCode().concat("T");
            builder.rvvCode(rvvWindowed);
            return trafficSignJsonDto.toBuilder().properties(builder.build())
                    .build();
        }
        return trafficSignJsonDto;
    }
}