package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.mappers.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TrafficSignDataService {

    final TrafficSignMapper trafficSignMapper;

    final TrafficSignService trafficSignService;

    public List<TrafficSign> findAllByType(Set<TrafficSignType> trafficSignTypes) {
        Set<String> trafficSignCodes = trafficSignTypes.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return trafficSignService.getTrafficSigns(trafficSignCodes)
                .trafficSignsByRoadSectionId().values().stream()
                .flatMap(Collection::stream)
                .map(trafficSignMapper::mapFromTrafficSignGeoJsonDto)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
//
//    private void addTrafficSignToDirectionalSegment(
//            DirectionalSegment directionalSegment,
//            List<TrafficSignGeoJsonDto> trafficSignDataInAllDirections,
//            Predicate<TrafficSignGeoJsonDto> isInForwardDirection, MapGenerationProperties mapGenerationProperties) {
//
//        if (Objects.isNull(directionalSegment)) {
//            return;
//        }
//
//        List<TrafficSignGeoJsonDto> forwardTrafficSigns = trafficSignDataInAllDirections.stream()
//                .filter(isInForwardDirection)
//                .filter(trafficSignGeoJsonDto -> filterByMapGenerationProperties(trafficSignGeoJsonDto,
//                        mapGenerationProperties))
//                .toList();
//
//        directionalSegment.setTrafficSigns(forwardTrafficSigns.stream()
//                .filter(trafficSignGeoJsonDto -> mapGenerationProperties.getTrafficSignsAsString()
//                        .contains(trafficSignGeoJsonDto.getProperties().getRvvCode()))
//                .map(trafficSignMapper::mapFromTrafficSignGeoJsonDto)
//                .toList());
//    }
//
//    private TrafficSignData getTrafficData(
//            Set<TrafficSignType> trafficSignTypes,
//            List<RoadSection> roadSections) {
//
//        Set<String> trafficSignCodes = trafficSignTypes.stream()
//                .map(Enum::name)
//                .collect(Collectors.toSet());
//
//        Set<Long> roadSectionIds = roadSections.stream()
//                .map(RoadSection::getRoadSectionId)
//                .collect(Collectors.toSet());
//
//        return trafficSignService.getTrafficSigns(trafficSignCodes, roadSectionIds);
//    }
//
//    private boolean filterByMapGenerationProperties(
//            TrafficSignGeoJsonDto trafficSignGeoJsonDto,
//            MapGenerationProperties mapGenerationProperties) {
//
//        // TODO: take other options from mapGenerationProperties into account like timed / non timed windows.
//        return true;
//    }
//
//    private final Predicate<TrafficSignGeoJsonDto> isInForwardDirection = trafficSignGeoJsonDto ->
//            trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.FORTH
//                    || trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.BOTH;
//
//    private final Predicate<TrafficSignGeoJsonDto> isInBackwardDirection = trafficSignGeoJsonDto ->
//            trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.BACK
//                    || trafficSignGeoJsonDto.getProperties().getDrivingDirection() == DirectionType.BOTH;

}
