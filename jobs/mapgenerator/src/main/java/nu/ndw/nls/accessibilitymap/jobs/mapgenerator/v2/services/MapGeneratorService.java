package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService.ResultType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.LocalDateVersionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.VehicleTypeVehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSectionWithDirection;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapGeneratorService {

    private final LocalDateVersionMapper localDateVersionMapper;
    private final GenerateProperties generateProperties;

    private final GenerateConfiguration generateConfiguration;

    private final AccessibilityMapService accessibilityMapService;

    private final VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper;

    private final AccessibilityConfiguration accessibilityConfiguration;


    public List<RoadSectionWithDirection> getInaccessibleRoadSections(CmdGenerateGeoJsonType type) {
        LocalDateTime versionLocalDateTime = LocalDateTime.now();
        int version = localDateVersionMapper.map(versionLocalDateTime.toLocalDate());
        int nwbVersion = accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion();
        log.info("Generating geojson: {} version: {} based on NWB version: {}", type, version, nwbVersion);

        VehicleProperties vehicleProperties = vehicleTypeVehiclePropertiesMapper.map(type);

        SortedMap<Integer, RoadSection> idToRoadSections =
                accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties,
                        generateConfiguration.getStartLocation(), generateProperties.getSearchDistanceInMeters()
                        , ResultType.DIFFERENCE_OF_ADDED_RESTRICTIONS);

        List<RoadSection> inaccessibleRoads = idToRoadSections.values().stream()
                .filter(MapGeneratorService::isInaccessible)
                .toList();
        return inaccessibleRoads
                .stream()
                .map(r ->
                        RoadSectionWithDirection
                                .builder()
                                .roadSectionId(r.getRoadSectionId())
                                .forward(DirectionalSegment
                                        .builder()
                                        .accessible(r.getForwardAccessible())
                                        .direction(Direction.FORWARD)
                                        .lineString(r.getGeometry())
                                        //.trafficSign(//trafficSignService.getTrafficSign(roadSection,geometry,Direction.Forward).orElse(null))
                                        .build())
                                .backward(DirectionalSegment
                                        .builder()
                                        .accessible(r.getBackwardAccessible())
                                        .direction(Direction.BACKWARD)
                                        .lineString(r.getGeometry().reverse())
                                        //.trafficSign(//trafficSignService.getTrafficSign(roadSection,geometry,Direction.BACKWAR).orElse(null))
                                        .build())
                                .build()

                ).toList();
    }

    private static boolean isInaccessible(RoadSection r) {
        return r.getBackwardAccessible() != null && !r.getBackwardAccessible()
                || r.getBackwardAccessible() != null && !r.getForwardAccessible() || r.getBackwardAccessible() == null;
    }


}
