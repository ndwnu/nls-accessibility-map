package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFeatureMapperTest {

    private static final long ROAD_SECTION_ID = 123L;

    private RoadSectionFeatureMapper roadSectionFeatureMapper;

    @Mock
    private GeoJsonLineStringMergeMapper geoJsonLineStringMergeMapper;

    @Mock
    private LineStringJson lineStringJsonForward;

    @Mock
    private LineStringJson lineStringJsonBackward;

    @Mock
    private RoadSection roadSection;

    @Mock
    private LineString backwardGeometry;

    @Mock
    private LineString forwardGeometry;

    @Mock
    private CandidateMatch startPoint;

    @BeforeEach
    void setUp() {
        roadSectionFeatureMapper = new RoadSectionFeatureMapper(geoJsonLineStringMergeMapper);
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            true,   true
            false,  true
            true,   false
            false,  false
            true,   null
            false,  null
            """)
    void map(boolean includePropertyMatched, Boolean filterOutWithAccessibility) {

        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);

        if (Objects.isNull(filterOutWithAccessibility) || filterOutWithAccessibility) {
            when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        }

        if (!Boolean.FALSE.equals(filterOutWithAccessibility)) {
            when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry))).thenReturn(lineStringJsonForward);
            when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry))).thenReturn(lineStringJsonBackward);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                includePropertyMatched,
                startPoint,
                filterOutWithAccessibility
        );

        if (Boolean.FALSE.equals(filterOutWithAccessibility)) {
            assertThat(result).isEmpty();
        } else {
            Boolean matched = !includePropertyMatched ? null : false;

            assertThat(result).containsExactlyInAnyOrder(
                    buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, true, matched),
                    buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, true, matched)
            );
        }
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            true,   true,   true,   true
            false,  true,   true,   true
            true,   true,   false,   true
            false,  true,   false,   true
            """)
    void map_validatingForwardAndBackwardSegmentsAreCreatedCorrectly(boolean hasForwardSegment, boolean forwardAccessible,
            boolean hasBackwardSegment, boolean backwardAccessible) {

        when(roadSection.hasForwardSegments()).thenReturn(hasForwardSegment);
        when(roadSection.hasBackwardSegments()).thenReturn(hasBackwardSegment);

        if (hasForwardSegment || hasBackwardSegment) {
            when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        }

        if (hasForwardSegment) {
            when(roadSection.isForwardAccessible()).thenReturn(forwardAccessible);
            when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry))).thenReturn(lineStringJsonForward);
        }

        if (hasBackwardSegment) {
            when(roadSection.isBackwardAccessible()).thenReturn(backwardAccessible);
            when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry))).thenReturn(lineStringJsonBackward);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                true,
                startPoint,
                null
        );

        ArrayList<RoadSectionFeatureJson> expectedRoadSectionFeatures = new ArrayList<>();
        if (hasForwardSegment) {
            expectedRoadSectionFeatures.add(
                    buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, forwardAccessible, false));
        }

        if (hasBackwardSegment) {
            expectedRoadSectionFeatures.add(
                    buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, backwardAccessible, false));
        }

        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedRoadSectionFeatures);
    }

    @Test
    void map_matchedSegments() {

        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);

        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);

        when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
        when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry))).thenReturn(lineStringJsonForward);
        when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
        when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry))).thenReturn(lineStringJsonBackward);

        when(startPoint.getMatchedLinkId()).thenReturn((int) ROAD_SECTION_ID);

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                true,
                startPoint,
                true
        );

        assertThat(result).containsExactlyInAnyOrder(
                buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, true, true),
                buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, true, false)
        );
    }

    @Test
    void map_noStartSegment() {

        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);

        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);

        when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
        when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry))).thenReturn(lineStringJsonForward);
        when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
        when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry))).thenReturn(lineStringJsonBackward);

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                true,
                null,
                true
        );

        assertThat(result).containsExactlyInAnyOrder(
                buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, true, false),
                buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, true, false)
        );
    }

    private RoadSectionFeatureJson buildRoadSectionFeature(
            long roadSectionId,
            LineStringJson geometry,
            boolean accessible,
            Boolean matched) {

        return RoadSectionFeatureJson
                .builder()
                .id((int) roadSectionId)
                .geometry(geometry)
                .type(TypeEnum.FEATURE)
                .properties(RoadSectionPropertiesJson.builder()
                        .accessible(accessible)
                        .matched(matched)
                        .build())
                .build();
    }

}
