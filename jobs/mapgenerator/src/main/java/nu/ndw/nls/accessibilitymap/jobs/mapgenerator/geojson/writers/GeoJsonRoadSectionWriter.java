package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.FeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.utils.LongSequenceSupplier;
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
    protected FeatureCollection prepareGeoJsonFeatureCollection(Accessibility accessibility,
            GeoGenerationProperties geoGenerationProperties, LongSequenceSupplier idSequenceSupplier) {

        log.debug("Started building features");
        return FeatureCollection
                .builder()
                .features(accessibility.combinedAccessibility().stream()
                        .map(roadSection -> createFeatures(
                                roadSection,
                                idSequenceSupplier,
                                geoGenerationProperties.generateConfiguration()))
                        .flatMap(List::stream)
                        .toList())
                .build();
    }

    private List<Feature> createFeatures(
            RoadSection roadSection,
            LongSequenceSupplier idSequenceSupplier,
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
