package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.EffectivelyAccessibleGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.FullRoadSectionAccessibilityGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.LocalDateVersionMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.TrafficSignSplitRoadSectionAccessibilityGeoJsonMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.VehicleTypeVehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSignGroupedById;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.GeoJsonOutputFormat;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateGeoJsonService {

    private final GenerateProperties generateProperties;

    private final GenerateConfiguration generateConfiguration;

    private final AccessibilityMapService accessibilityMapService;

    private final FileService uploadService;

    private final AccessibilityConfiguration accessibilityConfiguration;

    private final MessageService messageService;

    private final AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    private final LocalDateVersionMapper localDateVersionMapper;

    private final FileService fileService;

    private final VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper;

    private final EnrichTrafficSignService enrichTrafficSignService;

    private final FullRoadSectionAccessibilityGeoJsonMapper fullRoadSectionAccessibilityGeoJsonMapper;

    private final TrafficSignSplitRoadSectionAccessibilityGeoJsonMapper
            trafficSignSplitRoadSectionAccessibilityGeoJsonMapper;

    private final EffectivelyAccessibleGeoJsonMapper effectivelyAccessibleGeoJsonMapper;

    private final EffectiveAccessibilityService effectiveAccessibilityService;

    public void generate( CmdGenerateGeoJsonType type,
                          GeoJsonOutputFormat outputFormat,
                            boolean produceMessage) {
        LocalDateTime versionLocalDateTime = LocalDateTime.now();
        int version = localDateVersionMapper.map(versionLocalDateTime.toLocalDate());
        int nwbVersion = accessibilityConfiguration.accessibilityGraphhopperMetaData().nwbVersion();

        log.info("Generating geojson: {} version: {} based on NWB version: {}", type, version, nwbVersion);

        VehicleProperties vehicleProperties = vehicleTypeVehiclePropertiesMapper.map(type);

        SortedMap<Integer, RoadSection> accessibilityRoadSectionsGroupedById =
                accessibilityMapService.determineAccessibilityByRoadSection(vehicleProperties,
                        generateConfiguration.getStartLocation(), generateProperties.getSearchDistanceInMeters()
                , ResultType.DIFFERENCE_OF_ADDED_RESTRICTIONS);

        logDebugStatistics(accessibilityRoadSectionsGroupedById);

        List<DirectionalRoadSectionAndTrafficSignGroupedById> directionalRoadSectionsWithTrafficSignsGroupedByIds =
                enrichTrafficSignService.addTrafficSigns(type, accessibilityRoadSectionsGroupedById.values());

        AccessibilityGeoJsonFeatureCollection geoJson = createGeoJson(outputFormat,
                directionalRoadSectionsWithTrafficSignsGroupedByIds, nwbVersion);

        Path tempFile = fileService.createTmpGeoJsonFile(type);
        try {
            generateConfiguration.getObjectMapper().writeValue(tempFile.toFile(), geoJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize geojson to file: " + tempFile, e);
        }

        uploadService.uploadFile(type, tempFile, versionLocalDateTime.toLocalDate());

        if (produceMessage) {
            sendMessage(type, version, nwbVersion, versionLocalDateTime);
        }
    }

    private AccessibilityGeoJsonFeatureCollection createGeoJson(
            GeoJsonOutputFormat outputFormat,
            List<DirectionalRoadSectionAndTrafficSignGroupedById> directionalRoadSectionAndTrafficSignGroupedByIds,
            int nwbVersion) {

        GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier = new GeoJsonIdSequenceSupplier();

        // Simple GeoJson can directly be mapped
        if (outputFormat == GeoJsonOutputFormat.FULL_ROAD_SECTION) {
            return fullRoadSectionAccessibilityGeoJsonMapper.map(geoJsonIdSequenceSupplier,
                    directionalRoadSectionAndTrafficSignGroupedByIds, nwbVersion);
        } else if (outputFormat == GeoJsonOutputFormat.TRAFFIC_SIGN_SPLIT){
            return trafficSignSplitRoadSectionAccessibilityGeoJsonMapper.map(geoJsonIdSequenceSupplier,
                    directionalRoadSectionAndTrafficSignGroupedByIds, nwbVersion);
        }

        // Determine effective accessibility
        List<DirectionalRoadSectionAndTrafficSign> directionalRoadSectionAndTrafficSigns =
                directionalRoadSectionAndTrafficSignGroupedByIds.stream()
                        .map(effectiveAccessibilityService::determineEffectiveAccessibility)
                        .flatMap(Collection::stream)
                        .toList();

        return effectivelyAccessibleGeoJsonMapper.map(
                geoJsonIdSequenceSupplier,
                directionalRoadSectionAndTrafficSigns,
                nwbVersion);

    }

    private void sendMessage(
            CmdGenerateGeoJsonType type,
            int version,
            int nwbVersion,
            LocalDateTime versionLocalDateTime) {
        NlsEvent nlsEvent = accessibilityGeoJsonGeneratedEventMapper.map(type, version, nwbVersion,
                versionLocalDateTime.toInstant(ZoneOffset.UTC));

        log.debug("Sending {} created event for type {}, version {}, NWB version {} and traffic sign timestamp {}",
                nlsEvent.getType().getLabel(), nlsEvent.getSubject().getType(), version, nwbVersion,
                versionLocalDateTime);

        messageService.publish(nlsEvent);
    }

    private void logDebugStatistics(SortedMap<Integer, RoadSection> idToRoadSectionSortedMap) {
        if (!log.isDebugEnabled()) {
            return;
        }

        long forwardBaseInaccessible = idToRoadSectionSortedMap.values()
                .stream()
                .map(RoadSection::getForwardAccessible)
                .filter(Objects::nonNull)
                .count();

        long forwardInaccessible = idToRoadSectionSortedMap.values()
                .stream()
                .map(RoadSection::getForwardAccessible)
                .filter(aBoolean -> !aBoolean)
                .count();

        long forwardAccessible = idToRoadSectionSortedMap.values()
                .stream()
                .map(RoadSection::getForwardAccessible)
                .filter(aBoolean -> aBoolean)
                .count();

        long backwardBaseInaccessible = idToRoadSectionSortedMap.values()
                .stream()
                .map(RoadSection::getBackwardAccessible)
                .filter(Objects::nonNull)
                .count();

        long backwardInaccessible = idToRoadSectionSortedMap.values()
                .stream()
                .map(RoadSection::getForwardAccessible)
                .filter(aBoolean -> !aBoolean)
                .count();

        long backwardAccessible = idToRoadSectionSortedMap.values()
                .stream()
                .map(RoadSection::getForwardAccessible)
                .filter(aBoolean -> aBoolean)
                .count();

        log.debug("Road sections evaluated: {}, forward( base inaccessible: {}, inaccessible: {}, accessible: {}),"
                        + "backwards( base inaccessible: {}, inaccessible: {}, accessible: {}) ",
                idToRoadSectionSortedMap.size(), forwardBaseInaccessible, forwardInaccessible, forwardAccessible,
                backwardBaseInaccessible, backwardInaccessible, backwardAccessible
                );
    }

}