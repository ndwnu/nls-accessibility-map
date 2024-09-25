package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.services;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.GenerateProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers.VehicleTypeVehiclePropertiesMapper;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MapGeneratorService {

    private final GenerateProperties generateProperties;

    private final GenerateConfiguration generateConfiguration;

    private final AccessibilityMapService accessibilityMapService;

    private final VehicleTypeVehiclePropertiesMapper vehicleTypeVehiclePropertiesMapper;

    public List<RoadSection> getInaccessibleRoadSections() {

        return Collections.emptyList();
    }
}
