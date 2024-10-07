package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.writers.OutputWriter;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.nwb.services.NdwDataService;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapGeneratorService {

    private final AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    private final NdwDataService ndwDataService;

    private final List<OutputWriter> outputWriters;

    private final AccessibilityService accessibilityService;

    private final MessageService messageService;

    public void generate(@Valid GeoGenerationProperties mapGenerationProperties) {

        LocalDateTime startTime = LocalDateTime.now();

        Accessibility accessibility = calculateAccessibility(mapGenerationProperties);

        long roadSectionsWithTrafficSigns = accessibility.mergedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasTrafficSign)
                .count();
        log.debug("Found {} with road sections with traffic signs.", roadSectionsWithTrafficSigns);

        outputWriters.forEach(
                outputWriter -> outputWriter.writeToFile(accessibility, mapGenerationProperties));

        if (mapGenerationProperties.isPublishEvents()) {
            sendEventGeneratingDone(mapGenerationProperties.getTrafficSignType(), mapGenerationProperties, startTime);
        }
    }

    private Accessibility calculateAccessibility(
            GeoGenerationProperties mapGenerationProperties) {

        log.debug("Generating with the following properties: {}", mapGenerationProperties);

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .vehicleProperties(mapGenerationProperties.getVehicleProperties())
                .startLocationLatitude(mapGenerationProperties.getStartLocationLatitude())
                .startLocationLongitude(mapGenerationProperties.getStartLocationLongitude())
                .searchDistanceInMetres(mapGenerationProperties.getSearchRadiusInMeters())
                .trafficSignType(mapGenerationProperties.getTrafficSignType())
                .includeOnlyTimeWindowedSigns(mapGenerationProperties.isIncludeOnlyTimeWindowedSigns())
                .build();

        Accessibility accessibility = accessibilityService.calculateAccessibility(accessibilityRequest);

        // TODO: can be moved to accessibilityService?
        // ndwDataService.addNwbDataToAccessibility(accessibility, mapGenerationProperties.getNwbVersion());

        return accessibility;
    }

    private void sendEventGeneratingDone(
            TrafficSignType trafficSignType,
            GeoGenerationProperties mapGenerationProperties,
            LocalDateTime versionLocalDateTime) {

        NlsEvent nlsEvent = accessibilityGeoJsonGeneratedEventMapper.map(
                trafficSignType,
                mapGenerationProperties.getExportVersion(),
                mapGenerationProperties.getNwbVersion(),
                versionLocalDateTime.toInstant(ZoneOffset.UTC));

        log.debug("Sending {} created event for type {}, version {}, NWB version {} and traffic sign timestamp {}",
                nlsEvent.getType().getLabel(),
                nlsEvent.getSubject().getType(),
                nlsEvent.getSubject().getVersion(),
                nlsEvent.getSubject().getNwbVersion(),
                versionLocalDateTime);

        messageService.publish(nlsEvent);
    }

}
