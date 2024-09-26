package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.service;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSectionWithDirection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.mapper.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TrafficSignFactory {

    final TrafficSignMapper trafficSignMapper;
    final nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService trafficSignDataService;

    public void addTrafficSignDataToRoadSections(
            List<RoadSectionWithDirection> roadSections,
            MapGenerationProperties mapGenerationProperties) {

        roadSections.forEach(roadSection -> {
            TrafficSignData trafficSignData = getTrafficData(mapGenerationProperties.getTrafficSigns(),
                    roadSection.getRoadSectionId());

            List<TrafficSignGeoJsonDto> trafficSignDataInAllDirections = trafficSignData.getTrafficSignsByRoadSectionId(
                    roadSection.getRoadSectionId());

            addTrafficSignToDirectionalSegment(roadSection.getForward(), trafficSignDataInAllDirections,
                    isInForwardDirection, mapGenerationProperties);
            addTrafficSignToDirectionalSegment(roadSection.getBackward(), trafficSignDataInAllDirections,
                    isInBackwardDirection, mapGenerationProperties);
        });
    }

    private void addTrafficSignToDirectionalSegment(
            DirectionalSegment roadSection,
            List<TrafficSignGeoJsonDto> trafficSignDataInAllDirections,
            Predicate<TrafficSignGeoJsonDto> isInForwardDirection, MapGenerationProperties mapGenerationProperties) {

        List<TrafficSignGeoJsonDto> forwardTrafficSigns = trafficSignDataInAllDirections.stream()
                .filter(isInForwardDirection)
                .filter(trafficSignGeoJsonDto -> filterByMapGenerationProperties(trafficSignGeoJsonDto,
                        mapGenerationProperties))
                .toList();

        roadSection.setTrafficSigns(forwardTrafficSigns.stream()
                .map(trafficSignMapper::mapFromTrafficSignGeoJsonDto)
                .toList());
    }

    private TrafficSignData getTrafficData(
            Set<TrafficSignType> trafficSignTypes,
            Long roadSectionIds) {

        Set<String> trafficSignCodes = trafficSignTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return trafficSignDataService.getTrafficSigns(trafficSignCodes, Set.of(roadSectionIds));
    }

    private boolean filterByMapGenerationProperties(
            TrafficSignGeoJsonDto trafficSignGeoJsonDto,
            MapGenerationProperties mapGenerationProperties) {

        // TODO: take other options from mapGenerationProperties into account like timed / non timed windows.
        return true;
    }

    private final Predicate<TrafficSignGeoJsonDto> isInForwardDirection = trafficSignGeoJsonDto ->
            trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.FORTH
                    || trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.BOTH;

    private final Predicate<TrafficSignGeoJsonDto> isInBackwardDirection = trafficSignGeoJsonDto ->
            trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.BACK
                    || trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.BOTH;

}
