package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionFeatureJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
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

        when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);

        if (!Boolean.FALSE.equals(filterOutWithAccessibility)) {
            when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry))).thenReturn(lineStringJsonForward);
            when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry))).thenReturn(lineStringJsonBackward);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                includePropertyMatched,
                Long.MAX_VALUE,
                filterOutWithAccessibility
        );

        if (Boolean.FALSE.equals(filterOutWithAccessibility)) {
            assertThat(result).isEmpty();
        } else {
            Boolean matched = !includePropertyMatched ? null : false;

            assertThat(result).containsExactlyInAnyOrder(
                    buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, matched),
                    buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, matched)
            );
        }
    }

    @ParameterizedTest
    @CsvSource(nullValues = "null", textBlock = """
            true,    true
            false,   true
            true,    false
            false,   false
            true,    true
            false,   true
            true,    false
            false,   false
            """)
    void map_validatingForwardAndBackwardSegmentsAreCreatedCorrectly(
            boolean hasForwardSegment,
            boolean hasBackwardSegment) {

        when(roadSection.hasForwardSegments()).thenReturn(hasForwardSegment);
        when(roadSection.hasBackwardSegments()).thenReturn(hasBackwardSegment);

        if (hasForwardSegment || hasBackwardSegment) {
            when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        }

        if (hasForwardSegment) {
            when(roadSection.isForwardAccessible()).thenReturn(true);
            when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry))).thenReturn(lineStringJsonForward);
        }

        if (hasBackwardSegment) {
            when(roadSection.isBackwardAccessible()).thenReturn(true);
            when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry))).thenReturn(lineStringJsonBackward);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                true,
                ROAD_SECTION_ID,
                null
        );

        ArrayList<RoadSectionFeatureJson> expectedRoadSectionFeatures = new ArrayList<>();
        if (hasForwardSegment) {
            expectedRoadSectionFeatures.add(
                    buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, true));
        }

        if (hasBackwardSegment) {
            expectedRoadSectionFeatures.add(
                    buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, true));
        }

        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedRoadSectionFeatures);
    }

    @Test
    void map_matchingStartPoint() {

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
                ROAD_SECTION_ID,
                true
        );

        assertThat(result).containsExactlyInAnyOrder(
                buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, true),
                buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, true)
        );
    }

    @Test
    void map_matchingStartPoint_withFilterOutAccessibility_false() {

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
                ROAD_SECTION_ID,
                false
        );

        assertThat(result).containsExactlyInAnyOrder(
                buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, true),
                buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, true)
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
                buildRoadSectionFeature(ROAD_SECTION_ID, lineStringJsonForward, false),
                buildRoadSectionFeature(-ROAD_SECTION_ID, lineStringJsonBackward, false)
        );
    }

    private RoadSectionFeatureJson buildRoadSectionFeature(
            long roadSectionId,
            LineStringJson geometry,
            Boolean matched) {

        return RoadSectionFeatureJson
                .builder()
                .id((int) roadSectionId)
                .geometry(geometry)
                .type(TypeEnum.FEATURE)
                .properties(RoadSectionPropertiesJson.builder()
                        .accessible(true)
                        .matched(matched)
                        .build())
                .build();
    }

}
