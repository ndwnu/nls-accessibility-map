package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureCollectionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.RoadSection;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFeatureCollectionMapperTest {

    @Mock
    private RoadSectionFeatureMapper roadSectionFeatureMapper;

    @InjectMocks
    private RoadSectionFeatureCollectionMapper roadSectionFeatureCollectionMapper;

    @Mock
    private RoadSection roadSection;
    @Mock
    private CandidateMatch candidateMatch;
    @Mock
    private RoadSectionFeatureJson roadSectionFeatureJson1;
    @Mock
    private RoadSectionFeatureJson roadSectionFeatureJson2;
    @Mock
    private RoadSectionPropertiesJson roadSectionPropertiesJson1;
    @Mock
    private RoadSectionPropertiesJson roadSectionPropertiesJson2;

    @Test
    void map_ok_noFilter() {
        when(roadSectionFeatureMapper.map(roadSection, candidateMatch, true)).thenReturn(roadSectionFeatureJson1);
        when(roadSectionFeatureMapper.map(roadSection, candidateMatch, false)).thenReturn(roadSectionFeatureJson2);
        when(roadSectionFeatureJson1.getProperties()).thenReturn(roadSectionPropertiesJson1);
        when(roadSectionFeatureJson2.getProperties()).thenReturn(roadSectionPropertiesJson2);
        when(roadSectionPropertiesJson1.getAccessible()).thenReturn(true);
        when(roadSectionPropertiesJson2.getAccessible()).thenReturn(null);

        RoadSectionFeatureCollectionJson actual = roadSectionFeatureCollectionMapper.map(
                new TreeMap<>(Map.of(1, roadSection)), candidateMatch, null);

        RoadSectionFeatureCollectionJson expected = new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION,
                List.of(roadSectionFeatureJson1));
        assertEquals(expected, actual);
    }

    @Test
    void map_ok_filterNotMatched() {
        when(roadSectionFeatureMapper.map(roadSection, candidateMatch, true)).thenReturn(roadSectionFeatureJson1);
        when(roadSectionFeatureMapper.map(roadSection, candidateMatch, false)).thenReturn(roadSectionFeatureJson2);
        when(roadSectionFeatureJson1.getProperties()).thenReturn(roadSectionPropertiesJson1);
        when(roadSectionFeatureJson2.getProperties()).thenReturn(roadSectionPropertiesJson2);
        when(roadSectionPropertiesJson1.getAccessible()).thenReturn(true);
        when(roadSectionPropertiesJson2.getAccessible()).thenReturn(false);

        RoadSectionFeatureCollectionJson actual = roadSectionFeatureCollectionMapper.map(
                new TreeMap<>(Map.of(1, roadSection)), candidateMatch, true);

        RoadSectionFeatureCollectionJson expected = new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION,
                List.of(roadSectionFeatureJson1));
        assertEquals(expected, actual);
    }

    @Test
    void map_ok_filterMatched() {
        when(roadSectionFeatureMapper.map(roadSection, candidateMatch, true)).thenReturn(roadSectionFeatureJson1);
        when(roadSectionFeatureMapper.map(roadSection, candidateMatch, false)).thenReturn(roadSectionFeatureJson2);
        when(roadSectionFeatureJson1.getProperties()).thenReturn(roadSectionPropertiesJson1);
        when(roadSectionFeatureJson2.getProperties()).thenReturn(roadSectionPropertiesJson2);
        when(roadSectionPropertiesJson1.getAccessible()).thenReturn(true);
        when(roadSectionPropertiesJson2.getAccessible()).thenReturn(false);
        when(roadSectionPropertiesJson2.getMatched()).thenReturn(true);

        RoadSectionFeatureCollectionJson actual = roadSectionFeatureCollectionMapper.map(
                new TreeMap<>(Map.of(1, roadSection)), candidateMatch, true);

        RoadSectionFeatureCollectionJson expected = new RoadSectionFeatureCollectionJson(TypeEnum.FEATURE_COLLECTION,
                List.of(roadSectionFeatureJson1, roadSectionFeatureJson2));
        assertEquals(expected, actual);
    }
}
