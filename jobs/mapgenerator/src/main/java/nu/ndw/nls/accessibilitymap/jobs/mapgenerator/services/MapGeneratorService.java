package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.event.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.Exporter;
import nu.ndw.nls.events.NlsEvent;
import nu.ndw.nls.springboot.messaging.services.MessageService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapGeneratorService {

    private final AccessibilityGeoJsonGeneratedEventMapper accessibilityGeoJsonGeneratedEventMapper;

    private final List<Exporter> resultExporters;

    private final AccessibilityService accessibilityService;

    private final MessageService messageService;

    public void generate(@Valid ExportProperties exportProperties) {

        log.info("Generating with the following properties: {}", exportProperties);
        Accessibility accessibility = accessibilityService.calculateAccessibility(exportProperties.accessibilityRequest());

        long roadSectionsWithTrafficSigns = accessibility.combinedAccessibility().stream()
                .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(DirectionalSegment::hasRestrictions)
                .count();

        log.debug("Found {} with road section fragments with traffic signs.", roadSectionsWithTrafficSigns);
        resultExporters.stream()
                .filter(abstractGeoJsonWriter -> abstractGeoJsonWriter.isEnabled(exportProperties.exportTypes()))
                .forEach(abstractGeoJsonWriter -> abstractGeoJsonWriter.export(
                        accessibility,
                        exportProperties));

        if (exportProperties.publishEvents()) {
            sendEventGeneratingDone(exportProperties);
        }
    }

    private void sendEventGeneratingDone(
            ExportProperties exportProperties) {

        NlsEvent nlsEvent = accessibilityGeoJsonGeneratedEventMapper.map(
                exportProperties.accessibilityRequest().trafficSignTypes().stream().toList(),
                -1,
                exportProperties.nwbVersion(),
                exportProperties.startTime().toInstant());

        messageService.publish(nlsEvent);
    }
}
