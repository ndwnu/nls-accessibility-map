package nu.ndw.nls.accessibilitymap.accessibility.services.accessibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibilityRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibleRoadSectionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MissingRoadSectionProviderTest {

    private MissingRoadSectionProvider missingRoadSectionProvider;

    @Mock
    private AccessibleRoadSectionsService accessibleRoadSectionsService;

    @Mock
    private LineString roadSection1LineString;

    @Mock
    private LineString roadSection1LineStringBackwards;

    @Mock
    private LineString roadSection2LineString;

    @Mock
    private LineString roadSection2LineStringBackwards;

    private RoadSection roadSectionExisting;

    @BeforeEach
    void setUp() {

        roadSectionExisting = RoadSection.builder()
                .id(1L)
                .roadSectionFragments(List.of(
                        RoadSectionFragment.builder()
                                .id(1)
                                .forwardSegment(DirectionalSegment.builder()
                                        .id(1)
                                        .lineString(roadSection1LineString)
                                        .build())
                                .backwardSegment(DirectionalSegment.builder()
                                        .id(2)
                                        .lineString(roadSection1LineStringBackwards)
                                        .build())
                                .build()
                ))
                .build();

        missingRoadSectionProvider = new MissingRoadSectionProvider(accessibleRoadSectionsService);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void get(boolean isAccessible) {

        when(roadSection2LineString.reverse()).thenReturn(roadSection2LineStringBackwards);

        when(accessibleRoadSectionsService.getRoadSectionsByMunicipalityId(123)).thenReturn(List.of(
                AccessibilityRoadSection.builder()
                        .roadSectionId(1)
                        .backwardAccessible(true)
                        .forwardAccessible(true)
                        .geometry(roadSection1LineString)
                        .build(),
                AccessibilityRoadSection.builder()
                        .roadSectionId(45648)
                        .backwardAccessible(true)
                        .forwardAccessible(true)
                        .geometry(roadSection2LineString)
                        .build()
        ));

        List<RoadSection> missingRoadSections = missingRoadSectionProvider.get(123, List.of(roadSectionExisting), isAccessible);

        assertThat(missingRoadSections).hasSize(1);

        RoadSection roadSection = missingRoadSections.get(0);
        assertThat(roadSection.getId()).isEqualTo(45648L);
        assertThat(roadSection.getRoadSectionFragments()).hasSize(1);

        RoadSectionFragment roadSectionFragment = roadSection.getRoadSectionFragments().get(0);
        assertThat(roadSectionFragment.getId()).isEqualTo(2);
        assertThat(roadSectionFragment.getRoadSection()).isEqualTo(roadSection);

        assertThat(roadSectionFragment.getForwardSegment().getId()).isEqualTo(3);
        assertThat(roadSectionFragment.getForwardSegment().getDirection()).isEqualTo(Direction.FORWARD);
        assertThat(roadSectionFragment.getForwardSegment().getDirection()).isEqualTo(Direction.FORWARD);
        assertThat(roadSectionFragment.getForwardSegment().getLineString()).isEqualTo(roadSection2LineString);
        assertThat(roadSectionFragment.getForwardSegment().getTrafficSigns()).isNull();
        assertThat(roadSectionFragment.getForwardSegment().isAccessible()).isEqualTo(isAccessible);
        assertThat(roadSectionFragment.getForwardSegment().getRoadSectionFragment()).isEqualTo(roadSectionFragment);

        assertThat(roadSectionFragment.getBackwardSegment().getId()).isEqualTo(4);
        assertThat(roadSectionFragment.getBackwardSegment().getDirection()).isEqualTo(Direction.BACKWARD);
        assertThat(roadSectionFragment.getBackwardSegment().getLineString()).isEqualTo(roadSection2LineStringBackwards);
        assertThat(roadSectionFragment.getBackwardSegment().getTrafficSigns()).isNull();
        assertThat(roadSectionFragment.getBackwardSegment().isAccessible()).isEqualTo(isAccessible);
        assertThat(roadSectionFragment.getBackwardSegment().getRoadSectionFragment()).isEqualTo(roadSectionFragment);

    }
}