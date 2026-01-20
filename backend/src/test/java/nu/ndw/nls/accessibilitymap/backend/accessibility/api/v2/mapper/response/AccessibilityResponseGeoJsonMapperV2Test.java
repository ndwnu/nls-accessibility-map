package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.response;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityResponseGeoJsonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.DestinationRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.RestrictionUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeRestrictionJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityResponseGeoJsonMapperV2Test {

    private AccessibilityResponseGeoJsonMapperV2 accessibilityResponseGeoJsonMapperV2;

    @Mock
    private AccessibilityReasonsJsonMapperV2 accessibilityReasonsJsonMapperV2;

    @Mock
    private List<List<AccessibilityReason>> reasons;

    private RoadSection roadSectionInaccessible;

    private RoadSection roadSectionAccessible;
    private RoadSection roadSectionPartiallyhaccessible;

    private RoadSection destinationRoadSectionAccessible;

    private RoadSection destinationRoadSectionInAccessible;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        roadSectionAccessible = buildRoadSection(1, true, true);
        roadSectionInaccessible = buildRoadSection(2, false, false);
        roadSectionPartiallyhaccessible = buildRoadSection(5, false, true);
        destinationRoadSectionAccessible = buildRoadSection(3, true, true);
        destinationRoadSectionInAccessible = buildRoadSection(4, false, false);

        accessibilityResponseGeoJsonMapperV2 = new AccessibilityResponseGeoJsonMapperV2(accessibilityReasonsJsonMapperV2);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true",
            "null"
    }, nullValues = "null")
    void map(Boolean includeAccessibleAndInAccessibleRoadSections) throws JsonProcessingException {

        Accessibility accessibility = Accessibility.builder()
                .toRoadSection(Optional.of(destinationRoadSectionAccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, roadSectionPartiallyhaccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(includeAccessibleAndInAccessibleRoadSections)
                .includeInaccessibleRoadSections(includeAccessibleAndInAccessibleRoadSections)
                .destination(DestinationRequestJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility);

        assertThatJson(objectMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
                {
                  "type" : "FeatureCollection",
                  "features" : [ {
                    "type" : "Feature",
                    "id" : 1,
                    "geometry" : {
                      "type" : "Point",
                      "coordinates" : [ 4.45, 5.34 ]
                    },
                    "properties" : {
                      "type" : "destination",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "reasons" : [ ]
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 4,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 5,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "accessible" : false,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 6,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 5,
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 7,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 5,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 8,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 9,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  } ]
                }
                """);
    }


    @ParameterizedTest
    @CsvSource(value = {
            "false",
            "null"
    }, nullValues = "null")
    void map_effectivelyAccessible_defaultBehavior(Boolean effectivelyAccessible) throws JsonProcessingException {

        Accessibility accessibility = Accessibility.builder()
                .toRoadSection(Optional.of(destinationRoadSectionAccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, roadSectionPartiallyhaccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .effectivelyAccessible(effectivelyAccessible)
                .destination(DestinationRequestJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility);

        assertThatJson(objectMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
                {
                  "type" : "FeatureCollection",
                  "features" : [ {
                    "type" : "Feature",
                    "id" : 1,
                    "geometry" : {
                      "type" : "Point",
                      "coordinates" : [ 4.45, 5.34 ]
                    },
                    "properties" : {
                      "type" : "destination",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "reasons" : [ ]
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 4,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 5,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "accessible" : false,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 6,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 5,
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 7,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 5,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 8,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 9,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  } ]
                }
                """);
    }

    @Test
    void map_effectivelyAccessible_enabled() throws JsonProcessingException {

        Accessibility accessibility = Accessibility.builder()
                .toRoadSection(Optional.of(destinationRoadSectionAccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, roadSectionPartiallyhaccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .effectivelyAccessible(true)
                .destination(DestinationRequestJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility);

        assertThatJson(objectMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
                {
                  "type" : "FeatureCollection",
                  "features" : [ {
                    "type" : "Feature",
                    "id" : 1,
                    "geometry" : {
                      "type" : "Point",
                      "coordinates" : [ 4.45, 5.34 ]
                    },
                    "properties" : {
                      "type" : "destination",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "reasons" : [ ]
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 4,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 5,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "accessible" : false,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 6,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 5,
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 7,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 5,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 8,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 9,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  } ]
                }
                """);
    }


    @Test
    void map_destinationInaccessible() throws JsonProcessingException {

        when(accessibilityReasonsJsonMapperV2.map(reasons)).thenReturn(List.of(List.of(ReasonJson.builder()
                .trafficSignId(UUID.fromString("71332fe6-fb88-4a91-8b72-eefc3c37c713"))
                .trafficSignType(TrafficSignTypeJson.C1)
                .restrictions(List.of(VehicleTypeRestrictionJson.builder()
                        .type(TypeEnum.VEHICLE_TYPE_RESTRICTION)
                        .unitSymbol(RestrictionUnitSymbolJson.ENUM)
                        .values(List.of(VehicleTypeJson.CAR))
                        .condition(RestrictionConditionJson.EQUALS)
                        .build()))
                .build())));
        Accessibility accessibility = Accessibility.builder()
                .toRoadSection(Optional.of(destinationRoadSectionAccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionAccessible))
                .reasons(reasons)
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(true)
                .includeInaccessibleRoadSections(true)
                .destination(DestinationRequestJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility);

        assertThatJson(objectMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
                {
                  "type" : "FeatureCollection",
                  "features" : [ {
                    "type" : "Feature",
                    "id" : 1,
                    "geometry" : {
                      "type" : "Point",
                      "coordinates" : [ 4.45, 5.34 ]
                    },
                    "properties" : {
                      "roadSectionId" : 3,
                      "accessible" : true,
                      "type" : "destination",
                      "reasons" : [ [ {
                        "trafficSignId" : "71332fe6-fb88-4a91-8b72-eefc3c37c713",
                        "trafficSignType" : "C1",
                        "restrictions" : [ {
                          "type" : "vehicleTypeRestriction",
                          "unitSymbol" : "enum",
                          "condition" : "equals",
                          "values" : [ "car" ]
                        } ]
                      } ] ]
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 1,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 1,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 4,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 2,
                      "type" : "roadSectionSegment",
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 5,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 2,
                      "type" : "roadSectionSegment",
                      "accessible" : false,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 6,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 3,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 7,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 3,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  } ]
                }
                """);
    }

    @Test
    void map_onlyAccessible() throws JsonProcessingException {

        Accessibility accessibility = Accessibility.builder()
                .toRoadSection(Optional.of(destinationRoadSectionInAccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionInAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(true)
                .includeInaccessibleRoadSections(false)
                .destination(DestinationRequestJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility);

        assertThatJson(objectMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
                {
                  "type" : "FeatureCollection",
                  "features" : [ {
                    "type" : "Feature",
                    "id" : 1,
                    "geometry" : {
                      "type" : "Point",
                      "coordinates" : [ 4.45, 5.34 ]
                    },
                    "properties" : {
                      "roadSectionId" : 4,
                      "type" : "destination",
                      "accessible" : false,
                      "reasons" : [ ]
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 1,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 1,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  } ]
                }
                """);
    }

    @Test
    void map_onlyInaccessible() throws JsonProcessingException {

        Accessibility accessibility = Accessibility.builder()
                .toRoadSection(Optional.of(destinationRoadSectionAccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(false)
                .includeInaccessibleRoadSections(true)
                .destination(DestinationRequestJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility);

        assertThatJson(objectMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
                {
                  "type" : "FeatureCollection",
                  "features" : [ {
                    "type" : "Feature",
                    "id" : 1,
                    "geometry" : {
                      "type" : "Point",
                      "coordinates" : [ 4.45, 5.34 ]
                    },
                    "properties" : {
                      "roadSectionId" : 3,
                      "type" : "destination",
                      "accessible" : true,
                      "reasons" : [ ]
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 2,
                      "type" : "roadSectionSegment",
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 2,
                      "type" : "roadSectionSegment",
                      "accessible" : false,
                      "direction" : "backward"
                    }
                  } ]
                }
                """);
    }

    @ParameterizedTest
    @CsvSource({
            "false, true",
            "true, false",
            "false, false",
    })
    void map_noDestination(boolean hasAccessibilityRoadSection, boolean hasRequestDestination) throws JsonProcessingException {

        Accessibility accessibility = Accessibility.builder()
                .toRoadSection(hasAccessibilityRoadSection ? Optional.of(destinationRoadSectionAccessible) : Optional.empty())
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(true)
                .includeInaccessibleRoadSections(true)
                .destination(hasRequestDestination
                        ? DestinationRequestJson.builder().latitude(5.34d).longitude(4.45d).build()
                        : null)
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(accessibilityRequestJson, accessibility);

        assertThatJson(objectMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
                {
                  "type" : "FeatureCollection",
                  "features" : [ {
                    "type" : "Feature",
                    "id" : 1,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 1,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 1,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 2,
                      "type" : "roadSectionSegment",
                      "accessible" : false,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 4,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 2,
                      "type" : "roadSectionSegment",
                      "accessible" : false,
                      "direction" : "backward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 5,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 3,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "forward"
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 6,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "roadSectionId" : 3,
                      "type" : "roadSectionSegment",
                      "accessible" : true,
                      "direction" : "backward"
                    }
                  } ]
                }
                """);
    }

    private RoadSection buildRoadSection(long id, boolean accessibleForward, boolean accessibleBackward) {
        RoadSection roadSection = RoadSection.builder()
                .id(id)
                .build();

        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
                .id(2)
                .roadSection(roadSection)
                .build();
        roadSection.getRoadSectionFragments().add(roadSectionFragment);

        DirectionalSegment directionalSegmentForward = DirectionalSegment.builder()
                .id(3)
                .accessible(accessibleForward)
                .direction(Direction.FORWARD)
                .roadSectionFragment(roadSectionFragment)
                .lineString(new GeometryFactory().createLineString(
                        new Coordinate[]{
                                new Coordinate(1, 2, 0),
                                new Coordinate(2, 3, 0)
                        }))
                .trafficSigns(List.of(TrafficSign.builder()
                        .id(4)
                        .roadSectionId(1)
                        .externalId("externalId")
                        .direction(Direction.FORWARD)
                        .fraction(2d)
                        .longitude(3d)
                        .latitude(4d)
                        .textSigns(List.of())
                        .networkSnappedLatitude(1D)
                        .networkSnappedLongitude(2D)
                        .trafficSignType(TrafficSignType.C7)
                        .restrictions(Restrictions.builder().build())
                        .build()))
                .build();

        DirectionalSegment directionalSegmentBackward = directionalSegmentForward
                .withDirection(Direction.BACKWARD)
                .withAccessible(accessibleBackward)
                .withLineString(new GeometryFactory().createLineString(
                        new Coordinate[]{
                                new Coordinate(2, 3, 0),
                                new Coordinate(1, 2, 0)
                        }));

        roadSectionFragment.setForwardSegment(directionalSegmentForward);
        roadSectionFragment.setBackwardSegment(directionalSegmentBackward);

        return roadSection;
    }
}
