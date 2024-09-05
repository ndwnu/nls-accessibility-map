package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EffectiveAccessibleDirectionalRoadSectionFragmentMapperTest {
    private static final double FRACTION_START_0 = 0;
    private static final double FRACTION_END_1 = 1;
    private static final double TRAFFIC_SIGN_FRACTION = 0.2;

    @InjectMocks
    private EffectiveAccessibleDirectionalRoadSectionFragmentMapper mapper;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSign;

    @Mock
    private DirectionalRoadSection directionalRoadSection;

    @Mock
    private DirectionalTrafficSign directionalTrafficSign;

    @Test
    void map_ok_nullReturnsEmptyList() {
        assertThat(mapper.map(null)).isEqualTo(Collections.emptyList());
    }

    @Test
    void map_ok_emptyTrafficSign() {
        when(directionalRoadSectionAndTrafficSign.getRoadSection()).thenReturn(directionalRoadSection);
        when(directionalRoadSectionAndTrafficSign.getTrafficSign()).thenReturn(null);

        when(directionalRoadSection.getAccessible()).thenReturn(Boolean.TRUE);

        assertThat(mapper.map(directionalRoadSectionAndTrafficSign))
                .isEqualTo(List.of(RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(FRACTION_END_1)
                                .data(EffectiveAccessibleDirectionalRoadSection.builder()
                                        .accessibility(true)
                                        .roadSection(directionalRoadSectionAndTrafficSign)
                                        .build())
                        .build()));
    }

    @Test
    void map_ok_forwardWithTrafficSign() {
        when(directionalRoadSectionAndTrafficSign.getRoadSection()).thenReturn(directionalRoadSection);
        when(directionalRoadSectionAndTrafficSign.getTrafficSign()).thenReturn(directionalTrafficSign);
        when(directionalRoadSection.getDirection()).thenReturn(Direction.FORWARD);
        when(directionalTrafficSign.getNwbFraction()).thenReturn(TRAFFIC_SIGN_FRACTION);

        assertThat(mapper.map(directionalRoadSectionAndTrafficSign))
                .isEqualTo(List.of(
                        RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                        .fromFraction(FRACTION_START_0)
                        .toFraction(TRAFFIC_SIGN_FRACTION)
                        .data(EffectiveAccessibleDirectionalRoadSection.builder()
                                .accessibility(true)
                                .roadSection(directionalRoadSectionAndTrafficSign)
                                .build())
                        .build(),
                        RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                        .fromFraction(TRAFFIC_SIGN_FRACTION)
                        .toFraction(FRACTION_END_1)
                        .data(EffectiveAccessibleDirectionalRoadSection.builder()
                                .accessibility(false)
                                .roadSection(directionalRoadSectionAndTrafficSign)
                                .build())
                        .build()));
    }

    @Test
    void map_ok_backwardWithTrafficSignAccessibleStateInOppositeDirection() {
        when(directionalRoadSectionAndTrafficSign.getRoadSection()).thenReturn(directionalRoadSection);
        when(directionalRoadSectionAndTrafficSign.getTrafficSign()).thenReturn(directionalTrafficSign);
        when(directionalRoadSection.getDirection()).thenReturn(Direction.BACKWARD);
        when(directionalTrafficSign.getNwbFraction()).thenReturn(TRAFFIC_SIGN_FRACTION);

        assertThat(mapper.map(directionalRoadSectionAndTrafficSign))
                .isEqualTo(List.of(
                        RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                                .fromFraction(FRACTION_START_0)
                                .toFraction(TRAFFIC_SIGN_FRACTION)
                                .data(EffectiveAccessibleDirectionalRoadSection.builder()
                                        .accessibility(false) // inverse of forward direction
                                        .roadSection(directionalRoadSectionAndTrafficSign)
                                        .build())
                                .build(),
                        RoadSectionFragment.<EffectiveAccessibleDirectionalRoadSection>builder()
                                .fromFraction(TRAFFIC_SIGN_FRACTION)
                                .toFraction(FRACTION_END_1)
                                .data(EffectiveAccessibleDirectionalRoadSection.builder()
                                        .accessibility(true) // inverse of forward direction
                                        .roadSection(directionalRoadSectionAndTrafficSign)
                                        .build())
                                .build()));
    }
}