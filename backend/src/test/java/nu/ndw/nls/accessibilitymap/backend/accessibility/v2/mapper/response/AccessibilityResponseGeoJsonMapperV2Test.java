package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.response;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.DirectionalSegment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.RoadSectionFragment;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonGroup;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AccessibilityResponseGeoJsonJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.LocationJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonConditionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonJson.TypeEnum;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.ReasonUnitSymbolJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignRestrictionJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.TrafficSignTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.VehicleTypeReasonJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class AccessibilityResponseGeoJsonMapperV2Test {

    private AccessibilityResponseGeoJsonMapperV2 accessibilityResponseGeoJsonMapperV2;

    @Mock
    private AccessibilityReasonsJsonMapperV2 accessibilityReasonsJsonMapperV2;

    @Mock
    private List<AccessibilityReasonGroup> reasons;

    private RoadSection roadSectionInaccessible;

    private RoadSection roadSectionAccessible;

    private RoadSection roadSectionPartiallyAccessible;

    private RoadSection destinationRoadSectionAccessible;

    private DirectionalSegment destinationDirectionalSegmentInaccessible;

    private DirectionalSegment destinationDirectionalSegmentAccessible;

    private RoadSection destinationRoadSectionInAccessible;

    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {

        jsonMapper = new JsonMapper();

        roadSectionAccessible = buildRoadSection(1, true, true);
        roadSectionInaccessible = buildRoadSection(2, false, false);
        roadSectionPartiallyAccessible = buildRoadSection(5, false, true);
        destinationRoadSectionAccessible = buildRoadSection(3, true, true);
        destinationRoadSectionInAccessible = buildRoadSection(4, false, false);
        destinationDirectionalSegmentAccessible = buildDestinationDirectionalSegment(3, true, true);
        destinationDirectionalSegmentInaccessible = buildDestinationDirectionalSegment(4, false, false);
        accessibilityResponseGeoJsonMapperV2 = new AccessibilityResponseGeoJsonMapperV2(accessibilityReasonsJsonMapperV2);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true",
            "null"
    }, nullValues = "null")
    void map(Boolean includeAccessibleAndInAccessibleRoadSections) throws JacksonException {

        Accessibility accessibility = Accessibility.builder()
                .toDirectionalSegment(Optional.of(destinationDirectionalSegmentAccessible))
                .combinedAccessibility(List.of(
                        roadSectionAccessible, roadSectionInaccessible,
                        roadSectionPartiallyAccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(includeAccessibleAndInAccessibleRoadSections)
                .includeInaccessibleRoadSections(includeAccessibleAndInAccessibleRoadSections)
                .destination(LocationJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(
                accessibilityRequestJson,
                accessibility);

        assertThatJson(jsonMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
    void map_effectivelyAccessible_defaultBehavior(Boolean effectivelyAccessible) throws JacksonException {

        Accessibility accessibility = Accessibility.builder()
                .toDirectionalSegment(Optional.of(destinationDirectionalSegmentAccessible))
                .combinedAccessibility(List.of(
                        roadSectionAccessible, roadSectionInaccessible, roadSectionPartiallyAccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .effectivelyAccessible(effectivelyAccessible)
                .destination(LocationJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(
                accessibilityRequestJson,
                accessibility);

        assertThatJson(jsonMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
                    }
                  } ]
                }
                """);
    }

    @Test
    void map_effectivelyAccessible_enabled() throws JacksonException {

        Accessibility accessibility = Accessibility.builder()
                .toDirectionalSegment(Optional.of(destinationDirectionalSegmentAccessible))
                .combinedAccessibility(List.of(
                        roadSectionAccessible, roadSectionInaccessible,
                        roadSectionPartiallyAccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .effectivelyAccessible(true)
                .destination(LocationJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(
                accessibilityRequestJson,
                accessibility);

        assertThatJson(jsonMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
                    }
                  } ]
                }
                """);
    }

    @Test
    void map_destinationInaccessible() throws JacksonException {

        when(accessibilityReasonsJsonMapperV2.map(reasons)).thenReturn(List.of(List.of(VehicleTypeReasonJson.builder()
                .type(TypeEnum.VEHICLE_TYPE_REASON)
                .unitSymbol(ReasonUnitSymbolJson.ENUM)
                .values(List.of(VehicleTypeJson.CAR))
                .condition(ReasonConditionJson.EQUALS)
                .becauseOf(List.of(TrafficSignRestrictionJson.builder()
                        .trafficSignId(UUID.fromString("71332fe6-fb88-4a91-8b72-eefc3c37c713"))
                        .trafficSignType(TrafficSignTypeJson.C1)
                        .build()))
                .build())));

        Accessibility accessibility = Accessibility.builder()
                .toDirectionalSegment(Optional.of(destinationDirectionalSegmentInaccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionAccessible))
                .reasons(reasons)
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(true)
                .includeInaccessibleRoadSections(true)
                .destination(LocationJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(
                accessibilityRequestJson,
                accessibility);

        assertThatJson(jsonMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
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
                      "roadSectionId" : 4,
                      "accessible" : false,
                      "reasons" : [ [ {
                        "type" : "vehicleTypeReason",
                        "unitSymbol" : "enum",
                        "condition" : "equals",
                        "becauseOf" : [ {
                          "type" : "trafficSign",
                          "trafficSignId" : "71332fe6-fb88-4a91-8b72-eefc3c37c713",
                          "trafficSignType" : "C1"
                        } ],
                        "requestExemptionUrls" : null,
                        "values" : [ "car" ]
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
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
                      "roadSectionId" : 3,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "roadSectionId" : 3,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
                    }
                  } ]
                }
                """);
    }

    @Test
    void map_onlyAccessible() throws JacksonException {

        Accessibility accessibility = Accessibility.builder()
                .toDirectionalSegment(Optional.of(destinationDirectionalSegmentInaccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionInAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(true)
                .includeInaccessibleRoadSections(false)
                .destination(LocationJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(
                accessibilityRequestJson,
                accessibility);

        assertThatJson(jsonMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
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
                      "roadSectionId" : 4,
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
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
                    }
                  } ]
                }
                """);
    }

    @Test
    void map_onlyInaccessible() throws JacksonException {

        Accessibility accessibility = Accessibility.builder()
                .toDirectionalSegment(Optional.of(destinationDirectionalSegmentAccessible))
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(false)
                .includeInaccessibleRoadSections(true)
                .destination(LocationJson.builder()
                        .latitude(5.34d)
                        .longitude(4.45d)
                        .build())
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(
                accessibilityRequestJson,
                accessibility);

        assertThatJson(jsonMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
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
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
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
                      "roadSectionId" : 2,
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
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
    void map_noDestination(boolean hasAccessibilityRoadSection, boolean hasRequestDestination) throws JacksonException {

        Accessibility accessibility = Accessibility.builder()
                .toDirectionalSegment(hasAccessibilityRoadSection ? Optional.of(destinationDirectionalSegmentAccessible) : Optional.empty())
                .combinedAccessibility(List.of(roadSectionAccessible, roadSectionInaccessible, destinationRoadSectionAccessible))
                .build();

        AccessibilityRequestJson accessibilityRequestJson = AccessibilityRequestJson.builder()
                .includeAccessibleRoadSections(true)
                .includeInaccessibleRoadSections(true)
                .destination(hasRequestDestination
                        ? LocationJson.builder().latitude(5.34d).longitude(4.45d).build()
                        : null)
                .build();

        AccessibilityResponseGeoJsonJson geoJsonResponse = accessibilityResponseGeoJsonMapperV2.map(
                accessibilityRequestJson,
                accessibility);

        assertThatJson(jsonMapper.writeValueAsString(geoJsonResponse)).isEqualTo("""
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
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 2,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 1,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 3,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 4,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 2,
                      "functionalRoadClass" : "1",
                      "accessible" : false,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 5,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 1.0, 2.0 ], [ 2.0, 3.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "forward",
                      "delayInMilliSecondsBecauseOfRestrictions": 23
                    }
                  }, {
                    "type" : "Feature",
                    "id" : 6,
                    "geometry" : {
                      "type" : "LineString",
                      "coordinates" : [ [ 2.0, 3.0 ], [ 1.0, 2.0 ] ]
                    },
                    "properties" : {
                      "type" : "roadSectionSegment",
                      "roadSectionId" : 3,
                      "functionalRoadClass" : "1",
                      "accessible" : true,
                      "direction" : "backward",
                      "delayInMilliSecondsBecauseOfRestrictions": 24
                    }
                  } ]
                }
                """);
    }

    private DirectionalSegment buildDestinationDirectionalSegment(long id, boolean accessibleForward, boolean accessibleBackward) {
        RoadSection roadSection = RoadSection.builder()
                .id(id)
                .functionalRoadClass("1")
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
                .restrictions(new Restrictions(List.of(TrafficSign.builder()
                        .id(4)
                        .roadSectionId(1)
                        .externalId("externalId")
                        .direction(Direction.FORWARD)
                        .fraction(2d)
                        .longitude(3d)
                        .latitude(4d)
                        .supplementaryTrafficSigns(Collections.emptyList())
                        .networkSnappedLatitude(1D)
                        .networkSnappedLongitude(2D)
                        .trafficSignType(TrafficSignType.C7)
                        .transportRestrictions(TransportRestrictions.builder().build())
                        .build())))
                .delayInMilliSecondsBecauseOfRestrictions(23)
                .build();

        DirectionalSegment directionalSegmentBackward = directionalSegmentForward
                .withDirection(Direction.BACKWARD)
                .withAccessible(accessibleBackward)
                .withLineString(new GeometryFactory().createLineString(
                        new Coordinate[]{
                                new Coordinate(2, 3, 0),
                                new Coordinate(1, 2, 0)
                        }))
                .withDelayInMilliSecondsBecauseOfRestrictions(24);

        roadSectionFragment.setForwardSegment(directionalSegmentForward);
        roadSectionFragment.setBackwardSegment(directionalSegmentBackward);

        return directionalSegmentForward;
    }

    private RoadSection buildRoadSection(long id, boolean accessibleForward, boolean accessibleBackward) {
        RoadSection roadSection = RoadSection.builder()
                .id(id)
                .functionalRoadClass("1")
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
                .restrictions(new Restrictions(List.of(TrafficSign.builder()
                        .id(4)
                        .roadSectionId(1)
                        .externalId("externalId")
                        .direction(Direction.FORWARD)
                        .fraction(2d)
                        .longitude(3d)
                        .latitude(4d)
                        .supplementaryTrafficSigns(Collections.emptyList())
                        .networkSnappedLatitude(1D)
                        .networkSnappedLongitude(2D)
                        .trafficSignType(TrafficSignType.C7)
                        .transportRestrictions(TransportRestrictions.builder().build())
                        .build())))
                .delayInMilliSecondsBecauseOfRestrictions(23)
                .build();

        DirectionalSegment directionalSegmentBackward = directionalSegmentForward
                .withDirection(Direction.BACKWARD)
                .withAccessible(accessibleBackward)
                .withLineString(new GeometryFactory().createLineString(
                        new Coordinate[]{
                                new Coordinate(2, 3, 0),
                                new Coordinate(1, 2, 0)
                        })

                )
                .withDelayInMilliSecondsBecauseOfRestrictions(24);

        roadSectionFragment.setForwardSegment(directionalSegmentForward);
        roadSectionFragment.setBackwardSegment(directionalSegmentBackward);

        return roadSection;
    }
}
