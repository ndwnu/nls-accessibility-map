package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import com.esotericsoftware.kryo.kryo5.minlog.Log;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;
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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSectionWithDirection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.trafficsign.service.TrafficSignFactory;
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

    private final TrafficSignFactory trafficSignFactory;

    public void generate(MapGenerationProperties mapGenerationProperties) {

        if (mapGenerationProperties.trafficSigns().size() != 1) {
            throw new IllegalArgumentException("Exactly one traffic sign is supported right now.");
        }

        int version = localDateVersionMapper.map(LocalDateTime.now().toLocalDate());
        int nwbVersion = accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion();
        log.info("Generating geojson: {} version: {} based on NWB version: {}",
                mapGenerationProperties.trafficSigns().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining("-")),
                version,
                nwbVersion);

        CmdGenerateGeoJsonType cmdGenerateGeoJsonType = CmdGenerateGeoJsonType.valueOf(
                mapGenerationProperties.trafficSigns().stream()
                        .map(Enum::name)
                        .findFirst()
                        .orElseThrow()
        );

        List<RoadSectionWithDirection> inaccessibleRoadSections = getInaccessibleRoadSections(cmdGenerateGeoJsonType);

        trafficSignFactory.addTrafficSignDataToRoadSections(inaccessibleRoadSections, mapGenerationProperties);
        // TODO: ndwDataService.addNdwDataToRoadSections(inaccessibleRoadSections);

        log.info("Map generation done.");

        List<RoadSectionWithDirection> roadSectionsWithTrafficSigns = inaccessibleRoadSections.stream()
                .filter(roadSectionWithDirection ->
                        !roadSectionWithDirection.getForward().getTrafficSigns().isEmpty()
                                || !roadSectionWithDirection.getBackward().getTrafficSigns().isEmpty())
                .toList();
        log.info("Found {} with road sections with traffic signs. {}", roadSectionsWithTrafficSigns.size(), roadSectionsWithTrafficSigns);
    }

    private List<RoadSectionWithDirection> getInaccessibleRoadSections(CmdGenerateGeoJsonType cmdGenerateGeoJsonType) {

        VehicleProperties vehicleProperties = vehicleTypeVehiclePropertiesMapper.map(cmdGenerateGeoJsonType);
        SortedMap<Integer, RoadSection> idToRoadSections =
                accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties,
                        generateConfiguration.getStartLocation(), generateProperties.getSearchDistanceInMeters()
                        , ResultType.DIFFERENCE_OF_ADDED_RESTRICTIONS);

        List<RoadSection> inaccessibleRoads = idToRoadSections.values().stream()
                .filter(this::isInaccessible)
                .toList();
        return inaccessibleRoads
                .stream()
                .map(roadSection ->
                        RoadSectionWithDirection
                                .builder()
                                .roadSectionId(roadSection.getRoadSectionId())
                                .forward(DirectionalSegment
                                        .builder()
                                        .accessible(roadSection.getForwardAccessible())
                                        .direction(Direction.FORWARD)
                                        .lineString(roadSection.getGeometry())
                                        .build())
                                .backward(DirectionalSegment
                                        .builder()
                                        .accessible(roadSection.getBackwardAccessible())
                                        .direction(Direction.BACKWARD)
                                        .lineString(roadSection.getGeometry().reverse())
                                        .build())
                                .build()

                ).toList();
    }

    private boolean isInaccessible(RoadSection roadSection) {
        return roadSection.getBackwardAccessible() != null && !roadSection.getBackwardAccessible()
                || roadSection.getBackwardAccessible() != null && !roadSection.getForwardAccessible()
                || roadSection.getBackwardAccessible() == null;
    }


}
