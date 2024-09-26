package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.mappers;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.springframework.stereotype.Service;

@Service
public class TrafficSignMapper {

    public TrafficSign mapFromTrafficSignGeoJsonDto(TrafficSignGeoJsonDto trafficSignGeoJsonDto) {

        return TrafficSign.builder()
                .trafficSignType(TrafficSignType.valueOf(trafficSignGeoJsonDto.getProperties().getRvvCode()))
                .build();
    }
}
