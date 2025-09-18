package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.FeatureCollection;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeoJsonRoadAsymmetricTrafficSignWriter extends AbstractGeoJsonWriter {

    private final FeatureBuilder featureBuilder;

    public GeoJsonRoadAsymmetricTrafficSignWriter(
            FileService fileService,
            FeatureBuilder featureBuilder,
            GenerateConfiguration generateConfiguration,
            GeoJsonObjectMapperFactory geoJsonObjectMapperFactory) {

        super(generateConfiguration, geoJsonObjectMapperFactory, fileService);
        this.featureBuilder = featureBuilder;
    }

    @Override
    public boolean isEnabled(Set<ExportType> exportTypes) {

        return exportTypes.contains(ExportType.ASYMMETRIC_TRAFFIC_SIGNS_GEO_JSON);
    }

    @Override
    protected FeatureCollection prepareGeoJsonFeatureCollection(
            Accessibility accessibility,
            ExportProperties exportProperties,
            AtomicLong idSequenceSupplier) {

        return FeatureCollection
                .builder()
                .features(accessibility.combinedAccessibility().stream()
                        .flatMap(roadSection -> roadSection.getRoadSectionFragments().stream())
                        .filter(RoadSectionFragment::isPartiallyAccessible)
                        .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                        .filter(DirectionalSegment::hasTrafficSigns)
                        .map(directionalSegment -> featureBuilder.createTrafficSigns(
                                directionalSegment,
                                idSequenceSupplier,
                                exportProperties.generateConfiguration()))
                        .flatMap(List::stream)
                        .toList())
                .build();
    }
}
