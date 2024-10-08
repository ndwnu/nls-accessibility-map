package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.mapper.AccessibilityRequestMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers.OutputWriter;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapGeneratorService {

    private final AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    private final List<OutputWriter> outputWriters;

    private final AccessibilityService accessibilityService;

    private final MessageService messageService;

    private final AccessibilityRequestMapper accessibilityRequestMapper;

    public void generate(@Valid GeoGenerationProperties geoGenerationProperties) {

        log.info("Generating with the following properties: {}", geoGenerationProperties);
        Accessibility accessibility = accessibilityService.calculateAccessibility(
                accessibilityRequestMapper.map(geoGenerationProperties));

        long roadSectionsWithTrafficSigns = accessibility.mergedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasTrafficSign)
                .count();
        log.debug("Found {} with road sections with traffic signs.", roadSectionsWithTrafficSigns);

        outputWriters.forEach(
                outputWriter -> outputWriter.writeToFile(accessibility, geoGenerationProperties));

        if (geoGenerationProperties.publishEvents()) {
            sendEventGeneratingDone(geoGenerationProperties);
        }
    }

    private void sendEventGeneratingDone(
            GeoGenerationProperties geoGenerationProperties) {

        NlsEvent nlsEvent = accessibilityGeoJsonGeneratedEventMapper.map(
                geoGenerationProperties.trafficSignType(),
                geoGenerationProperties.exportVersion(),
                geoGenerationProperties.nwbVersion(),
                geoGenerationProperties.startTime().toInstant());

        messageService.publish(nlsEvent);
    }
}
