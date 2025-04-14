package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionFeatureJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionPropertiesJson;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.routingmapmatcher.model.singlepoint.SinglePointMatch.CandidateMatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFeatureMapperTest {

    private static final long ROAD_SECTION_ID = 123L;
    @Mock
    private GeoJsonLineStringMergeMapper geoJsonLineStringMergeMapper;

    private RoadSectionFeatureMapper roadSectionFeatureMapper;

    @Mock
    private LineStringJson lineStringJson;
    @Mock
    private RoadSection roadSection;
    @Mock
    private LineString backwardGeometry;
    @Mock
    private LineString forwardGeometry;
    @Mock
    private CandidateMatch candidateMatch;

    @BeforeEach
    void setUp() {
        roadSectionFeatureMapper = new RoadSectionFeatureMapper(geoJsonLineStringMergeMapper);
    }

    @ParameterizedTest
    @MethodSource("provideFilterValues")
    void map_withForwardAccessibleAndFilters(boolean startPointRequested, Boolean accessible) {
        when(roadSection.hasForwardSegments()).thenReturn(true);
        when(roadSection.isForwardAccessible()).thenReturn(true);
        if (!Boolean.FALSE.equals(accessible) || startPointRequested) {
            when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        }
        if (!Boolean.FALSE.equals(accessible)) {
            when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry)))
                    .thenReturn(lineStringJson);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                startPointRequested,  // startPointRequested
                candidateMatch,
                accessible   // accessible
        );
        if (Boolean.FALSE.equals(accessible)) {
            assertThat(result).hasSize(0);
        } else {
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(RoadSectionFeatureJson
                    .builder()
                    .id((int) ROAD_SECTION_ID)
                    .geometry(lineStringJson)
                    .type(TypeEnum.FEATURE)
                    .properties(RoadSectionPropertiesJson.builder()
                            .accessible(true)
                            .build())
                    .build());
        }

    }

    @ParameterizedTest
    @MethodSource("provideFilterValues")
    void map_withBackwardAccessibleAndFilters(boolean startPointRequested, Boolean accessible) {
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        when(roadSection.isBackwardAccessible()).thenReturn(true);
        if (!Boolean.FALSE.equals(accessible) || startPointRequested) {
            when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
        }
        if (!Boolean.FALSE.equals(accessible)) {
            when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry)))
                    .thenReturn(lineStringJson);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                startPointRequested,
                candidateMatch,
                accessible
        );
        if (Boolean.FALSE.equals(accessible)) {
            assertThat(result).hasSize(0);
        } else {
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(RoadSectionFeatureJson
                    .builder()
                    .id((int) -ROAD_SECTION_ID)
                    .geometry(lineStringJson)
                    .type(TypeEnum.FEATURE)
                    .properties(RoadSectionPropertiesJson.builder()
                            .accessible(true)
                            .build())
                    .build());
        }

    }

    @ParameterizedTest
    @MethodSource("provideMatchedValues")
    void map_forwardWithMatched(boolean startPointRequested, Boolean matched) {
        when(roadSection.hasForwardSegments()).thenReturn(true);
        if (Boolean.TRUE.equals(matched) && startPointRequested) {
            when(candidateMatch.getMatchedLinkId())
                    .thenReturn((int) ROAD_SECTION_ID);
            when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
            when(roadSection.getForwardGeometries()).thenReturn(List.of(forwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(forwardGeometry)))
                    .thenReturn(lineStringJson);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                startPointRequested,
                Objects.isNull(matched) ? null : candidateMatch,
                true   // accessible
        );
        if (Boolean.TRUE.equals(matched) && startPointRequested) {
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(RoadSectionFeatureJson
                    .builder()
                    .id((int) ROAD_SECTION_ID)
                    .geometry(lineStringJson)
                    .type(TypeEnum.FEATURE)
                    .properties(RoadSectionPropertiesJson.builder()
                            .accessible(false)
                            .matched(true)
                            .build())
                    .build());

        }

    }

    @ParameterizedTest
    @MethodSource("provideMatchedValues")
    void map_backwardWithMatched(boolean startPointRequested, Boolean matched) {
        when(roadSection.hasBackwardSegments()).thenReturn(true);
        if (Boolean.TRUE.equals(matched) && startPointRequested) {
            when(candidateMatch.getMatchedLinkId())
                    .thenReturn((int) ROAD_SECTION_ID);
            when(candidateMatch.isReversed())
                    .thenReturn(true);
            when(roadSection.getId()).thenReturn(ROAD_SECTION_ID);
            when(roadSection.getBackwardGeometries()).thenReturn(List.of(backwardGeometry));
            when(geoJsonLineStringMergeMapper.mapToLineStringJson(List.of(backwardGeometry)))
                    .thenReturn(lineStringJson);
        }

        List<RoadSectionFeatureJson> result = roadSectionFeatureMapper.map(
                roadSection,
                startPointRequested,
                Objects.isNull(matched) ? null : candidateMatch,
                true   // accessible
        );
        if (Boolean.TRUE.equals(matched) && startPointRequested) {
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).isEqualTo(RoadSectionFeatureJson
                    .builder()
                    .id((int) -ROAD_SECTION_ID)
                    .geometry(lineStringJson)
                    .type(TypeEnum.FEATURE)
                    .properties(RoadSectionPropertiesJson.builder()
                            .accessible(false)
                            .matched(true)
                            .build())
                    .build());

        }

    }

    private static Stream<Arguments> provideFilterValues() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(false, false),
                Arguments.of(true, null),
                Arguments.of(false, null)

        );
    }

    private static Stream<Arguments> provideMatchedValues() {
        return Stream.of(
                Arguments.of(true, true),
                Arguments.of(true, false),
                Arguments.of(false, true),
                Arguments.of(false, false),
                Arguments.of(true, null),
                Arguments.of(false, null)

        );
    }
}
