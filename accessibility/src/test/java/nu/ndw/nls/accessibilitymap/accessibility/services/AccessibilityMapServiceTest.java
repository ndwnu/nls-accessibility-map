package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.SortedMap;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.accessibility.model.AccessibleRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.Municipality;
import nu.ndw.nls.accessibilitymap.accessibility.model.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.AccessibilityMapService.ResultType;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityMapServiceTest {

    private static final double SEARCH_DISTANCE_IN_METERS = 1000D;
    private static final String MUNICIPALITY_ID_STRING = "GM307";

    private static final int ID_1 = 1;
    private static final int ID_2 = 2;
    private static final int ID_3 = 3;

    private static final GeometryFactoryWgs84 WGS84_GEOMETRY_FACTORY = new GeometryFactoryWgs84();
    private static final LineString LINE_STRING_1 = WGS84_GEOMETRY_FACTORY.createLineString(new Coordinate[]{
            new Coordinate(1.0, 1.0), new Coordinate(2.0, 2.0)});
    private static final LineString LINE_STRING_2 = WGS84_GEOMETRY_FACTORY.createLineString(new Coordinate[]{
            new Coordinate(2.0, 2.0), new Coordinate(3.0, 3.0)});
    private static final LineString LINE_STRING_3 = WGS84_GEOMETRY_FACTORY.createLineString(new Coordinate[]{
            new Coordinate(3.0, 3.0), new Coordinate(3.0, 3.0)});

    private static final int MUNICIPALITY_ID_INTEGER = 307;

    @Mock
    private AccessibilityMapFactory accessibilityMapFactory;
    @Mock
    private NetworkGraphHopper networkGraphHopper;
    @Mock
    private MunicipalityService municipalityService;
    @Mock
    private AccessibleRoadsService accessibleRoadsService;

    @Mock
    private AccessibilityMap accessibilityMap;
    @Mock
    private Municipality municipality;
    @Mock
    private VehicleProperties vehicleProperties;
    @Mock
    private AccessibleRoadSectionsService accessibleRoadSectionsService;
    @InjectMocks
    private AccessibilityMapService accessibilityMapService;

    @Mock
    private Point startPoint;


    private final AccessibleRoadSection NWB_ACCESSIBLE_BOTH = new AccessibleRoadSection(ID_1, LINE_STRING_1, true, true);

    private final AccessibleRoadSection NEW_ACCESSIBLE_FORWARD = new AccessibleRoadSection(ID_2, LINE_STRING_2, true, false);

    private final AccessibleRoadSection NEW_ACCESSIBLE_REVERSED = new AccessibleRoadSection(ID_3, LINE_STRING_3, false, true);

    private static final IsochroneMatch ACCESSIBLE_MATCH = IsochroneMatch.builder()
            .matchedLinkId(ID_1)
            .geometry(LINE_STRING_1)
            .reversed(false)
            .build();
    private static final IsochroneMatch INACCESSIBLE_MATCH = IsochroneMatch.builder()
            .matchedLinkId(ID_2)
            .geometry(LINE_STRING_2)
            .reversed(false)
            .build();
    private static final IsochroneMatch INACCESSIBLE_MATCH_REVERSED = IsochroneMatch.builder()
            .matchedLinkId(ID_2)
            .geometry(LINE_STRING_2.reverse())
            .reversed(true)
            .build();

    @Test
    void determineAccessibilityByRoadSection_ok_byMunicipalityDifferenceOfAddedRestrictions() {
        // Backward is processed before forward, but result should still contain forward geometry.
        List<IsochroneMatch> allIsochroneMatches = List.of(ACCESSIBLE_MATCH, INACCESSIBLE_MATCH_REVERSED,
                INACCESSIBLE_MATCH);
        List<IsochroneMatch> restrictedIsochroneMatches = List.of(INACCESSIBLE_MATCH, INACCESSIBLE_MATCH_REVERSED);

        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING)).thenReturn(municipality);
        when(accessibleRoadsService.getBaseAccessibleRoadsByMunicipality(accessibilityMap, municipality))
                .thenReturn(allIsochroneMatches);
        when(accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties,
                municipality)).thenReturn(restrictedIsochroneMatches);

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID_STRING,
                        ResultType.DIFFERENCE_OF_ADDED_RESTRICTIONS);

        assertThat(idToRoadSections).hasSize(2)
                .containsEntry(ID_1, new RoadSection(ID_1, LINE_STRING_1, false, null))
                .containsEntry(ID_2, new RoadSection(ID_2, LINE_STRING_2, true, true));
    }

    @Test
    void determineAccessibilityByRoadSection_ok_byStartPointAndDistanceDifferenceOfAddedRestrictions() {
        // Backward is processed before forward, but result should still contain forward geometry.
        List<IsochroneMatch> allIsochroneMatches = List.of(ACCESSIBLE_MATCH, INACCESSIBLE_MATCH_REVERSED,
                INACCESSIBLE_MATCH);
        List<IsochroneMatch> restrictedIsochroneMatches = List.of(INACCESSIBLE_MATCH, INACCESSIBLE_MATCH_REVERSED);

        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(accessibleRoadsService.getBaseAccessibleRoads(accessibilityMap, startPoint, SEARCH_DISTANCE_IN_METERS))
                .thenReturn(allIsochroneMatches);
        when(accessibleRoadsService.getVehicleAccessibleRoads(accessibilityMap, vehicleProperties, startPoint,
                SEARCH_DISTANCE_IN_METERS)).thenReturn(restrictedIsochroneMatches);

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, startPoint, SEARCH_DISTANCE_IN_METERS,
                        ResultType.DIFFERENCE_OF_ADDED_RESTRICTIONS);

        assertThat(idToRoadSections).hasSize(2)
                .containsEntry(ID_1, new RoadSection(ID_1, LINE_STRING_1, false, null))
                .containsEntry(ID_2, new RoadSection(ID_2, LINE_STRING_2, true, true));
    }


    @Test
    void determineAccessibilityByRoadSection_ok_byStartPointAndDistanceEffectiveAccessibilityAllAccessibleIsochroneEffectivelyAccessible() {
        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);

        when(accessibleRoadSectionsService.getRoadSections())
                .thenReturn(List.of(NWB_ACCESSIBLE_BOTH, NEW_ACCESSIBLE_FORWARD, NEW_ACCESSIBLE_REVERSED));

        when(accessibleRoadsService.getVehicleAccessibleRoads(accessibilityMap, vehicleProperties,
                startPoint, SEARCH_DISTANCE_IN_METERS)).thenReturn(List.of(IsochroneMatch.builder()
                        .matchedLinkId(ID_1)
                        .geometry(LINE_STRING_1)
                        .reversed(false)
                        .build(),
                IsochroneMatch.builder()
                        .matchedLinkId(ID_1)
                        .geometry(LINE_STRING_1)
                        .reversed(true)
                        .build(),
                IsochroneMatch.builder()
                        .matchedLinkId(ID_2)
                        .geometry(LINE_STRING_2)
                        .reversed(false)
                        .build(),
                IsochroneMatch.builder()
                        .matchedLinkId(ID_3)
                        .geometry(LINE_STRING_3)
                        .reversed(true)
                        .build()));

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, startPoint, SEARCH_DISTANCE_IN_METERS,
                        ResultType.EFFECTIVE_ACCESSIBILITY);

        assertThat(idToRoadSections).hasSize(3)
                .containsEntry(ID_1, new RoadSection(ID_1, LINE_STRING_1, true, true))
                .containsEntry(ID_2, new RoadSection(ID_2, LINE_STRING_2, true, null))
                .containsEntry(ID_3, new RoadSection(ID_3, LINE_STRING_3, null, true));
    }

    @Test
    void determineAccessibilityByRoadSection_ok_noAccessibleIsochroneSectionsEffectivelyAccessible() {
        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING)).thenReturn(municipality);
        when(municipality.getMunicipalityIdInteger()).thenReturn(MUNICIPALITY_ID_INTEGER);

        when(accessibleRoadSectionsService.getRoadSectionIdToRoadSection(MUNICIPALITY_ID_INTEGER))
                .thenReturn(List.of(NWB_ACCESSIBLE_BOTH, NEW_ACCESSIBLE_FORWARD, NEW_ACCESSIBLE_REVERSED));

        when(accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties,
                municipality)).thenReturn(List.of());

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID_STRING,
                        ResultType.EFFECTIVE_ACCESSIBILITY);

        assertThat(idToRoadSections).hasSize(3)
                .containsEntry(ID_1, new RoadSection(ID_1, LINE_STRING_1, false, false))
                .containsEntry(ID_2, new RoadSection(ID_2, LINE_STRING_2, false, null))
                .containsEntry(ID_3, new RoadSection(ID_3, LINE_STRING_3, null, false));
    }

    @Test
    void determineAccessibilityByRoadSection_ok_allAccessibleIsochroneEffectivelyAccessible() {
        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING)).thenReturn(municipality);
        when(municipality.getMunicipalityIdInteger()).thenReturn(MUNICIPALITY_ID_INTEGER);

        when(accessibleRoadSectionsService.getRoadSectionIdToRoadSection(MUNICIPALITY_ID_INTEGER))
                .thenReturn(List.of(NWB_ACCESSIBLE_BOTH, NEW_ACCESSIBLE_FORWARD, NEW_ACCESSIBLE_REVERSED));

        when(accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties,
                municipality)).thenReturn(List.of(IsochroneMatch.builder()
                        .matchedLinkId(ID_1)
                        .geometry(LINE_STRING_1)
                        .reversed(false)
                        .build(),
                IsochroneMatch.builder()
                        .matchedLinkId(ID_1)
                        .geometry(LINE_STRING_1)
                        .reversed(true)
                        .build(),
                IsochroneMatch.builder()
                        .matchedLinkId(ID_2)
                        .geometry(LINE_STRING_2)
                        .reversed(false)
                        .build(),
                IsochroneMatch.builder()
                        .matchedLinkId(ID_3)
                        .geometry(LINE_STRING_3)
                        .reversed(true)
                        .build()));

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID_STRING,
                        ResultType.EFFECTIVE_ACCESSIBILITY);

        assertThat(idToRoadSections).hasSize(3)
                .containsEntry(ID_1, new RoadSection(ID_1, LINE_STRING_1, true, true))
                .containsEntry(ID_2, new RoadSection(ID_2, LINE_STRING_2, true, null))
                .containsEntry(ID_3, new RoadSection(ID_3, LINE_STRING_3, null, true));
    }

    @Test
    void determineAccessibilityByRoadSection_ok_isolatingAForwardAccessibleEffectivelyAccessible() {
        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING)).thenReturn(municipality);
        when(municipality.getMunicipalityIdInteger()).thenReturn(MUNICIPALITY_ID_INTEGER);

        when(accessibleRoadSectionsService.getRoadSectionIdToRoadSection(MUNICIPALITY_ID_INTEGER))
                .thenReturn(List.of(NWB_ACCESSIBLE_BOTH, NEW_ACCESSIBLE_FORWARD, NEW_ACCESSIBLE_REVERSED));

        when(accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties,
                municipality)).thenReturn(List.of(IsochroneMatch.builder()
                        .matchedLinkId(ID_1)
                        .geometry(LINE_STRING_1)
                        .reversed(false)
                        .build()));

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID_STRING,
                        ResultType.EFFECTIVE_ACCESSIBILITY);

        assertThat(idToRoadSections).hasSize(3)
                .containsEntry(ID_1, new RoadSection(ID_1, LINE_STRING_1, true, false))
                .containsEntry(ID_2, new RoadSection(ID_2, LINE_STRING_2, false, null))
                .containsEntry(ID_3, new RoadSection(ID_3, LINE_STRING_3, null, false));
    }

    @Test
    void determineAccessibilityByRoadSection_ok_isolatingAReverseAccessibleEffectivelyAccessible() {
        when(accessibilityMapFactory.createMapMatcher(networkGraphHopper)).thenReturn(accessibilityMap);
        when(municipalityService.getMunicipalityById(MUNICIPALITY_ID_STRING)).thenReturn(municipality);
        when(municipality.getMunicipalityIdInteger()).thenReturn(MUNICIPALITY_ID_INTEGER);

        when(accessibleRoadSectionsService.getRoadSectionIdToRoadSection(MUNICIPALITY_ID_INTEGER))
                .thenReturn(List.of(NWB_ACCESSIBLE_BOTH, NEW_ACCESSIBLE_FORWARD, NEW_ACCESSIBLE_REVERSED));

        when(accessibleRoadsService.getVehicleAccessibleRoadsByMunicipality(accessibilityMap, vehicleProperties,
                municipality)).thenReturn(List.of(IsochroneMatch.builder()
                        .matchedLinkId(ID_1)
                        .geometry(LINE_STRING_1)
                        .reversed(true)
                        .build()));

        SortedMap<Integer, RoadSection> idToRoadSections = accessibilityMapService
                .determineAccessibilityByRoadSection(vehicleProperties, MUNICIPALITY_ID_STRING,
                        ResultType.EFFECTIVE_ACCESSIBILITY);

        assertThat(idToRoadSections).hasSize(3)
                .containsEntry(ID_1, new RoadSection(ID_1, LINE_STRING_1, false, true))
                .containsEntry(ID_2, new RoadSection(ID_2, LINE_STRING_2, false, null))
                .containsEntry(ID_3, new RoadSection(ID_3, LINE_STRING_3, null, false));
    }

}
