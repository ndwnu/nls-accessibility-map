package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.accessibility.services.NetworkService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.DirectionalRoadSectionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.RoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.mappers.RvvCodeWindowTimeEncodedValueMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignApiRvvCodeMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrichTrafficSignService {

    private final TrafficSignService trafficSignService;

    private final TrafficSignApiRvvCodeMapper trafficSignApiRvvCodeMapper;

    private final DirectionalRoadSectionMapper directionalRoadSectionMapper;

    private final RvvCodeWindowTimeEncodedValueMapper rvvCodeWindowTimeEncodedValueMapper;

    private final NetworkService networkService;

    private final TrafficSignMapper trafficSignMapper;

    private final TrafficSignFilterService trafficSignFilterService;

    public List<RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign>> addTrafficSigns(
            CmdGenerateGeoJsonType type, Collection<RoadSection> roadSections) {

        WindowTimeEncodedValue windowTimeEncodedValue = rvvCodeWindowTimeEncodedValueMapper.map(type);
        Set<String> trafficSignApiRvvCodes = trafficSignApiRvvCodeMapper.mapRvvCode(type);

        return roadSections.stream()
                .map(roadSection -> map(roadSection, windowTimeEncodedValue, trafficSignApiRvvCodes))
                .flatMap(Collection::stream)
                .toList();
    }

    private List<RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign>>
                map(RoadSection roadSection, WindowTimeEncodedValue windowTimeEncodedValue,
                    Set<String> trafficSignRvvCodes) {


        List<TrafficSignGeoJsonDto> roadSectionTrafficSigns;

        // In GrapHopper and TrafficSign API the data is encoded on a road section level, so it makes sense to
        // retrieve the data once and then re-use the result where possible for both driving directions
        if (    hasInaccessibility(roadSection) &&
                hasWindowTimeTrafficSignInRoutingNetwork(roadSection, windowTimeEncodedValue)) {

            // At least one window time traffic sign on this road section
            TrafficSignData trafficSignData = trafficSignService.getTrafficSigns(trafficSignRvvCodes,
                    Set.of(roadSection.getRoadSectionId()));

            // Get traffic signs
            roadSectionTrafficSigns = trafficSignData.getTrafficSignsByRoadSectionId(
                    (long) roadSection.getRoadSectionId());
        } else {
            roadSectionTrafficSigns = Collections.emptyList();
        }

        return mapToDirectionalRoadSectionAndInaccessibleWithTrafficSigns(roadSection, roadSectionTrafficSigns);
    }

    private List<RoadSectionAndTrafficSign<DirectionalRoadSection, TrafficSign>>
        mapToDirectionalRoadSectionAndInaccessibleWithTrafficSigns(RoadSection roadSection,
                                                                   List<TrafficSignGeoJsonDto> roadSectionTrafficSigns){
        return directionalRoadSectionMapper.map(roadSection)
                .stream()
                .map(directionalRoadSection ->
                        RoadSectionAndTrafficSign.<DirectionalRoadSection, TrafficSign>builder()
                                .roadSection(directionalRoadSection)
                                .trafficSign(findFirstWindowTrafficSignForThisDirection(roadSectionTrafficSigns,
                                        directionalRoadSection))
                                .build())
                .toList();
    }

    private TrafficSign findFirstWindowTrafficSignForThisDirection(List<TrafficSignGeoJsonDto> trafficSignGeoJsons,
            DirectionalRoadSection directionalRoadSection) {

        // null results are possible, because we don't know for which direction we have a traffic sign
        return trafficSignFilterService.findWindowTimeTrafficSignsOrderInDrivingDirection(
                        trafficSignGeoJsons, directionalRoadSection.isForwards())
                .stream()
                .findFirst()
                .map(trafficSignMapper::map)
                .orElse(null);
    }

    private boolean hasWindowTimeTrafficSignInRoutingNetwork(RoadSection roadSection,
            WindowTimeEncodedValue windowTimeEncodedValue) {
        return networkService.hasWindowTimeByRoadSectionId(roadSection.getRoadSectionId(),
                windowTimeEncodedValue);
    }

    private boolean hasInaccessibility(RoadSection roadsection) {
        return roadsection.getForwardAccessible() == Boolean.FALSE ||
                roadsection.getBackwardAccessible() == Boolean.FALSE;
    }
}
