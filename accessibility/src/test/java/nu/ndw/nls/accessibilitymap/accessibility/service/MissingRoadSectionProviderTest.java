package nu.ndw.nls.accessibilitymap.accessibility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NwbNetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.springboot.test.util.annotation.AnnotationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MissingRoadSectionProviderTest {

    private static final int ROAD_SECTION_ID_ONE = 1;

    private static final int ROAD_SECTION_ID_TWO = 12;

    private static final int ROAD_SECTION_ID_THREE = 45648;

    private static final int ROAD_SECTION_ID_FOUR = 45649;

    private MissingRoadSectionProvider missingRoadSectionProvider;

    @Mock
    private NetworkData networkData;

    @Mock
    private NwbNetworkData nwbNetworkData;

    @Mock
    private BBox searchArea;

    private LineString roadSection2LineString;

    private LineString roadSection3LineString;

    private RoadSection roadSectionExisting;

    @BeforeEach
    void setUp() {

        GeometryFactory geometryFactory = new GeometryFactory();
        LineString roadSection1LineString = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(0, 0), new Coordinate(
                ROAD_SECTION_ID_ONE,
                ROAD_SECTION_ID_ONE)});
        roadSection2LineString = geometryFactory.createLineString(new Coordinate[]{new Coordinate(2, 2), new Coordinate(3, 3)});
        roadSection3LineString = geometryFactory.createLineString(new Coordinate[]{new Coordinate(4, 4), new Coordinate(5, 5)});

        roadSectionExisting = RoadSection.builder()
                .id(1L)
                .roadSectionFragments(List.of(
                        RoadSectionFragment.builder()
                                .id(ROAD_SECTION_ID_ONE)
                                .forwardSegment(DirectionalSegment.builder()
                                        .id(ROAD_SECTION_ID_ONE)
                                        .lineString(roadSection1LineString)
                                        .build())
                                .backwardSegment(DirectionalSegment.builder()
                                        .id(2)
                                        .lineString(roadSection1LineString.reverse())
                                        .build())
                                .build()
                ))
                .build();

        missingRoadSectionProvider = new MissingRoadSectionProvider();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void findAll(boolean isAccessible) {

        when(networkData.getNwbNetworkData()).thenReturn(nwbNetworkData);
        when(nwbNetworkData.findAllAccessibilityNwbRoadSectionByMunicipalityId(123)).thenReturn(buildRoadSections(true, true));
        when(searchArea.intersects(PointList.from(roadSection2LineString))).thenReturn(true);
        when(searchArea.intersects(PointList.from(roadSection3LineString))).thenReturn(false);
        when(networkData.findGeometryInNetwork(ROAD_SECTION_ID_THREE)).thenReturn(Optional.of(roadSection2LineString));
        when(networkData.findGeometryInNetwork(ROAD_SECTION_ID_FOUR)).thenReturn(Optional.of(roadSection3LineString));

        Collection<RoadSection> missingRoadSections = missingRoadSectionProvider.findAll(
                networkData,
                123,
                List.of(roadSectionExisting),
                isAccessible,
                searchArea);

        validateMissingRoadSections(missingRoadSections, isAccessible, true, true);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true
            false, true
            true, false
            false, false
            """)
    void findAll_validateAllDirections(boolean hasForwardSection, boolean hasBackwardSection) {

        when(networkData.getNwbNetworkData()).thenReturn(nwbNetworkData);
        when(nwbNetworkData.findAllAccessibilityNwbRoadSectionByMunicipalityId(123)).thenReturn(buildRoadSections(
                hasForwardSection,
                hasBackwardSection));
        when(networkData.findGeometryInNetwork(ROAD_SECTION_ID_THREE)).thenReturn(Optional.of(roadSection2LineString));
        when(networkData.findGeometryInNetwork(ROAD_SECTION_ID_FOUR)).thenReturn(Optional.of(roadSection3LineString));
        when(searchArea.intersects(PointList.from(roadSection2LineString))).thenReturn(true);
        when(searchArea.intersects(PointList.from(roadSection3LineString))).thenReturn(false);

        Collection<RoadSection> missingRoadSections = missingRoadSectionProvider.findAll(
                networkData,
                123,
                List.of(roadSectionExisting),
                true,
                searchArea);

        validateMissingRoadSections(missingRoadSections, true, hasForwardSection, hasBackwardSection);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true,
            false
            """)
    void findAll_noMunicipalityGiven_shouldFindAllAllRoadSections(boolean isAccessible) {

        when(networkData.getNwbNetworkData()).thenReturn(nwbNetworkData);
        when(nwbNetworkData.findAllAccessibilityNwbRoadSections()).thenReturn(buildRoadSections(true, true));
        when(networkData.findGeometryInNetwork(ROAD_SECTION_ID_THREE)).thenReturn(Optional.of(roadSection2LineString));
        when(networkData.findGeometryInNetwork(ROAD_SECTION_ID_FOUR)).thenReturn(Optional.of(roadSection3LineString));
        when(searchArea.intersects(PointList.from(roadSection2LineString))).thenReturn(true);
        when(searchArea.intersects(PointList.from(roadSection3LineString))).thenReturn(false);

        Collection<RoadSection> missingRoadSections = missingRoadSectionProvider.findAll(
                networkData,
                null,
                List.of(roadSectionExisting),
                isAccessible,
                searchArea);

        validateMissingRoadSections(missingRoadSections, isAccessible, true, true);
    }

    private List<AccessibilityNwbRoadSection> buildRoadSections(boolean hasForwardSection, boolean hasBackwardSection) {
        return List.of(
                new AccessibilityNwbRoadSection(ROAD_SECTION_ID_ONE, 2, 3, 4, null, true, true, CarriagewayTypeCode.RB, "1", "WS14"),
                new AccessibilityNwbRoadSection(ROAD_SECTION_ID_TWO, 23, 34, 4, null, true, true, CarriagewayTypeCode.FP, "1", "WS14"),
                new AccessibilityNwbRoadSection(
                        ROAD_SECTION_ID_THREE,
                        22,
                        23,
                        24,
                        null,
                        hasForwardSection,
                        hasBackwardSection,
                        CarriagewayTypeCode.RB,
                        "1",
                        "WS14"),
                new AccessibilityNwbRoadSection(ROAD_SECTION_ID_FOUR, 32, 33, 34, null, true, true, CarriagewayTypeCode.RB, "1", "WS14")
        );
    }

    @SuppressWarnings("unchecked")
    private void validateMissingRoadSections(
            Collection<RoadSection> missingRoadSections,
            boolean isAccessible,
            boolean hasForwardSection,
            boolean hasBackwardSection
    ) {
        assertThat(missingRoadSections).hasSize(1);

        RoadSection roadSection = missingRoadSections.stream().toList().getFirst();
        assertThat(roadSection.getId()).isEqualTo(45648L);
        assertThat(roadSection.getFunctionalRoadClass()).isEqualTo("1");
        assertThat(roadSection.getRoadSectionFragments()).hasSize(1);

        RoadSectionFragment roadSectionFragment = roadSection.getRoadSectionFragments().getFirst();
        assertThat(roadSectionFragment.getId()).isEqualTo(2);
        assertThat(roadSectionFragment.getRoadSection()).isEqualTo(roadSection);

        if (hasForwardSection) {
            assertThat(roadSectionFragment.getForwardSegment()).isNotNull();
            assertThat(roadSectionFragment.getForwardSegment().getId()).isEqualTo(3);
            assertThat(roadSectionFragment.getForwardSegment().getDirection()).isEqualTo(Direction.FORWARD);
            assertThat(roadSectionFragment.getForwardSegment().getDirection()).isEqualTo(Direction.FORWARD);
            assertThat(roadSectionFragment.getForwardSegment().getLineString()).isEqualTo(roadSection2LineString);
            assertThat(roadSectionFragment.getForwardSegment().getRestrictions()).isNull();
            assertThat(roadSectionFragment.getForwardSegment().isAccessible()).isEqualTo(isAccessible);
            assertThat(roadSectionFragment.getForwardSegment().getRoadSectionFragment()).isEqualTo(roadSectionFragment);
        } else {
            assertThat(roadSectionFragment.getForwardSegment()).isNull();
        }

        if (hasBackwardSection) {
            assertThat(roadSectionFragment.getBackwardSegment().getId()).isEqualTo(hasForwardSection ? 4 : 3);
            assertThat(roadSectionFragment.getBackwardSegment().getDirection()).isEqualTo(Direction.BACKWARD);
            assertThat(roadSectionFragment.getBackwardSegment().getLineString()).isEqualTo(roadSection2LineString.reverse());
            assertThat(roadSectionFragment.getBackwardSegment().getRestrictions()).isNull();
            assertThat(roadSectionFragment.getBackwardSegment().isAccessible()).isEqualTo(isAccessible);
            assertThat(roadSectionFragment.getBackwardSegment().getRoadSectionFragment()).isEqualTo(roadSectionFragment);
        } else {
            assertThat(roadSectionFragment.getBackwardSegment()).isNull();
        }
    }

    @Test
    void findAll_containsTimeAnnotation() {

        AnnotationUtil.methodContainsAnnotation(
                missingRoadSectionProvider.getClass(),
                Timed.class,
                "findAll",
                annotation -> {
                    assertThat(annotation).isNotNull();
                    assertThat(annotation.value()).isEqualTo("accessibilitymap.accessibility.calculateMissingRoadSections");
                }
        );
    }

    @Test
    void findAll_no_geometry_exception() {
        when(networkData.getNwbNetworkData()).thenReturn(nwbNetworkData);
        when(nwbNetworkData.findAllAccessibilityNwbRoadSections()).thenReturn(buildRoadSections(true, true));
        when(networkData.findGeometryInNetwork(ROAD_SECTION_ID_THREE)).thenReturn(Optional.empty());
        var existingRoadSections = List.of(roadSectionExisting);
        assertThatThrownBy(() -> missingRoadSectionProvider.findAll(networkData, null, existingRoadSections, true, searchArea))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Road section geometry not found for road section id: 45648");
    }
}
