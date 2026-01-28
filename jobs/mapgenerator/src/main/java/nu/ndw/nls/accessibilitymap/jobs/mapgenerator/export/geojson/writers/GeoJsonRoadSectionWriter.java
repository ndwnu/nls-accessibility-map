package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.FeatureCollection;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GeoJsonRoadSectionWriter extends AbstractGeoJsonWriter {

    private final FeatureBuilder featureBuilder;

    public GeoJsonRoadSectionWriter(
            FileService fileService,
            FeatureBuilder featureBuilder,
            GenerateConfiguration generateConfiguration,
            GeoJsonObjectMapperFactory geoJsonObjectMapperFactory) {

        super(generateConfiguration, geoJsonObjectMapperFactory, fileService);
        this.featureBuilder = featureBuilder;
    }

    @Override
    public boolean isEnabled(Set<ExportType> exportTypes) {
        return exportTypes.contains(ExportType.LINE_STRING_GEO_JSON);
    }

    @Override
    protected FeatureCollection prepareGeoJsonFeatureCollection(
            Accessibility accessibility,
            ExportProperties exportProperties,
            AtomicLong idSequenceSupplier) {

        return FeatureCollection
                .builder()
                .features(accessibility.combinedAccessibility().stream()
                        .map(roadSection -> createFeatures(
                                roadSection,
                                idSequenceSupplier,
                                exportProperties.generateConfiguration()))
                        .flatMap(List::stream)
                        .toList())
                .build();
    }

    private List<Feature> createFeatures(
            RoadSection roadSection,
            AtomicLong idSequenceSupplier,
            GenerateConfiguration generateConfiguration) {

        return roadSection.getRoadSectionFragments().stream()
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .filter(Objects::nonNull)
                .map(directionalSegment -> featureBuilder.createLineStringsAndTrafficSigns(
                        directionalSegment,
                        idSequenceSupplier,
                        generateConfiguration))
                .flatMap(Collection::stream)
                .toList();
    }
}
