package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.backend.controllers.AccessibilityMapApiDelegateImpl.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import nu.ndw.nls.routingmapmatcher.domain.model.accessibility.VehicleProperties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequestMapperTest {

    private static final float VEHICLE_AXLE_WEIGHT = 2F;
    private static final float VEHICLE_HEIGHT = 1.5F;
    private static final float VEHICLE_LENGTH = 3.5F;
    private static final float VEHICLE_WEIGHT = 2.25F;
    private static final float VEHICLE_WIDTH = 2.5F;
    private static final float HGV_VEHICLE_WEIGHT = 3.6F;

    private RequestMapper requestMapper;

    @BeforeEach
    void setup() {
        requestMapper = new RequestMapper();
    }

    @ParameterizedTest
    @MethodSource("provideTestScenarios")
    void mapToVehicleProperties_ok(Pair<VehicleArguments, VehicleProperties> testScenario) {
        VehicleProperties result = requestMapper.mapToVehicleProperties(testScenario.getLeft());
        assertThat(result).isEqualTo(testScenario.getRight());
    }

    private static Stream<Arguments> provideTestScenarios() {

        VehicleArguments carRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.CAR)
                .vehicleAxleWeight(VEHICLE_AXLE_WEIGHT)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(VEHICLE_WEIGHT)
                .vehicleHasTrailer(false)
                .build();

        VehicleProperties expectedCarVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .trailerAccessForbidden(false)
                .axleLoad((double) VEHICLE_AXLE_WEIGHT)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) VEHICLE_WEIGHT)
                .build();

        VehicleArguments lightCommercialVehicleRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.COMMERCIAL_VEHICLE)
                .vehicleAxleWeight(VEHICLE_AXLE_WEIGHT)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(VEHICLE_WEIGHT)
                .vehicleHasTrailer(false)
                .build();

        VehicleArguments hgvCommercialVehicleRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.COMMERCIAL_VEHICLE)
                .vehicleAxleWeight(VEHICLE_AXLE_WEIGHT)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(HGV_VEHICLE_WEIGHT)
                .vehicleHasTrailer(false)
                .build();

        VehicleProperties expectedHgvCommercialVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .hgvAccessForbidden(true)
                .hgvAndAutoBusAccessForbidden(true)
                .trailerAccessForbidden(false)
                .axleLoad((double) VEHICLE_AXLE_WEIGHT)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) HGV_VEHICLE_WEIGHT)
                .build();

        VehicleArguments busRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.BUS)
                .vehicleAxleWeight(VEHICLE_AXLE_WEIGHT)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(HGV_VEHICLE_WEIGHT)
                .vehicleHasTrailer(false)
                .build();

        VehicleProperties expectedBusVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .hgvAndAutoBusAccessForbidden(true)
                .trailerAccessForbidden(false)
                .axleLoad((double) VEHICLE_AXLE_WEIGHT)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) HGV_VEHICLE_WEIGHT)
                .build();

        VehicleArguments tractorRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.TRACTOR)
                .vehicleAxleWeight(VEHICLE_AXLE_WEIGHT)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(HGV_VEHICLE_WEIGHT)
                .vehicleHasTrailer(true)
                .build();

        VehicleProperties expectedTractorVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .trailerAccessForbidden(true)
                .axleLoad((double) VEHICLE_AXLE_WEIGHT)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) HGV_VEHICLE_WEIGHT)
                .build();

        VehicleArguments motorCycleRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.MOTORCYCLE)
                .build();
        VehicleProperties expectedMotorCycleProperties = VehicleProperties
                .builder()
                .motorVehicleAccessForbidden(true)
                .motorBikeAccessForbidden(true)
                .build();

        return Stream.of(
                Arguments.of(
                        named("carScenario",
                                new ImmutablePair<>(carRequest, expectedCarVehicleProperties))),

                Arguments.of(named("lightCommercialVehicleScenario",
                        new ImmutablePair<>(lightCommercialVehicleRequest,
                                expectedCarVehicleProperties))),

                Arguments.of(named("hgvCommercialVehicleScenario",
                        new ImmutablePair<>(hgvCommercialVehicleRequest,
                                expectedHgvCommercialVehicleProperties))),

                Arguments.of(named("busScenario",
                        new ImmutablePair<>(busRequest,
                                expectedBusVehicleProperties))),

                Arguments.of(named("tractorScenario",
                        new ImmutablePair<>(tractorRequest,
                                expectedTractorVehicleProperties))),

                Arguments.of(named("motorcycleScenario",
                        new ImmutablePair<>(motorCycleRequest,
                                expectedMotorCycleProperties)))

        );
    }

}