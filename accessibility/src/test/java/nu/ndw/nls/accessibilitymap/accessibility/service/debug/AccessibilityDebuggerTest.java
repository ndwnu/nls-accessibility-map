package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.routing.querygraph.QueryGraph;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.EdgeExplorer;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import com.graphhopper.util.shapes.GHPoint3D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.javacrumbs.jsonunit.core.Option;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.roadsection.RoadSectionRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportConditions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.service.debug.configuration.DebugConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityNetwork;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.geojson.geometry.mappers.JtsPointJsonMapper;
import nu.ndw.nls.geojson.geometry.mappers.JtsPolygonJsonMapper;
import nu.ndw.nls.geojson.geometry.model.GeometryJson.TypeEnum;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.geojson.geometry.model.PointJson;
import nu.ndw.nls.geojson.geometry.model.PolygonJson;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityDebuggerTest {

    private AccessibilityDebugger accessibilityDebugger;

    @Mock
    private DebugConfiguration debugConfiguration;

    @Mock
    private JtsPointJsonMapper jtsPointJsonMapper;

    @Mock
    private JtsLineStringJsonMapper jtsLineStringJsonMapper;

    @Mock
    private JtsPolygonJsonMapper jtsPolygonJsonMapper;

    @Mock
    private LineString lineStringForward;

    @Mock
    private LineString lineStringBackward;

    private Path testDir;

    @Mock
    private NetworkData networkData;

    @Mock
    private Weighting weighting;

    @BeforeEach
    void setUp() throws IOException {

        testDir = Files.createTempDirectory(this.getClass().getSimpleName());

        accessibilityDebugger = new AccessibilityDebugger(
                debugConfiguration,
                jtsPointJsonMapper,
                jtsLineStringJsonMapper,
                jtsPolygonJsonMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(testDir.toFile());
    }

    @ParameterizedTest
    @CsvSource({
            "true, false, false, false",
            "false, true, false, false",
            "false, false, true, false",
            "false, false, false, true"
    })
    void writeDebug_accessibility(
            boolean hasWithoutRestrictions,
            boolean hasWithRestrictions,
            boolean hasUnroutable,
            boolean hasCombined
    ) throws IOException {
        debugEnabled();

        RoadSection roadSection = buildRoadSection();

        List<List<Double>> coordinatesForward = List.of(List.of(0d, 0d), List.of(1d, 1d));
        when(jtsLineStringJsonMapper.map(lineStringForward)).thenReturn(new LineStringJson(coordinatesForward, TypeEnum.LINE_STRING));
        List<List<Double>> coordinatesBackward = List.of(List.of(1d, 1d), List.of(0d, 0d));
        when(jtsLineStringJsonMapper.map(lineStringBackward)).thenReturn(new LineStringJson(coordinatesBackward, TypeEnum.LINE_STRING));

        Accessibility accessibility = Accessibility.builder()
                .accessibleRoadsSectionsWithoutAppliedRestrictions(hasWithoutRestrictions ? List.of(roadSection) : List.of())
                .accessibleRoadSectionsWithAppliedRestrictions(hasWithRestrictions ? List.of(roadSection) : List.of())
                .unroutableRoadSections(hasUnroutable ? List.of(roadSection) : List.of())
                .combinedAccessibility(hasCombined ? List.of(roadSection) : List.of())
                .build();

        accessibilityDebugger.writeDebug(accessibility);

        String emptyFeatureCollection = "{\"features\":[],\"type\":\"FeatureCollection\"}";
        String fileName = null;
        if (!hasWithoutRestrictions) {
            assertThatJson(Files.readString(testDir.resolve("accessibility.roadSectionsWithoutRestrictions.geojson")))
                    .isEqualTo(emptyFeatureCollection);
        } else {
            fileName = "accessibility.roadSectionsWithoutRestrictions.geojson";
        }
        if (!hasWithRestrictions) {
            assertThatJson(Files.readString(testDir.resolve("accessibility.roadSectionsWithRestrictions.geojson")))
                    .isEqualTo(emptyFeatureCollection);
        } else {
            fileName = "accessibility.roadSectionsWithRestrictions.geojson";
        }
        if (!hasUnroutable) {
            assertThatJson(Files.readString(testDir.resolve("accessibility.unroutableRoadSections.geojson")))
                    .isEqualTo(emptyFeatureCollection);
        } else {
            fileName = "accessibility.unroutableRoadSections.geojson";
        }
        if (!hasCombined) {
            assertThatJson(Files.readString(testDir.resolve("accessibility.combinedAccessibility.geojson")))
                    .isEqualTo(emptyFeatureCollection);
        } else {
            fileName = "accessibility.combinedAccessibility.geojson";
        }

        assertThat(fileName).isNotNull();
        assertThatJson(Files.readString(testDir.resolve(fileName)))
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "LineString",
                              "coordinates" : [ [ 0.0, 0.0 ], [ 1.0, 1.0 ] ]
                            },
                            "properties" : {
                              "roadSectionId" : 1,
                              "roadSectionFragmentId" : 2,
                              "edge" : 2,
                              "segmentId" : 3,
                              "edgeKey" : 3,
                              "direction" : "FORWARD",
                              "startFraction" : 0.5,
                              "endFraction" : 1.0,
                              "accessible" : true
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 2,
                            "geometry" : {
                              "type" : "LineString",
                              "coordinates" : [ [ 1.0, 1.0 ], [ 0.0, 0.0 ] ]
                            },
                            "properties" : {
                              "roadSectionId" : 1,
                              "roadSectionFragmentId" : 2,
                              "edge" : 2,
                              "segmentId" : 3,
                              "edgeKey" : 3,
                              "direction" : "BACKWARD",
                              "startFraction" : 1.0,
                              "endFraction" : 0.5,
                              "accessible" : true
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_accessibility_debugDisabled() {
        when(debugConfiguration.isDisabled()).thenReturn(true);
        accessibilityDebugger.writeDebug((Accessibility) null);

        assertThat(testDir.resolve("accessibility.roadSectionsWithoutRestrictions.geojson")).doesNotExist();
        assertThat(testDir.resolve("accessibility.roadSectionsWithRestrictions.geojson")).doesNotExist();
        assertThat(testDir.resolve("accessibility.unroutableRoadSections.geojson")).doesNotExist();
        assertThat(testDir.resolve("accessibility.combinedAccessibility.geojson")).doesNotExist();
    }

    @Test
    void writeDebug_queryGraph() throws IOException {
        when(debugConfiguration.getDebugFolder()).thenReturn(testDir);

        QueryGraph queryGraph = mock(QueryGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);
        EdgeExplorer edgeExplorer = mock(EdgeExplorer.class);
        EdgeIterator edgeIterator = mock(EdgeIterator.class);
        EdgeIteratorState edgeIteratorState = mock(EdgeIteratorState.class);
        PointList pointList = mock(PointList.class);

        when(queryGraph.getNodes()).thenReturn(2);
        when(queryGraph.getNodeAccess()).thenReturn(nodeAccess);
        when(nodeAccess.getLat(0)).thenReturn(52.0);
        when(nodeAccess.getLon(0)).thenReturn(4.0);
        when(nodeAccess.getLat(1)).thenReturn(52.1);
        when(nodeAccess.getLon(1)).thenReturn(4.1);

        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);
        when(edgeExplorer.setBaseNode(0)).thenReturn(edgeIterator);
        when(edgeExplorer.setBaseNode(1)).thenReturn(edgeIterator);

        when(edgeIterator.next())
                .thenReturn(true)
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(false);

        when(edgeIterator.getAdjNode())
                .thenReturn(1)
                .thenReturn(0);

        when(edgeIterator.getEdge())
                .thenReturn(10)
                .thenReturn(10);

        when(queryGraph.getEdgeIteratorState(10, 1)).thenReturn(edgeIteratorState);
        when(queryGraph.getEdgeIteratorState(10, 0)).thenReturn(edgeIteratorState);

        when(edgeIteratorState.getEdge())
                .thenReturn(10)
                .thenReturn(10);

        when(edgeIteratorState.getEdgeKey())
                .thenReturn(20)
                .thenReturn(21);

        when(edgeIterator.getDistance())
                .thenReturn(100.5)
                .thenReturn(100.5);

        when(edgeIterator.fetchWayGeometry(FetchMode.ALL)).thenReturn(pointList);
        LineString lineString = mock(LineString.class);
        when(pointList.toLineString(false)).thenReturn(lineString);

        when(jtsPointJsonMapper.map(argThat(point -> {
            if (point.getX() == 4.0 && point.getY() == 52.0) {
                return true;
            }
            return point.getX() == 4.1 && point.getY() == 52.1;
        }))).thenAnswer(invocation -> {
            Point point = invocation.getArgument(0, Point.class);
            return new PointJson(List.of(point.getX(), point.getY()), TypeEnum.POINT);
        });

        when(jtsLineStringJsonMapper.map(lineString))
                .thenReturn(new LineStringJson(List.of(List.of(4.0, 52.0), List.of(4.1, 52.1)), TypeEnum.LINE_STRING));

        accessibilityDebugger.writeDebug(queryGraph);

        assertThatJson(Files.readString(testDir.resolve("graphHopper.nodes.geojson")))
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 4.0, 52.0 ]
                            },
                            "properties" : {
                              "id" : 0,
                              "type" : "node"
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 2,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 4.1, 52.1 ]
                            },
                            "properties" : {
                              "id" : 1,
                              "type" : "node"
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);

        assertThatJson(Files.readString(testDir.resolve("graphHopper.edges.geojson")))
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "LineString",
                              "coordinates" : [ [ 4.0, 52.0 ], [ 4.1, 52.1 ] ]
                            },
                            "properties" : {
                              "type" : "edge",
                              "edge" : 10,
                              "edgeKey" : 20,
                              "fromNode" : 0,
                              "toNode" : 1,
                              "distance" : 100.5
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 2,
                            "geometry" : {
                              "type" : "LineString",
                              "coordinates" : [ [ 4.0, 52.0 ], [ 4.1, 52.1 ] ]
                            },
                            "properties" : {
                              "type" : "edge",
                              "edge" : 10,
                              "edgeKey" : 21,
                              "fromNode" : 1,
                              "toNode" : 0,
                              "distance" : 100.5
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_queryGraph_emptyGraph() throws IOException {
        when(debugConfiguration.getDebugFolder()).thenReturn(testDir);

        QueryGraph queryGraph = mock(QueryGraph.class);
        NodeAccess nodeAccess = mock(NodeAccess.class);
        EdgeExplorer edgeExplorer = mock(EdgeExplorer.class);

        when(queryGraph.getNodes()).thenReturn(0);
        when(queryGraph.getNodeAccess()).thenReturn(nodeAccess);
        when(queryGraph.createEdgeExplorer()).thenReturn(edgeExplorer);

        accessibilityDebugger.writeDebug(queryGraph);

        assertThatJson(Files.readString(testDir.resolve("graphHopper.nodes.geojson")))
                .isEqualTo("""
                        {
                          "features" : [],
                          "type" : "FeatureCollection"
                        }
                        """);

        assertThatJson(Files.readString(testDir.resolve("graphHopper.edges.geojson")))
                .isEqualTo("""
                        {
                          "features" : [],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_queryGraph_debugDisabled() {
        when(debugConfiguration.isDisabled()).thenReturn(true);

        QueryGraph queryGraph = mock(QueryGraph.class);
        accessibilityDebugger.writeDebug(queryGraph);

        assertThat(testDir.resolve("graphHopper.nodes.geojso")).doesNotExist();
        assertThat(testDir.resolve("graphHopper.edges.geojso")).doesNotExist();
    }

    @Test
    void writeDebug_roadSections() throws IOException {
        debugEnabled();

        var roadSection = buildRoadSection();

        List<List<Double>> coordinatesForward = List.of(List.of(0d, 0d), List.of(1d, 1d));
        when(jtsLineStringJsonMapper.map(lineStringForward)).thenReturn(new LineStringJson(coordinatesForward, TypeEnum.LINE_STRING));
        List<List<Double>> coordinatesBackward = List.of(List.of(1d, 1d), List.of(0d, 0d));
        when(jtsLineStringJsonMapper.map(lineStringBackward)).thenReturn(new LineStringJson(coordinatesBackward, TypeEnum.LINE_STRING));

        accessibilityDebugger.writeDebug("roadSections", List.of(roadSection));

        assertThatJson(Files.readString(testDir.resolve("roadSections.geojson")))
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "LineString",
                              "coordinates" : [ [ 0.0, 0.0 ], [ 1.0, 1.0 ] ]
                            },
                            "properties" : {
                              "roadSectionId" : 1,
                              "roadSectionFragmentId" : 2,
                              "edge" : 2,
                              "segmentId" : 3,
                              "edgeKey" : 3,
                              "direction" : "FORWARD",
                              "startFraction" : 0.5,
                              "endFraction" : 1.0,
                              "accessible" : true
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 2,
                            "geometry" : {
                              "type" : "LineString",
                              "coordinates" : [ [ 1.0, 1.0 ], [ 0.0, 0.0 ] ]
                            },
                            "properties" : {
                              "roadSectionId" : 1,
                              "roadSectionFragmentId" : 2,
                              "edge" : 2,
                              "segmentId" : 3,
                              "edgeKey" : 3,
                              "direction" : "BACKWARD",
                              "startFraction" : 1.0,
                              "endFraction" : 0.5,
                              "accessible" : true
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_roadSections_debugDisabled() {
        when(debugConfiguration.isDisabled()).thenReturn(true);

        accessibilityDebugger.writeDebug("roadSections", List.of());

        assertThat(testDir.resolve("roadSections.geojson")).doesNotExist();
    }

    @Test
    void writeDebug_restrictions_roadSection() throws IOException {
        debugEnabled();

        Restriction roadSection = RoadSectionRestriction.builder()
                .id(7)
                .networkSnappedLatitude(3D)
                .networkSnappedLongitude(4D)
                .direction(Direction.BACKWARD)
                .fraction(0.6)
                .build();
        Restrictions restrictions = new Restrictions(List.of(roadSection));

        when(jtsPointJsonMapper.map(any(Point.class))).thenAnswer(invocation -> {
            Point point = invocation.getArgument(0, Point.class);
            if (point == null) {
                return null;
            }

            if (point.getX() == 4D && point.getY() == 3D) {
                return new PointJson(List.of(4D, 3D), TypeEnum.POINT);
            }

            throw new IllegalArgumentException("Unexpected point: " + point);
        });

        accessibilityDebugger.writeDebug(restrictions);

        assertThatJson(Files.readString(testDir.resolve("activeRestriction.geojson")))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 4.0, 3.0 ]
                            },
                            "properties" : {
                              "type" : "RoadSectionRestriction",
                              "roadSectionId" : 7,
                              "direction" : "BACKWARD",
                              "fraction" : 0.6,
                              "trafficSignId" : null,
                              "trafficSignExternalId" : null,
                              "trafficSignType" : null
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    private void debugEnabled() {
        when(debugConfiguration.getDebugFolder()).thenReturn(testDir);
        when(debugConfiguration.isDisabled()).thenReturn(false);
    }

    @Test
    void writeDebug_restrictions_trafficSign() throws IOException {
        debugEnabled();

        Restriction trafficSign = TrafficSign.builder()
                .id(1)
                .externalId("2")
                .roadSectionId(6)
                .networkSnappedLatitude(3D)
                .networkSnappedLongitude(4D)
                .direction(Direction.BACKWARD)
                .trafficSignType(TrafficSignType.C1)
                .fraction(0.5)
                .transportRestrictions(TransportRestrictions.builder()
                        .restrictions(TransportConditions.builder()
                                .transportTypes(Set.of(TransportType.CAR))
                                .categories(Set.of(Category.LOCAL_TRAFFIC))
                                .emissionClass(EmissionClass.EURO_1)
                                .timeValidity("time restrict")
                                .vehicleLengthInCm(new Maximum(2D))
                                .vehicleHeightInCm(new Maximum(2D))
                                .vehicleWidthInCm(new Maximum(3D))
                                .vehicleWeightInKg(new Maximum(1D))
                                .vehicleAxleLoadInKg(new Maximum(4D))
                                .build())
                        .exemptions(List.of(
                                TransportConditions.builder()
                                        .transportTypes(Set.of(TransportType.TRUCK))
                                        .categories(Set.of(Category.LOCAL_TRAFFIC))
                                        .emissionClass(EmissionClass.EURO_2)
                                        .timeValidity("time exempt")
                                        .vehicleLengthInCm(new Maximum(5D))
                                        .vehicleHeightInCm(new Maximum(6D))
                                        .vehicleWidthInCm(new Maximum(7D))
                                        .vehicleWeightInKg(new Maximum(8D))
                                        .vehicleAxleLoadInKg(new Maximum(9D))
                                        .build()
                        ))
                        .build())
                .build();
        Restrictions restrictions = new Restrictions(List.of(trafficSign));

        when(jtsPointJsonMapper.map(any(Point.class))).thenAnswer(invocation -> {
            Point point = invocation.getArgument(0, Point.class);
            if (point == null) {
                return null;
            }

            if (point.getX() == 4D && point.getY() == 3D) {
                return new PointJson(List.of(4D, 3D), TypeEnum.POINT);
            }

            throw new IllegalArgumentException("Unexpected point: " + point);
        });

        accessibilityDebugger.writeDebug(restrictions);

        assertThatJson(Files.readString(testDir.resolve("activeRestriction.geojson")))
                .withOptions(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo("""
                        {
                            "features": [
                              {
                                "id": 1,
                                "geometry": {
                                  "type": "Point",
                                  "coordinates": [
                                    4.0,
                                    3.0
                                  ]
                                },
                                "properties": {
                                    "direction": "BACKWARD",
                                    "exemptions1.categories": "LOCAL_TRAFFIC",
                                    "exemptions1.emissionClass": "EURO_2",
                                    "exemptions1.timeValidity": "time exempt",
                                    "exemptions1.transportTypes": "TRUCK",
                                    "exemptions1.vehicleAxleLoadInKg": "9.0",
                                    "exemptions1.vehicleHeightInCm": "6.0",
                                    "exemptions1.vehicleLengthInCm": "5.0",
                                    "exemptions1.vehicleWeightInKg": "8.0",
                                    "exemptions1.vehicleWidthInCm": "7.0",
                                    "fraction": 0.5,
                                    "restrictions.categories": "LOCAL_TRAFFIC",
                                    "restrictions.emissionClass": "EURO_1",
                                    "restrictions.timeValidity": "time restrict",
                                    "restrictions.transportTypes": "CAR",
                                    "restrictions.vehicleAxleLoadInKg": "4.0",
                                    "restrictions.vehicleHeightInCm": "2.0",
                                    "restrictions.vehicleLengthInCm": "2.0",
                                    "restrictions.vehicleWeightInKg": "1.0",
                                    "restrictions.vehicleWidthInCm": "3.0",
                                    "roadSectionId": 6,
                                    "trafficSignExternalId": "2",
                                    "trafficSignId": 1,
                                    "trafficSignType": "C1",
                                    "type": "TrafficSign"
                                },
                                "type": "Feature"
                              }
                            ],
                            "type": "FeatureCollection"
                          }
                        """);
    }



    @Test
    void writeDebug_restrictions_debugDisabled() {
        when(debugConfiguration.isDisabled()).thenReturn(true);

        accessibilityDebugger.writeDebug((Restrictions) null);

        assertThat(testDir.resolve("activeRestriction.geojson")).doesNotExist();
    }

    @Test
    void writeDebug_accessibilityRequest() throws IOException {
        debugEnabled();

        AccessibilityRequest accessibilityRequest = AccessibilityRequest.builder()
                .startLocationLatitude(1D)
                .startLocationLongitude(2D)
                .endLocationLatitude(3D)
                .endLocationLongitude(4D)
                .requestArea(BBox.fromPoints(10, 11, 12, 13))
                .searchArea(BBox.fromPoints(20, 21, 22, 23))
                .maxSearchDistanceInMeters(123D)
                .build();

        when(jtsPolygonJsonMapper.map(any(Polygon.class))).thenAnswer(invocation -> {
            Polygon polygon = invocation.getArgument(0, Polygon.class);
            if (polygon == null) {
                return null;
            }

            if (polygon.toString().equals("POLYGON ((11 10, 13 10, 13 12, 11 12, 11 10))")) {
                return new PolygonJson(
                        List.of(List.of(List.of(11D, 10D), List.of(13D, 10D), List.of(13D, 12D), List.of(11D, 12D), List.of(11D, 10D))),
                        TypeEnum.POINT);
            }
            if (polygon.toString().equals("POLYGON ((21 20, 23 20, 23 22, 21 22, 21 20))")) {
                return new PolygonJson(
                        List.of(List.of(List.of(21D, 20D), List.of(23D, 20D), List.of(23D, 22D), List.of(21D, 22D), List.of(21D, 20D))),
                        TypeEnum.POINT);
            }

            //The Circle
            if (polygon.getCoordinates().length == 65) {
                return new PolygonJson(
                        List.of(List.of(List.of(1D, 1D), List.of(1D, 2D), List.of(2D, 2D), List.of(2D, 1D), List.of(1D, 1D))),
                        TypeEnum.POINT);
            }
            throw new IllegalArgumentException("Unexpected point: " + polygon);
        });
        when(jtsPointJsonMapper.map(any(Point.class))).thenAnswer(invocation -> {
            Point p = invocation.getArgument(0, Point.class);
            if (p == null) {
                return null;
            }

            if (p.getX() == 2D && p.getY() == 1D) {
                return new PointJson(List.of(2D, 1D), TypeEnum.POINT);
            }
            if (p.getX() == 4D && p.getY() == 3D) {
                return new PointJson(List.of(4D, 3D), TypeEnum.POINT);
            }

            throw new IllegalArgumentException("Unexpected point: " + p);
        });
        accessibilityDebugger.writeDebug(accessibilityRequest);

        assertThatJson(Files.readString(testDir.resolve("accessibilityRequest.geojson")))
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "Polygon",
                              "coordinates" : [ [ [ 11.0, 10.0 ], [ 13.0, 10.0 ], [ 13.0, 12.0 ], [ 11.0, 12.0 ], [ 11.0, 10.0 ] ] ]
                            },
                            "properties" : {
                              "name" : "requestArea"
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 2,
                            "geometry" : {
                              "type" : "Polygon",
                              "coordinates" : [ [ [ 21.0, 20.0 ], [ 23.0, 20.0 ], [ 23.0, 22.0 ], [ 21.0, 22.0 ], [ 21.0, 20.0 ] ] ]
                            },
                            "properties" : {
                              "name" : "searchArea"
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 3,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 2.0, 1.0 ]
                            },
                            "properties" : {
                              "name" : "from"
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 4,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 4.0, 3.0 ]
                            },
                            "properties" : {
                              "name" : "destination"
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 5,
                            "geometry" : {
                              "type" : "Polygon",
                              "coordinates" : [ [ [ 1.0, 1.0 ], [ 1.0, 2.0 ], [ 2.0, 2.0 ], [ 2.0, 1.0 ], [ 1.0, 1.0 ] ] ]
                            },
                            "properties" : {
                              "name" : "searchRadius"
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_accessibilityRequest_debugDisabled() {
        when(debugConfiguration.isDisabled()).thenReturn(true);

        accessibilityDebugger.writeDebug((AccessibilityRequest) null);

        assertThat(testDir.resolve("accessibilityRequest.geojson")).doesNotExist();
    }

    @Test
    void writeDebug_accessibilityNetwork() throws IOException {
        debugEnabled();

        Snap from = mock(Snap.class);
        when(from.getSnappedPoint()).thenReturn(new GHPoint3D(1D, 2D, 0));
        Snap destination = mock(Snap.class);
        when(destination.getSnappedPoint()).thenReturn(new GHPoint3D(3D, 4D, 0));

        AccessibilityNetwork accessibilityNetwork = new AccessibilityNetwork(
                networkData,
                null,
                mock(Restrictions.class),
                Map.of(),
                from,
                destination, weighting);

        when(jtsPointJsonMapper.map(any(Point.class))).thenAnswer(invocation -> {
            Point p = invocation.getArgument(0, Point.class);
            if (p == null) {
                return null;
            }

            if (p.getX() == 2D && p.getY() == 1D) {
                return new PointJson(List.of(2D, 1D), TypeEnum.POINT);
            }
            if (p.getX() == 4D && p.getY() == 3D) {
                return new PointJson(List.of(4D, 3D), TypeEnum.POINT);
            }

            throw new IllegalArgumentException("Unexpected point: " + p);
        });

        accessibilityDebugger.writeDebug(accessibilityNetwork);

        assertThatJson(Files.readString(testDir.resolve("accessibilityNetwork.geojson")))
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 2.0, 1.0 ]
                            },
                            "properties" : {
                              "name" : "from"
                            },
                            "type" : "Feature"
                          }, {
                            "id" : 2,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 4.0, 3.0 ]
                            },
                            "properties" : {
                              "name" : "destination"
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_accessibilityNetwork_noDestination() throws IOException {
        debugEnabled();

        Snap from = mock(Snap.class);
        when(from.getSnappedPoint()).thenReturn(new GHPoint3D(1D, 2D, 0));

        AccessibilityNetwork accessibilityNetwork = new AccessibilityNetwork(
                networkData,
                null,
                mock(Restrictions.class),
                Map.of(),
                from,
                null, weighting);

        when(jtsPointJsonMapper.map(any(Point.class))).thenAnswer(invocation -> {
            Point p = invocation.getArgument(0, Point.class);
            if (p == null) {
                return null;
            }

            if (p.getX() == 2D && p.getY() == 1D) {
                return new PointJson(List.of(2D, 1D), TypeEnum.POINT);
            }

            throw new IllegalArgumentException("Unexpected point: " + p);
        });

        accessibilityDebugger.writeDebug(accessibilityNetwork);

        assertThatJson(Files.readString(testDir.resolve("accessibilityNetwork.geojson")))
                .isEqualTo("""
                        {
                          "features" : [ {
                            "id" : 1,
                            "geometry" : {
                              "type" : "Point",
                              "coordinates" : [ 2.0, 1.0 ]
                            },
                            "properties" : {
                              "name" : "from"
                            },
                            "type" : "Feature"
                          } ],
                          "type" : "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_directionalSegment() throws IOException {
        debugEnabled();

        when(debugConfiguration.getDebugFolder()).thenReturn(testDir);

        List<List<Double>> coordinatesForward = List.of(List.of(0d, 0d), List.of(1d, 1d));
        when(jtsLineStringJsonMapper.map(lineStringForward)).thenReturn(new LineStringJson(coordinatesForward, TypeEnum.LINE_STRING));
        List<List<Double>> coordinatesBackward = List.of(List.of(1d, 1d), List.of(0d, 0d));
        when(jtsLineStringJsonMapper.map(lineStringBackward)).thenReturn(new LineStringJson(coordinatesBackward, TypeEnum.LINE_STRING));

        RoadSection roadSection = buildRoadSection();
        List<DirectionalSegment> directionalSegments = roadSection.getRoadSectionFragments().stream()
                .flatMap(roadSectionFragment -> roadSectionFragment.getSegments().stream())
                .toList();

        accessibilityDebugger.writeDebug(directionalSegments);

        assertThatJson(Files.readString(testDir.resolve("destinationPaths.geojson")))
                .isEqualTo("""
                        {
                          "features": [
                            {
                              "id": 1,
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [ [0.0, 0.0], [1.0, 1.0] ]
                              },
                              "properties": {
                                "accessible": true,
                                "direction": "FORWARD",
                                "edge": 2,
                                "edgeKey": 3,
                                "startFraction": 0.5,
                                "endFraction": 1.0,
                                "roadSectionFragmentId": 2,
                                "roadSectionId": 1,
                                "segmentId": 3
                              },
                              "type": "Feature"
                            },
                            {
                              "id": 2,
                              "geometry": {
                                "type": "LineString",
                                "coordinates": [ [1.0, 1.0], [0.0, 0.0] ]
                              },
                              "properties": {
                                "accessible": true,
                                "direction": "BACKWARD",
                                "edge": 2,
                                "edgeKey": 3,
                                "startFraction": 1.0,
                                "endFraction": 0.5,
                                "roadSectionFragmentId": 2,
                                "roadSectionId": 1,
                                "segmentId": 3
                              },
                              "type": "Feature"
                            }
                          ],
                          "type": "FeatureCollection"
                        }
                        """);
    }

    @Test
    void writeDebug_accessibilityNetwork_debugDisabled() {
        when(debugConfiguration.isDisabled()).thenReturn(true);

        accessibilityDebugger.writeDebug((AccessibilityNetwork) null);

        assertThat(testDir.resolve("accessibilityNetwork.geojson")).doesNotExist();
    }

    private @NonNull RoadSection buildRoadSection() {
        RoadSection roadSection = RoadSection.builder()
                .id(1L)
                .build();

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .id(2)
                .roadSection(roadSection)
                .build();

        DirectionalSegment forwardSegment = DirectionalSegment.builder()
                .id(3)
                .roadSectionFragment(roadSectionFragment)
                .startFraction(0.5)
                .endFraction(1)
                .direction(Direction.FORWARD)
                .lineString(lineStringForward)
                .accessible(true)
                .build();
        DirectionalSegment backwardSegment = DirectionalSegment.builder()
                .id(3)
                .roadSectionFragment(roadSectionFragment)
                .startFraction(1)
                .endFraction(0.5)
                .direction(Direction.BACKWARD)
                .lineString(lineStringBackward)
                .accessible(true)
                .build();

        roadSectionFragment.setForwardSegment(forwardSegment);
        roadSectionFragment.setBackwardSegment(backwardSegment);
        roadSection.setRoadSectionFragments(List.of(roadSectionFragment));
        return roadSection;
    }
}
