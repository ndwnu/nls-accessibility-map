package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalTrafficSign;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DirectionalRoadSectionSplitAtTrafficSignServiceTest {

    private static final double FRACTION = 0.2;
    private static final double END_FRACTION = 1;
    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @InjectMocks
    private DirectionalRoadSectionSplitAtTrafficSignService directionalRoadSectionSplitAtTrafficSignService;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSign;

    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignWithRoadSectionA;
    @Mock
    private DirectionalRoadSectionAndTrafficSign directionalRoadSectionAndTrafficSignWithRoadSectionB;


    @Mock
    private DirectionalRoadSection directionalRoadSection;
    @Mock
    private DirectionalRoadSection directionalRoadSectionWithAccessibleA;
    @Mock
    private DirectionalRoadSection directionalRoadSectionWithNwbGeometryA;

    @Mock
    private DirectionalRoadSection directionalRoadSectionWithAccessibleB;
    @Mock
    private DirectionalRoadSection directionalRoadSectionWithNwbGeometryB;
    @Mock
    private DirectionalTrafficSign directionalTrafficSign;

    @Mock
    private LineString nwbGeometry;

    @Mock
    private LineString startGeometry;

    @Mock
    private LineString endGeometry;

    @Test
    void split_ok_noTrafficSignResultsInListOfOriginal() {
        when(directionalRoadSectionAndTrafficSign.getTrafficSign()).thenReturn(null);
        assertThat(directionalRoadSectionSplitAtTrafficSignService.split(directionalRoadSectionAndTrafficSign))
                .isEqualTo(List.of(directionalRoadSectionAndTrafficSign));
    }

    @Test
    void split_ok_splitInForwardDirection() {
        when(directionalRoadSectionAndTrafficSign.getRoadSection()).thenReturn(directionalRoadSection);
        when(directionalRoadSectionAndTrafficSign.getTrafficSign()).thenReturn(directionalTrafficSign);

        when(directionalRoadSection.getNwbGeometry()).thenReturn(nwbGeometry);
        when(directionalTrafficSign.getNwbFraction()).thenReturn(FRACTION);

        when(fractionAndDistanceCalculator.getSubLineString(nwbGeometry, FRACTION)).thenReturn(startGeometry);
        when(fractionAndDistanceCalculator.getSubLineString(nwbGeometry, FRACTION, END_FRACTION))
                .thenReturn(endGeometry);

        when(directionalRoadSection.getDirection()).thenReturn(Direction.FORWARD);

        when(directionalRoadSection.withAccessible(true)).thenReturn(directionalRoadSectionWithAccessibleA);
        when(directionalRoadSectionWithAccessibleA.withNwbGeometry(startGeometry))
                .thenReturn(directionalRoadSectionWithNwbGeometryA);
        when(directionalRoadSectionAndTrafficSign.withRoadSection(directionalRoadSectionWithNwbGeometryA))
                .thenReturn(directionalRoadSectionAndTrafficSignWithRoadSectionA);


        when(directionalRoadSection.withAccessible(false)).thenReturn(directionalRoadSectionWithAccessibleB);
        when(directionalRoadSectionWithAccessibleB.withNwbGeometry(endGeometry))
                .thenReturn(directionalRoadSectionWithNwbGeometryB);
        when(directionalRoadSectionAndTrafficSign.withRoadSection(directionalRoadSectionWithNwbGeometryB))
                .thenReturn(directionalRoadSectionAndTrafficSignWithRoadSectionB);

        assertThat(directionalRoadSectionSplitAtTrafficSignService.split(directionalRoadSectionAndTrafficSign))
                .isEqualTo(List.of(directionalRoadSectionAndTrafficSignWithRoadSectionA,
                        directionalRoadSectionAndTrafficSignWithRoadSectionB));
    }


    @Test
    void split_ok_splitInBackwardDirection() {
        when(directionalRoadSectionAndTrafficSign.getRoadSection()).thenReturn(directionalRoadSection);
        when(directionalRoadSectionAndTrafficSign.getTrafficSign()).thenReturn(directionalTrafficSign);

        when(directionalRoadSection.getNwbGeometry()).thenReturn(nwbGeometry);
        when(directionalTrafficSign.getNwbFraction()).thenReturn(FRACTION);

        when(fractionAndDistanceCalculator.getSubLineString(nwbGeometry, FRACTION)).thenReturn(startGeometry);
        when(fractionAndDistanceCalculator.getSubLineString(nwbGeometry, FRACTION, END_FRACTION))
                .thenReturn(endGeometry);

        when(directionalRoadSection.getDirection()).thenReturn(Direction.BACKWARD);

        // in backward direction, the first part of the original nwb geometry is not accessible as it is behind
        // the traffic sign
        when(directionalRoadSection.withAccessible(false)).thenReturn(directionalRoadSectionWithAccessibleA);
        when(directionalRoadSectionWithAccessibleA.withNwbGeometry(startGeometry))
                .thenReturn(directionalRoadSectionWithNwbGeometryA);
        when(directionalRoadSectionAndTrafficSign.withRoadSection(directionalRoadSectionWithNwbGeometryA))
                .thenReturn(directionalRoadSectionAndTrafficSignWithRoadSectionA);


        // in forward direction, the second part of the original nwb geometry is accessible as it is in front of
        // the traffic sign
        when(directionalRoadSection.withAccessible(true)).thenReturn(directionalRoadSectionWithAccessibleB);
        when(directionalRoadSectionWithAccessibleB.withNwbGeometry(endGeometry))
                .thenReturn(directionalRoadSectionWithNwbGeometryB);
        when(directionalRoadSectionAndTrafficSign.withRoadSection(directionalRoadSectionWithNwbGeometryB))
                .thenReturn(directionalRoadSectionAndTrafficSignWithRoadSectionB);

        assertThat(directionalRoadSectionSplitAtTrafficSignService.split(directionalRoadSectionAndTrafficSign))
                .isEqualTo(List.of(directionalRoadSectionAndTrafficSignWithRoadSectionA,
                        directionalRoadSectionAndTrafficSignWithRoadSectionB));
    }
}
