package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureCollectionMapperTest {

    @InjectMocks
    private AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    @Mock
    private List<AccessibilityGeoJsonFeature> accessibilityGeoJsonFeatures;

    @Test
    void map_ok() {
        assertEquals(AccessibilityGeoJsonFeatureCollection.builder()
                .features(accessibilityGeoJsonFeatures)
                .build(), accessibilityGeoJsonFeatureCollectionMapper.map(accessibilityGeoJsonFeatures));
    }
}