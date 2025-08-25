package nu.ndw.nls.accessibilitymap.accessibility.reason.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C10;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C11;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C12;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C17;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C18;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C20;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C21;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22A;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C22C;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C6;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7A;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C7B;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C8;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType.C9;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReasons;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction.RestrictionType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.MaximumRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import org.junit.jupiter.api.BeforeEach;
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
    void mapToAoAccessibilityReasons(TrafficSign trafficSign, AccessibilityReason expectedReason) {

        AccessibilityReasons actual = mapper.mapToAoAccessibilityReasons(List.of(trafficSign));
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("accessibilityReason")
                .isEqualTo(new AccessibilityReasons(List.of(expectedReason)));
    }

    private static Stream<Arguments> provideReasons() {

        return Stream.of(
                Arguments.of(
                        createTrafficSign(
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C7),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C7)),
                Arguments.of(createTrafficSign(
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C9),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C9)),

                Arguments.of(createTrafficSign(
                                Restrictions.builder()
                                        .transportTypes(Set.of(TransportType.CAR))
                                        .build(),
                                C10),
                        createAccessibilityReason(
                                List.of(TransportTypeRestriction.builder()
                                        .value(Set.of(TransportType.CAR))
                                        .build()),
                                C10)),

                Arguments.of(createTrafficSign(
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                Restrictions.builder()
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
                                List.of(MaximumRestriction.builder()
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
                                Restrictions.builder()
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
                                List.of(MaximumRestriction.builder()
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

    private static TrafficSign createTrafficSign(Restrictions restrictions, TrafficSignType trafficSignType) {

        return TrafficSign.builder()
                .direction(Direction.FORWARD)
                .externalId(EXTERNAL_ID)
                .roadSectionId(ROAD_SECTION_ID)
                .trafficSignType(trafficSignType)
                .restrictions(restrictions)
                .build();
    }

    private static AccessibilityReason createAccessibilityReason(
            List<AccessibilityRestriction> restrictions,
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

}
