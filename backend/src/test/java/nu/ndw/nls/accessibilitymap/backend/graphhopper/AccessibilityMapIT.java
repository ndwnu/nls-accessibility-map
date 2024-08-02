package nu.ndw.nls.accessibilitymap.backend.graphhopper;

import static nu.ndw.nls.accessibilitymap.shared.model.NetworkConstants.PROFILE;

import java.util.List;
import java.util.function.Predicate;
import nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.AccessibilityMap;
import nu.ndw.nls.accessibilitymap.shared.accessibility.graphhopper.factory.AccessibilityMapFactory;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;
import nu.ndw.nls.routingmapmatcher.network.GraphHopperNetworkService;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import nu.ndw.nls.routingmapmatcher.network.model.RoutingNetworkSettings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = AccessibilityMapITConfig.class)
class AccessibilityMapIT {

    private static final GeometryFactoryWgs84 WGS84_GEOMETRY_FACTORY = new GeometryFactoryWgs84();
    private static final Coordinate NODE = new Coordinate(0.0, 0.0);
    private static final LineString LINE_STRING = WGS84_GEOMETRY_FACTORY.createLineString(new Coordinate[]{NODE, NODE});

    private static final List<AccessibilityLink> OUTER_RING = List.of(
            createBaseRoad(0, 0, 1),
            createBaseRoad(1, 1, 2),
            createBaseRoad(2, 2, 3),
            createBaseRoad(3, 3, 4),
            createBaseRoad(4, 4, 5),
            createBaseRoad(5, 5, 0)
    );

    private static final List<AccessibilityLink> INNER_RING = List.of(
            createBaseRoad(6, 6, 7),
            createBaseRoad(7, 7, 8),
            createBaseRoad(8, 8, 9),
            createBaseRoad(9, 9, 10),
            createBaseRoad(10, 10, 6)
    );

    private static final double SEARCH_DISTANCE_IN_METRES = 500.0;
    private static final AccessibilityRequest REQUEST_RESTRICTED = AccessibilityRequest
            .builder()
            .vehicleProperties(VehicleProperties.builder()
                    .hgvAccessForbidden(true)
                    .axleLoad(10.00)
                    .build())
            .municipalityId(1)
            .searchDistanceInMetres(SEARCH_DISTANCE_IN_METRES)
            .startPoint(WGS84_GEOMETRY_FACTORY.createPoint(NODE))
            .build();
    private static final AccessibilityRequest REQUEST_UNRESTRICTED = AccessibilityRequest
            .builder()
            .municipalityId(1)
            .startPoint(WGS84_GEOMETRY_FACTORY.createPoint(NODE))
            .searchDistanceInMetres(SEARCH_DISTANCE_IN_METRES)
            .build();

    @Autowired
    private GraphHopperNetworkService graphHopperNetworkService;
    @Autowired
    private AccessibilityMapFactory accessibilityMapFactory;
    private NetworkGraphHopper graphHopper;

    /*
         Test network for accessibility
         The outer ring (connecting nodes 0-5) is always accessible.
         If access roads 4-10 and 1-7 are blocked and the start point is 0,0, the inner ring is also inaccessible

          5----4-----3
          |    |     |
          |    10-9  |
          |   /   |  |
          |  6    |  |
          |   \   |  |
          |    7--8  |
          |    |     |
          0----1-----2
    */

    @Test
    void getAccessibleRoadSections_ok_restrictedAccessRoadsInsideMunicipality() {
        // Access roads are blocked, and inside the municipality. Therefore, both these and the inner ring are returned.
        List<AccessibilityLink> accessRoads = createAccessRoads(true, 1);
        graphHopper = createGhNetwork(accessRoads);
        List<IsochroneMatch> notAccessible = getIsochroneMatches();
        assertMatches(notAccessible, join(List.of(INNER_RING, accessRoads)));
    }

    @Test
    void getAccessibleRoadSections_ok_unrestrictedAccessRoadsOutsideMunicipality() {
        // Access roads are open, though outside the municipality. Therefore, the inner ring is accessible.
        List<AccessibilityLink> accessRoads = createAccessRoads(false, 2);
        graphHopper = createGhNetwork(accessRoads);
        List<IsochroneMatch> notAccessible = getIsochroneMatches();
        assertMatches(notAccessible, List.of());
    }

    @Test
    void getAccessibleRoadSections_ok_restrictedAccessRoadsOutsideMunicipality() {
        // Access roads are closed, and outside the municipality. Therefore, the inner ring is inaccessible.
        List<AccessibilityLink> accessRoads = createAccessRoads(true, 2);
        graphHopper = createGhNetwork(accessRoads);
        List<IsochroneMatch> notAccessible = getIsochroneMatches();
        assertMatches(notAccessible, INNER_RING);
    }

    private void assertMatches(List<IsochroneMatch> matches, List<AccessibilityLink> expectedLinks) {
        List<Integer> expectedIds = expectedLinks.stream()
                .map(AccessibilityLink::getId)
                .map(Long::intValue)
                .toList();
        assertMatchesInDirection(matches, expectedIds, IsochroneMatch::isReversed);
        assertMatchesInDirection(matches, expectedIds, match -> !match.isReversed());
    }

    private void assertMatchesInDirection(List<IsochroneMatch> matches, List<Integer> expectedIds,
            Predicate<IsochroneMatch> directionPredicate) {
        Assertions.assertThat(matches).filteredOn(directionPredicate)
                .map(IsochroneMatch::getMatchedLinkId)
                .containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    private List<IsochroneMatch> getIsochroneMatches() {
        AccessibilityMap accessibilityMap = accessibilityMapFactory.createMapMatcher(graphHopper);
        List<IsochroneMatch> allAccessible = accessibilityMap.getAccessibleRoadSections(REQUEST_UNRESTRICTED);
        List<IsochroneMatch> restrictedAccess = accessibilityMap.getAccessibleRoadSections(REQUEST_RESTRICTED);
        return allAccessible.stream()
                .filter(m -> !restrictedAccess.contains(m))
                .toList();
    }

    private NetworkGraphHopper createGhNetwork(List<AccessibilityLink> accessRoads) {
        List<AccessibilityLink> allLinks = join(List.of(OUTER_RING, INNER_RING, accessRoads));
        RoutingNetworkSettings<AccessibilityLink> networkSettings = RoutingNetworkSettings
                .builder(AccessibilityLink.class)
                .linkSupplier(allLinks::iterator)
                .profiles(List.of(PROFILE))
                .build();
        return graphHopperNetworkService.inMemory(networkSettings);
    }

    private static AccessibilityLink createBaseRoad(long id, long fromNodeId, long toNodeId) {
        return createNoEntryRestrictedLink(id, fromNodeId, toNodeId, 1, Double.POSITIVE_INFINITY, false);
    }

    private static List<AccessibilityLink> createAccessRoads(boolean restrict, int municipalityCode) {
        return List.of(
                createNoEntryRestrictedLink(12, 7, 1, municipalityCode, restrict ? 3.0 : Double.POSITIVE_INFINITY,
                        false),
                createNoEntryRestrictedLink(13, 10, 4, municipalityCode, Double.POSITIVE_INFINITY, restrict));
    }

    private static AccessibilityLink createNoEntryRestrictedLink(long id, long fromNodeId, long toNodeId,
            int municipalityCode, double maxAxleLoad, boolean hgvAccessForbidden) {
        return AccessibilityLink.builder()
                .id(id)
                .fromNodeId(fromNodeId)
                .toNodeId(toNodeId)
                .accessibility(new DirectionalDto<>(true))
                .distanceInMeters(1)
                .municipalityCode(municipalityCode)
                .maxAxleLoad(new DirectionalDto<>(maxAxleLoad))
                .hgvAccessForbidden(new DirectionalDto<>(hgvAccessForbidden))
                .geometry(LINE_STRING)
                .build();
    }

    private <T> List<T> join(List<List<T>> lists) {
        return lists.stream().flatMap(List::stream).toList();
    }
}
