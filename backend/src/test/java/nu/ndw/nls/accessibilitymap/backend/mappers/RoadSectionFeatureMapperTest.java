package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.GeometryJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.LineStringJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.accessibilitymap.backend.model.RoadSection;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import nu.ndw.nls.geometry.rounding.dto.RoundDoubleConfiguration;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFeatureMapperTest {

    @Mock
    private GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    @InjectMocks
    private RoadSectionFeatureMapper roadSectionFeatureMapper;

    @Mock
    private RoadSection roadSection;
    @Mock
    private LineString geometry;
    @Mock
    private LineString reverseGeometry;
    @Mock
    private List<List<Double>> coordinates;
    @Mock
    private CandidateMatch candidateMatch;

    @Test
    void map_ok_forward() {
        when(roadSection.getRoadSectionId()).thenReturn(1);
        when(roadSection.getGeometry()).thenReturn(geometry);
        when(roadSection.getForwardAccessible()).thenReturn(true);
        when(geoJsonLineStringCoordinateMapper.map(geometry, RoundDoubleConfiguration.ROUND_7_HALF_UP))
                .thenReturn(coordinates);

        RoadSectionFeatureJson actual = roadSectionFeatureMapper.map(roadSection, null, true);

        RoadSectionFeatureJson expected = new RoadSectionFeatureJson(RoadSectionFeatureJson.TypeEnum.FEATURE, 1,
                new LineStringJson(coordinates, TypeEnum.LINE_STRING))
                .properties(new RoadSectionPropertiesJson(true).matched(null));
        assertEquals(expected, actual);
    }

    @Test
    void map_ok_backward() {
        when(roadSection.getRoadSectionId()).thenReturn(1);
        when(roadSection.getGeometry()).thenReturn(geometry);
        when(geometry.reverse()).thenReturn(reverseGeometry);
        when(roadSection.getBackwardAccessible()).thenReturn(true);
        when(geoJsonLineStringCoordinateMapper.map(reverseGeometry, RoundDoubleConfiguration.ROUND_7_HALF_UP))
                .thenReturn(coordinates);

        RoadSectionFeatureJson actual = roadSectionFeatureMapper.map(roadSection, null, false);

        RoadSectionFeatureJson expected = new RoadSectionFeatureJson(RoadSectionFeatureJson.TypeEnum.FEATURE, -1,
                new LineStringJson(coordinates, TypeEnum.LINE_STRING))
                .properties(new RoadSectionPropertiesJson(true).matched(null));
        assertEquals(expected, actual);
    }

    @Test
    void map_ok_matchedTrue() {
        when(roadSection.getRoadSectionId()).thenReturn(1);
        when(candidateMatch.getMatchedLinkId()).thenReturn(1);
        when(candidateMatch.isReversed()).thenReturn(false);

        RoadSectionFeatureJson actual = roadSectionFeatureMapper.map(roadSection, candidateMatch, true);

        assertTrue(actual.getProperties().getMatched());
    }

    @Test
    void map_ok_matchedFalse() {
        when(roadSection.getRoadSectionId()).thenReturn(1);
        when(candidateMatch.getMatchedLinkId()).thenReturn(1);
        when(candidateMatch.isReversed()).thenReturn(true);

        RoadSectionFeatureJson actual = roadSectionFeatureMapper.map(roadSection, candidateMatch, true);

        assertFalse(actual.getProperties().getMatched());
    }
}
