package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.writers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.utils.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.geojson.geometry.mappers.JtsLineStringJsonMapper;
import nu.ndw.nls.geojson.geometry.model.LineStringJson;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureBuilderTest {

    private static final String EXTERNAL_ID = "externalId";

    private FeatureBuilder featureBuilder;

    @Mock
    private JtsLineStringJsonMapper jtsLineStringJsonMapper;

    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Mock
    private LineString directionalSegmentLineString;

    @Mock
    private LineString trafficSignLineString;

    private DirectionalSegment directionalSegmentForward;

    private LongSequenceSupplier idSequenceSupplier;

    @BeforeEach
    void setUp() {

        idSequenceSupplier = new LongSequenceSupplier();

        featureBuilder = new FeatureBuilder(jtsLineStringJsonMapper, fractionAndDistanceCalculator);
    }

    @Test
    void createLineStringsAndTrafficSigns_ok_forDirectionalSegment_allRoadSections_andTrafficSigns() throws JsonProcessingException {

        prepareDirectionsSegments(true, true, true);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .addAllRoadSectionFragments(true)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(false)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(false)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, true, true, true);

        List<Feature> features = featureBuilder.createLineStringsAndTrafficSigns(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, true, true, true, true, true);
    }

    @Test
    void createLineStringsAndTrafficSigns_ok_forDirectionalSegment_allRoadSections_andTrafficSignsNoTrafficSigns()
            throws JsonProcessingException {

        prepareDirectionsSegments(true, true, false);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .addAllRoadSectionFragments(true)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(false)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(false)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, true, false, false);

        List<Feature> features = featureBuilder.createLineStringsAndTrafficSigns(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, true, true, true, false, false);
    }

    @Test
    void createLineStringsAndTrafficSigns_ok_forDirectionalSegment_allRoadSections_andTrafficSignsOnlyTrafficSignLineString()
            throws JsonProcessingException {

        prepareDirectionsSegments(true, true, true);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(false)
                .addAllRoadSectionFragments(true)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(false)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(false)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, true, true, false);

        List<Feature> features = featureBuilder.createLineStringsAndTrafficSigns(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, true, true, true, true, false);
    }

    @Test
    void createLineStringsAndTrafficSigns_ok_forDirectionalSegment_allRoadSections_andTrafficSignsOnlyTrafficSignPoint()
            throws JsonProcessingException {

        prepareDirectionsSegments(true, true, true);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(false)
                .addTrafficSignsAsPoints(true)
                .addAllRoadSectionFragments(true)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(false)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(false)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, true, false, true);

        List<Feature> features = featureBuilder.createLineStringsAndTrafficSigns(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, true, true, true, false, true);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, true
            true, false, false
            false, false, false
            """)
    void createLineStringsAndTrafficSigns_ok_accessibleInAllAvailableDirections(
            boolean addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections,
            boolean accessible,
            boolean expectRoadSection) throws JsonProcessingException {

        prepareDirectionsSegments(accessible, accessible, true);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .addAllRoadSectionFragments(false)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(
                        addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(false)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, expectRoadSection, true, true);

        List<Feature> features = featureBuilder.createLineStringsAndTrafficSigns(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, accessible, accessible, expectRoadSection, true, true);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true, false
            true, false, true
            false, false, false
            """)
    void createLineStringsAndTrafficSigns_ok_notAccessibleInAllAvailableDirections_blockedInAllDirections(
            boolean addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections,
            boolean accessible,
            boolean expectRoadSection) throws JsonProcessingException {

        prepareDirectionsSegments(accessible, accessible, true);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .addAllRoadSectionFragments(false)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(false)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(
                        addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, expectRoadSection, true, true);

        List<Feature> features = featureBuilder.createLineStringsAndTrafficSigns(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, accessible, accessible, expectRoadSection, true, true);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, false, true, true, false
            true, true, false, true, true
            false, false, true, false, false
            false, true, false, true, true
            """)
    void createLineStringsAndTrafficSigns_ok_partiallyAccessibleSegments(
            boolean writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible,
            boolean forwardAccessible,
            boolean backwardAccessible,
            boolean roadSectionAccessible,
            boolean trafficSignAccessible) throws JsonProcessingException {

        prepareDirectionsSegments(forwardAccessible, backwardAccessible, true);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .addAllRoadSectionFragments(false)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(false)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(false)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(
                        writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible)
                .build();

        prepareDependencies(generateConfiguration, true, true, true);

        List<Feature> features = featureBuilder.createLineStringsAndTrafficSigns(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, roadSectionAccessible, trafficSignAccessible, true, true, true);
    }

    @Test
    void createPolygon_ok()
            throws JsonProcessingException {

        Geometry polygonGeometry = mock(Geometry.class);
        when(polygonGeometry.getCoordinates()).thenReturn(new Coordinate[]{
                new Coordinate(1, 1, 0),
                new Coordinate(2, 2, 0),
        });

        List<TrafficSign> trafficSigns = List.of(TrafficSign.builder()
                .textSigns(List.of(
                        TextSign.builder()
                                .type(TextSignType.EXCLUDING)
                                .build(),
                        TextSign.builder()
                                .type(TextSignType.TIME_PERIOD)
                                .text("window")
                                .build()))
                .build());
        Set<Long> relevantRoadSectionIds = Set.of(1L, 2L, 3L);

        Feature polygonFeature = featureBuilder.createPolygon(
                polygonGeometry,
                idSequenceSupplier,
                trafficSigns,
                relevantRoadSectionIds);

        assertThatJson(new ObjectMapper().writeValueAsString(polygonFeature))
                .isEqualTo("""
                        {
                           "id":1,
                           "geometry":{
                              "coordinates":[[[1.0, 1.0],[2.0, 2.0]]
                              ],
                              "type":"Polygon",
                              "bbox":null
                           },
                           "properties":{
                              "roadSectionIds":[1,2,3],
                              "windowTimes":[
                                 "window"
                              ]
                           },
                           "type":"Feature"
                        }
                        """);
    }

    private void validateFeatures(
            List<Feature> features,
            boolean expectedRoadSectionToBeAccessible,
            boolean expectedTrafficSignToBeAccessible,
            boolean expectedRoadSection,
            boolean expectedTrafficSignLineString,
            boolean expectedTrafficSignPoint)
            throws JsonProcessingException {

        LongSequenceSupplier featureIdSupplier = new LongSequenceSupplier();
        String roadSegmentFeature = """
                {
                   "id":%s,
                   "geometry":{
                      "coordinates":[[12.0, 23.0]],
                      "type":"LineString"
                   },
                   "properties":{
                      "nwbRoadSectionId":1,
                      "roadSectionFragmentId": 10,
                      "accessible":%s,
                      "direction":"FORWARD"
                   },
                   "type":"Feature"
                }
                """
                .formatted(
                        expectedRoadSection ? featureIdSupplier.next() : 0,
                        expectedRoadSectionToBeAccessible);

        String trafficSignLineStringFeature = """
                {
                   "id":%s,
                   "geometry":{
                      "coordinates":[[78.0, 89.0]],
                      "type":"LineString"
                   },
                   "properties":{
                      "nwbRoadSectionId":1,
                      "accessible":%s,
                      "trafficSignId": "externalId",
                      "direction":"FORWARD",
                      "trafficSignType":"C7",
                      "iconUrl":"https://exmaple.com/image.png",
                      "trafficSign":true,
                      "windowTimes":"window1"
                   },
                   "type":"Feature"
                }
                """
                .formatted(
                        expectedTrafficSignLineString ? featureIdSupplier.next() : 0,
                        expectedTrafficSignToBeAccessible);

        String trafficSignPointFeature = """
                {
                   "id":%s,
                   "geometry":{
                      "coordinates":[12.0, 23.0],
                      "type":"Point"
                   },
                   "properties":{
                      "nwbRoadSectionId":1,
                      "accessible":%s,
                      "trafficSignId": "externalId",
                      "direction":"FORWARD",
                      "trafficSignType":"C7",
                      "iconUrl":"https://exmaple.com/image.png",
                      "trafficSign":true,
                      "windowTimes":"window1"
                   },
                   "type":"Feature"
                }
                """
                .formatted(
                        expectedTrafficSignPoint ? featureIdSupplier.next() : 0,
                        expectedTrafficSignToBeAccessible);

        List<String> expectedFeatures = new ArrayList<>();
        if (expectedRoadSection) {
            expectedFeatures.add(roadSegmentFeature);
        }
        if (expectedTrafficSignLineString) {
            expectedFeatures.add(trafficSignLineStringFeature);
        }
        if (expectedTrafficSignPoint) {
            expectedFeatures.add(trafficSignPointFeature);
        }
        assertThatJson(new ObjectMapper().writeValueAsString(features))
                .isEqualTo("[%s]".formatted(String.join(",", expectedFeatures)));
    }

    private void prepareDirectionsSegments(
            boolean forwardAccessible,
            boolean backwardAccessible,
            boolean addTrafficSign) {

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .id(10)
                .roadSection(RoadSection.builder()
                        .id(1L)
                        .build())
                .build();

        directionalSegmentForward = DirectionalSegment.builder()
                .direction(Direction.FORWARD)
                .lineString(directionalSegmentLineString)
                .accessible(forwardAccessible)
                .trafficSign(
                        !addTrafficSign
                                ? null
                                : TrafficSign.builder()
                                        .externalId(EXTERNAL_ID)
                                        .trafficSignType(TrafficSignType.C7)
                                        .textSigns(List.of(
                                                TextSign.builder()
                                                        .text("window1")
                                                        .type(TextSignType.TIME_PERIOD)
                                                        .build(),
                                                TextSign.builder()
                                                        .text("window2")
                                                        .type(TextSignType.TIME_PERIOD)
                                                        .build()
                                        ))
                                        .longitude(2.3)
                                        .latitude(4.5)
                                        .direction(Direction.FORWARD)
                                        .iconUri(URI.create("https://exmaple.com/image.png"))
                                        .build()
                )
                .roadSectionFragment(roadSectionFragment)
                .build();
        roadSectionFragment.setForwardSegment(directionalSegmentForward);

        DirectionalSegment directionalSegmentBackward = DirectionalSegment.builder()
                .direction(Direction.BACKWARD)
                .lineString(directionalSegmentLineString)
                .accessible(backwardAccessible)
                .trafficSign(
                        !addTrafficSign
                                ? null
                                : TrafficSign.builder()
                                        .trafficSignType(TrafficSignType.C7)
                                        .textSigns(List.of(
                                                TextSign.builder()
                                                        .text("window1")
                                                        .type(TextSignType.TIME_PERIOD)
                                                        .build(),
                                                TextSign.builder()
                                                        .text("window2")
                                                        .type(TextSignType.TIME_PERIOD)
                                                        .build()
                                        ))
                                        .longitude(3.3)
                                        .latitude(5.5)
                                        .direction(Direction.BACKWARD)
                                        .iconUri(URI.create("https://exmaple.com/image.png"))
                                        .build())
                .roadSectionFragment(roadSectionFragment)
                .build();
        roadSectionFragment.setBackwardSegment(directionalSegmentBackward);
    }

    private void prepareDependencies(
            GenerateConfiguration generateConfiguration,
            boolean prepareRoadSection,
            boolean prepareTrafficSignLineString,
            boolean prepareTrafficSignPoint) {

        if (prepareTrafficSignLineString) {
            when(fractionAndDistanceCalculator.getSubLineStringByLengthInMeters(
                    directionalSegmentLineString,
                    generateConfiguration.trafficSignLineStringDistanceInMeters())
            ).thenReturn(trafficSignLineString);
            when(jtsLineStringJsonMapper.map(trafficSignLineString))
                    .thenReturn(new LineStringJson(List.of(List.of(78d, 89d))));
        }

        if (prepareTrafficSignPoint || prepareRoadSection) {
            when(jtsLineStringJsonMapper.map(directionalSegmentLineString))
                    .thenReturn(new LineStringJson(List.of(List.of(12d, 23d))));
        }
    }
}
