package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignTestDataService {

    private final GraphHopperDriver graphHopperDriver;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @SuppressWarnings("java:S109")
    public TrafficSignGeoJsonDto createTrafficSignGeoJsonDto(TrafficSign trafficSign) {

        return TrafficSignGeoJsonDto.builder()
                .id(UUID.fromString(trafficSign.id()))
                .geometry(new PointJson().type(TypeEnum.POINT).coordinates(List.of(trafficSign.location().x, trafficSign.location().y)))
                .properties(TrafficSignPropertiesDto.builder()
                        .fraction(trafficSign.fraction())
                        .blackCode(trafficSign.blackCode())
                        .rvvCode(trafficSign.rvvCode())
                        .drivingDirection(trafficSign.directionType())
                        .roadSectionId(trafficSign.roadSectionId())
                        .trafficOrderUrl(trafficSign.regulationOrderId())
                        .textSigns(List.of(
                                TextSign.builder()
                                        .type(TextSignType.TIME_PERIOD)
                                        .text(trafficSign.windowTime())
                                        .build()))
                        .build())
                .build();
    }
}
