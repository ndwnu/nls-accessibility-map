package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.dto.Feature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.util.LongSequenceSupplier;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.geojson.mappers.GeoJsonLineStringCoordinateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeatureBuilderTest {

    private FeatureBuilder featureBuilder;

    @Mock
    private GeoJsonLineStringCoordinateMapper geoJsonLineStringCoordinateMapper;

    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Mock
    private LineString directionalSegmentLineString;

    @Mock
    private LineString trafficSignLineString;

    @Mock
    private Point startPoint;

    private DirectionalSegment directionalSegmentForward;

    private LongSequenceSupplier idSequenceSupplier;

    @BeforeEach
    void setUp() {

        idSequenceSupplier = new LongSequenceSupplier();

        featureBuilder = new FeatureBuilder(geoJsonLineStringCoordinateMapper, fractionAndDistanceCalculator);
    }

    @Test
    void createFeaturesForDirectionalSegment_ok_allRoadSectionsAndTrafficSigns() throws JsonProcessingException {

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

        List<Feature> features = featureBuilder.createFeaturesForDirectionalSegment(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, true, true, true, true, true);
    }

    @Test
    void createFeaturesForDirectionalSegment_ok_allRoadSectionsAndTrafficSigns_noTrafficSigns()
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

        List<Feature> features = featureBuilder.createFeaturesForDirectionalSegment(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, true, true, true, false, false);
    }

    @Test
    void createFeaturesForDirectionalSegment_ok_allRoadSectionsAndTrafficSigns_onlyTrafficSignLineString()
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

        List<Feature> features = featureBuilder.createFeaturesForDirectionalSegment(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, true, true, true, true, false);
    }

    @Test
    void createFeaturesForDirectionalSegment_ok_allRoadSectionsAndTrafficSigns_onlyTrafficSignPoint()
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

        List<Feature> features = featureBuilder.createFeaturesForDirectionalSegment(
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
    void createFeaturesForDirectionalSegment_ok_accessibleInAllAvailableDirections(
            boolean addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections,
            boolean accessible,
            boolean expectRoadSection) throws JsonProcessingException {

        prepareDirectionsSegments(accessible, accessible, true);

        GenerateConfiguration generateConfiguration = GenerateConfiguration.builder()
                .trafficSignLineStringDistanceInMeters(1)
                .addTrafficSignsAsLineStrings(true)
                .addTrafficSignsAsPoints(true)
                .addAllRoadSectionFragments(false)
                .addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections(addRoadSegmentFragmentsThatAreAccessibleInAllAvailableDirections)
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(false)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, expectRoadSection, true, true);

        List<Feature> features = featureBuilder.createFeaturesForDirectionalSegment(
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
    void createFeaturesForDirectionalSegment_ok_NotAccessibleInAllAvailableDirections_blockedInAllDirections(
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
                .addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections(addRoadSegmentFragmentsThatAreBlockedInAllAvailableDirections)
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(false)
                .build();

        prepareDependencies(generateConfiguration, expectRoadSection, true, true);

        List<Feature> features = featureBuilder.createFeaturesForDirectionalSegment(
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
    void createFeaturesForDirectionalSegment_ok_partiallyAccessibleSegments(
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
                .writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible(writeRoadSegmentFragmentsThatArePartiallyAccessibleAsAccessible)
                .build();

        prepareDependencies(generateConfiguration, true, true, true);

        List<Feature> features = featureBuilder.createFeaturesForDirectionalSegment(
                directionalSegmentForward,
                idSequenceSupplier,
                generateConfiguration);

        validateFeatures(features, roadSectionAccessible, trafficSignAccessible, true, true, true);
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
                      "coordinates":[[45.0, 56.0]],
                      "type":"LineString"
                   },
                   "properties":{
                      "nwbRoadSectionId":1,
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
                .roadSection(RoadSection.builder()
                        .id(1)
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
            when(geoJsonLineStringCoordinateMapper.map(trafficSignLineString))
                    .thenReturn(List.of(List.of(78d, 89d)));
        }

        if (prepareTrafficSignPoint) {
            when(directionalSegmentLineString.getStartPoint()).thenReturn(startPoint);
            when(startPoint.getX()).thenReturn(12d);
            when(startPoint.getY()).thenReturn(23d);
        }

        if(prepareRoadSection) {
            when(geoJsonLineStringCoordinateMapper.map(directionalSegmentLineString))
                    .thenReturn(List.of(List.of(45d, 56d)));
        }
    }
}
