package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C10;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C11;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C12;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C17;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C18;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C20;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C21;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C22;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C22A;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C22C;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C6;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C7;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C7A;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C7B;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C8;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType.C9;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasonRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.reason.dto.TransportTypeRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AccessibilityReasonsMapperTest {

    private static final String EXTERNAL_ID = "id";

    private static final int ROAD_SECTION_ID = 1;

    private static final double VEHICLE_LENGTH_CM = 10d;

    private static final double VEHICLE_WIDTH_CM = 20D;

    private static final double VEHICLE_HEIGHT_CM = 30D;

    private static final double MAXIMUM_AXLE_LOAD = 40D;

    private static final double VEHICLE_WEIGHT = 3D;

    private AccessibilityReasonsMapper mapper;

    @BeforeEach
    void setUp() {

        mapper = new AccessibilityReasonsMapper();
    }

    @ParameterizedTest
    @MethodSource("provideReasons")
    void mapRestrictions(TrafficSign trafficSign, AccessibilityReason expectedReason) {

        AccessibilityReasons actual = mapper.mapRestrictions(new Restrictions(Set.of(trafficSign)));
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("accessibilityReason")
                .isEqualTo(new AccessibilityReasons(List.of(expectedReason)));
    }

    @Test
    void mapRestrictions_unsupportedRestriction() {

        assertThat(catchThrowable(() -> mapper.mapRestrictions(new Restrictions(Set.of(new UnsupportedRestriction())))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Restriction type class nu.ndw.nls.accessibilitymap.accessibility.reason.mapper"
                            + ".AccessibilityReasonsMapperTest$UnsupportedRestriction is not supported");
    }

    @Test
    void mapRestrictions_unsupportedTrafficSignType() {

        TrafficSignType trafficSignType = mock(TrafficSignType.class);

        assertThat(catchThrowable(() -> mapper.mapRestrictions(new Restrictions(Set.of(createTrafficSign(
                TransportRestrictions.builder()
                        .transportTypes(Set.of(TransportType.CAR))
                        .build(),
                trafficSignType))))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Traffic sign type '%s' is not supported".formatted(trafficSignType));
    }

    private static Stream<Arguments> provideReasons() {

        return Stream.of(
                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C1),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C1)),
                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C6),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C6)),
                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C7),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C7)),
                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C7A),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C7A)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C7B),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C7B)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C8),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C8)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C9),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C9)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C10),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C10)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C11),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C11)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C12),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C12)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .vehicleLengthInCm(Maximum.builder()
                                                .value(VEHICLE_LENGTH_CM)
                                                .build())
                                        .build(), C17),
                        createAccessibilityReason(
                                List.of(MaximumRestriction.builder()
                                        .restrictionType(MaximumRestriction.RestrictionType.VEHICLE_LENGTH)
                                        .value(Maximum.builder()
                                                .value(VEHICLE_LENGTH_CM)
                                                .build())
                                        .build()),
                                C17))
                ,

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .vehicleWidthInCm(Maximum.builder()
                                                .value(VEHICLE_WIDTH_CM)
                                                .build())
                                        .build(),
                                C18),
                        createAccessibilityReason(
                                List.of(MaximumRestriction.builder()
                                        .restrictionType(MaximumRestriction.RestrictionType.VEHICLE_WIDTH)
                                        .value(Maximum.builder()
                                                .value(VEHICLE_WIDTH_CM)
                                                .build())
                                        .build()),
                                C18)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .vehicleHeightInCm(Maximum.builder()
                                                .value(VEHICLE_HEIGHT_CM)
                                                .build())
                                        .build(), C19),
                        createAccessibilityReason(
                                List.of(MaximumRestriction.builder()
                                        .restrictionType(RestrictionType.VEHICLE_HEIGHT)
                                        .value(Maximum.builder()
                                                .value(VEHICLE_HEIGHT_CM)
                                                .build())
                                        .build()),
                                C19)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .vehicleAxleLoadInKg(Maximum.builder()
                                                .value(MAXIMUM_AXLE_LOAD)
                                                .build())
                                        .build(),
                                C20),
                        createAccessibilityReason(
                                List.of(MaximumRestriction.builder()
                                        .restrictionType(RestrictionType.VEHICLE_AXLE_LOAD)
                                        .value(Maximum.builder()
                                                .value(MAXIMUM_AXLE_LOAD)
                                                .build())
                                        .build()),
                                C20)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .vehicleWeightInKg(Maximum.builder()
                                                .value(MAXIMUM_AXLE_LOAD)
                                                .build())
                                        .build(),
                                C21),
                        createAccessibilityReason(
                                List.of(MaximumRestriction.builder()
                                        .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                                        .value(Maximum.builder()
                                                .value(MAXIMUM_AXLE_LOAD)
                                                .build())
                                        .build()),
                                C21)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C22),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C22)),
                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .emissionZone(EmissionZone.builder()
                                                .restriction(EmissionZoneRestriction
                                                        .builder()
                                                        .vehicleWeightInKg(Maximum.builder().value(VEHICLE_WEIGHT).build())
                                                        .transportTypes(Set.of(TransportType.CAR))
                                                        .fuelTypes(Set.of(FuelType.ELECTRIC))
                                                        .build())
                                                .build())
                                        .build(),
                                C22A),
                        createAccessibilityReason(
                                List.of(
                                        MaximumRestriction.builder()
                                                .value(Maximum.builder().value(VEHICLE_WEIGHT).build())
                                                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                                                .build(),
                                        FuelTypeRestriction.builder()
                                                .value(Set.of(FuelType.ELECTRIC))
                                                .build(),
                                        TransportTypeRestriction.builder()
                                                .value(Set.of(TransportType.CAR))
                                                .build()
                                ),
                                C22A)),

                Arguments.of(
                        createTrafficSign(
                                TransportRestrictions.builder()
                                        .emissionZone(EmissionZone.builder()
                                                .restriction(EmissionZoneRestriction
                                                        .builder()
                                                        .vehicleWeightInKg(Maximum.builder().value(VEHICLE_WEIGHT).build())
                                                        .transportTypes(Set.of(TransportType.CAR))
                                                        .fuelTypes(Set.of(FuelType.ELECTRIC))
                                                        .build())
                                                .build()).build(),
                                C22C),
                        createAccessibilityReason(
                                List.of(
                                        MaximumRestriction.builder()
                                                .value(Maximum.builder().value(VEHICLE_WEIGHT).build())
                                                .restrictionType(RestrictionType.VEHICLE_WEIGHT)
                                                .build(),
                                        FuelTypeRestriction.builder()
                                                .value(Set.of(FuelType.ELECTRIC))
                                                .build(),
                                        TransportTypeRestriction.builder()
                                                .value(Set.of(TransportType.CAR))
                                                .build()
                                ),
                                C22C))

        );
    }

    private static TrafficSign createTrafficSign(TransportRestrictions transportRestrictions, TrafficSignType trafficSignType) {

        return TrafficSign.builder()
                .direction(Direction.FORWARD)
                .externalId(EXTERNAL_ID)
                .roadSectionId(ROAD_SECTION_ID)
                .trafficSignType(trafficSignType)
                .transportRestrictions(transportRestrictions)
                .build();
    }

    private static AccessibilityReason createAccessibilityReason(
            List<AccessibilityReasonRestriction> restrictions,
            TrafficSignType trafficSignType) {

        AccessibilityReason reason = AccessibilityReason.builder()
                .trafficSignType(trafficSignType)
                .direction(Direction.FORWARD)
                .trafficSignExternalId(EXTERNAL_ID)
                .roadSectionId(ROAD_SECTION_ID)
                .restrictions(restrictions)
                .build();

        restrictions.forEach(r -> r.setAccessibilityReason(reason));
        return reason;
    }

    private class UnsupportedRestriction implements Restriction {

        @Override
        public boolean isRestrictive(AccessibilityRequest accessibilityRequest) {
            return false;
        }

        @Override
        public Double networkSnappedLatitude() {
            return 0.0;
        }

        @Override
        public Double networkSnappedLongitude() {
            return 0.0;
        }

        @Override
        public Integer roadSectionId() {
            return 0;
        }

        @Override
        public Direction direction() {
            return null;
        }

        @Override
        public boolean isDynamic() {
            return false;
        }
    }
}
