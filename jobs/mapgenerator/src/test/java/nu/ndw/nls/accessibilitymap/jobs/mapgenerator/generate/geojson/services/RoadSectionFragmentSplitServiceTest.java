package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.EffectiveAccessibleDirectionalRoadSections;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.directional.DirectionalRoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionFragmentSplitServiceTest {

    private static final double FROM_FRACTION = 0.2;
    private static final double TO_FRACTION = 0.5;
    private static final Boolean EFFECTIVE_ACCESSIBILITY = Boolean.TRUE;
    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @InjectMocks
    private RoadSectionFragmentSplitService roadSectionFragmentSplitService;

    @Mock
    private RoadSectionFragment<EffectiveAccessibleDirectionalRoadSections> effectiveAccessibleRoadSectionsFragment;

    @Mock
    private EffectiveAccessibleDirectionalRoadSections data;

    @Mock
    private DirectionalRoadSectionAndTrafficSign backward;

    @Mock
    private DirectionalRoadSectionAndTrafficSign forward;

    @Mock
    private DirectionalRoadSection backwardRoadSection;

    @Mock
    private DirectionalRoadSection forwardRoadSection;

    @Mock
    private LineString lineString;

    @Mock
    private LineString trimmedLineString;

    @Mock
    private DirectionalRoadSection backwardRoadSectionWithGeometry;
    @Mock
    private DirectionalRoadSection backwardRoadSectionWithGeometryWithAccessibility;

    @Mock
    private DirectionalRoadSection forwardRoadSectionWithGeometry;
    @Mock
    private DirectionalRoadSection forwardRoadSectionWithGeometryWithAccessibility;

    @Mock
    private DirectionalRoadSectionAndTrafficSign backwardWithTrimmedRoadSection;
    @Mock
    private DirectionalRoadSectionAndTrafficSign forwardWithTrimmedRoadSection;

    @Test
    void split_ok_bothDirections() {

        when(effectiveAccessibleRoadSectionsFragment.getData()).thenReturn(data);

        when(data.getAccessibility()).thenReturn(EFFECTIVE_ACCESSIBILITY);
        when(data.getBackward()).thenReturn(backward);
        when(data.getForward()).thenReturn(forward);


        when(backward.getRoadSection()).thenReturn(backwardRoadSection);
        when(forward.getRoadSection()).thenReturn(forwardRoadSection);

        when(effectiveAccessibleRoadSectionsFragment.getFromFraction()).thenReturn(FROM_FRACTION);
        when(effectiveAccessibleRoadSectionsFragment.getToFraction()).thenReturn(TO_FRACTION);

        when(backwardRoadSection.getNwbGeometry()).thenReturn(lineString);

        when(fractionAndDistanceCalculator.getSubLineString(lineString, FROM_FRACTION,TO_FRACTION))
                .thenReturn(trimmedLineString);

        when(backwardRoadSection.withNwbGeometry(trimmedLineString)).thenReturn(backwardRoadSectionWithGeometry);
        when(backwardRoadSectionWithGeometry.withAccessible(EFFECTIVE_ACCESSIBILITY))
                .thenReturn(backwardRoadSectionWithGeometryWithAccessibility);

        when(forwardRoadSection.withNwbGeometry(trimmedLineString)).thenReturn(forwardRoadSectionWithGeometry);
        when(forwardRoadSectionWithGeometry.withAccessible(EFFECTIVE_ACCESSIBILITY))
                .thenReturn(forwardRoadSectionWithGeometryWithAccessibility);


        when(backward.withRoadSection(backwardRoadSectionWithGeometryWithAccessibility))
                .thenReturn(backwardWithTrimmedRoadSection);
        when(forward.withRoadSection(forwardRoadSectionWithGeometryWithAccessibility))
                .thenReturn(forwardWithTrimmedRoadSection);

        assertThat(roadSectionFragmentSplitService.split(effectiveAccessibleRoadSectionsFragment))
                .isEqualTo(List.of(backwardWithTrimmedRoadSection, forwardWithTrimmedRoadSection));
    }

    @Test
    void split_ok_backward() {
        when(effectiveAccessibleRoadSectionsFragment.getData()).thenReturn(data);

        when(data.getAccessibility()).thenReturn(EFFECTIVE_ACCESSIBILITY);
        when(data.getBackward()).thenReturn(backward);
        // forward to null
        when(data.getForward()).thenReturn(null);

        when(backward.getRoadSection()).thenReturn(backwardRoadSection);

        when(effectiveAccessibleRoadSectionsFragment.getFromFraction()).thenReturn(FROM_FRACTION);
        when(effectiveAccessibleRoadSectionsFragment.getToFraction()).thenReturn(TO_FRACTION);

        when(backwardRoadSection.getNwbGeometry()).thenReturn(lineString);

        when(fractionAndDistanceCalculator.getSubLineString(lineString, FROM_FRACTION,TO_FRACTION))
                .thenReturn(trimmedLineString);

        when(backwardRoadSection.withNwbGeometry(trimmedLineString)).thenReturn(backwardRoadSectionWithGeometry);
        when(backwardRoadSectionWithGeometry.withAccessible(EFFECTIVE_ACCESSIBILITY))
                .thenReturn(backwardRoadSectionWithGeometryWithAccessibility);

        when(backward.withRoadSection(backwardRoadSectionWithGeometryWithAccessibility))
                .thenReturn(backwardWithTrimmedRoadSection);

        assertThat(roadSectionFragmentSplitService.split(effectiveAccessibleRoadSectionsFragment))
                .isEqualTo(List.of(backwardWithTrimmedRoadSection));
    }

    @Test
    void split_ok_forward() {

        when(effectiveAccessibleRoadSectionsFragment.getData()).thenReturn(data);

        when(data.getAccessibility()).thenReturn(EFFECTIVE_ACCESSIBILITY);
        // backwards to null
        when(data.getBackward()).thenReturn(null);
        when(data.getForward()).thenReturn(forward);

        when(forward.getRoadSection()).thenReturn(forwardRoadSection);

        when(effectiveAccessibleRoadSectionsFragment.getFromFraction()).thenReturn(FROM_FRACTION);
        when(effectiveAccessibleRoadSectionsFragment.getToFraction()).thenReturn(TO_FRACTION);

        // If backwards is null, the geometry is obtained from the forward
        when(forwardRoadSection.getNwbGeometry()).thenReturn(lineString);

        when(fractionAndDistanceCalculator.getSubLineString(lineString, FROM_FRACTION,TO_FRACTION))
                .thenReturn(trimmedLineString);

        when(forwardRoadSection.withNwbGeometry(trimmedLineString)).thenReturn(forwardRoadSectionWithGeometry);
        when(forwardRoadSectionWithGeometry.withAccessible(EFFECTIVE_ACCESSIBILITY))
                .thenReturn(forwardRoadSectionWithGeometryWithAccessibility);

        when(forward.withRoadSection(forwardRoadSectionWithGeometryWithAccessibility))
                .thenReturn(forwardWithTrimmedRoadSection);

        assertThat(roadSectionFragmentSplitService.split(effectiveAccessibleRoadSectionsFragment))
                .isEqualTo(List.of(forwardWithTrimmedRoadSection));
    }

    @Test
    void split_fail_noDirections() {

        when(effectiveAccessibleRoadSectionsFragment.getData()).thenReturn(data);

        // backwards and forwards to null
        when(data.getBackward()).thenReturn(null);
        when(data.getForward()).thenReturn(null);

        assertThatThrownBy(() -> roadSectionFragmentSplitService.split(effectiveAccessibleRoadSectionsFragment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("At least one of forward or backward must be provided");
    }

}