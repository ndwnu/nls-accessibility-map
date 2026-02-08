package nu.ndw.nls.accessibilitymap.job.mapgenerator.services;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.AccessibilityService;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.event.AccessibilityGeoJsonGeneratedEventMapper;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.export.Exporter;
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

    private final NetworkDataService networkDataService;

    private final MessageService messageService;

    public void generate(@Valid ExportProperties exportProperties) {

        log.info("Generating with the following properties: {}", exportProperties);
        NetworkData networkData = networkDataService.get();
        Accessibility accessibility = accessibilityService.calculateAccessibility(
                networkData,
                exportProperties.accessibilityRequest());

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
            sendEventGeneratingDone(networkData.getNwbVersion(), exportProperties);
        }
    }

    private void sendEventGeneratingDone(
            int nwbVersionId,
            ExportProperties exportProperties) {

        NlsEvent nlsEvent = accessibilityGeoJsonGeneratedEventMapper.map(
                exportProperties.accessibilityRequest().trafficSignTypes().stream().toList(),
                -1,
                nwbVersionId,
                exportProperties.startTime().toInstant());

        messageService.publish(nlsEvent);
    }
}
