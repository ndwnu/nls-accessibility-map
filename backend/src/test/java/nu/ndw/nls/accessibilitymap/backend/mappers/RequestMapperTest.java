package nu.ndw.nls.accessibilitymap.backend.mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.backend.controllers.AccessibilityMapApiDelegateImpl.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RequestMapperTest {

    private static final float VEHICLE_AXLE_LOAD = 2F;
    private static final float VEHICLE_HEIGHT = 1.5F;
    private static final float VEHICLE_LENGTH = 4.5F;
    private static final float VEHICLE_WEIGHT = 3.5F;
    private static final float VEHICLE_WIDTH = 2.5F;
    private static final float HGV_VEHICLE_WEIGHT = 3.6F;

    private RequestMapper requestMapper;

    @BeforeEach
    void setup() {
        requestMapper = new RequestMapper();
    }

    @ParameterizedTest
    @MethodSource("provideTestScenarios")
    void mapToVehicleProperties(Pair<VehicleArguments, VehicleProperties> testScenario) {
        VehicleProperties result = requestMapper.mapToVehicleProperties(testScenario.getLeft());
        assertThat(result).isEqualTo(testScenario.getRight());
    }

    private static Stream<Arguments> provideTestScenarios() {
        VehicleArguments carRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.CAR)
                .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(VEHICLE_WEIGHT)
                .build();

        VehicleProperties expectedCarVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .axleLoad((double) VEHICLE_AXLE_LOAD)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) VEHICLE_WEIGHT)
                .build();

        VehicleArguments lightCommercialVehicleRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.LIGHT_COMMERCIAL_VEHICLE)
                .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(VEHICLE_WEIGHT)
                .build();

        VehicleProperties expectedLightCommercialVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .lcvAndHgvAccessForbidden(true)
                .axleLoad((double) VEHICLE_AXLE_LOAD)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) VEHICLE_WEIGHT)
                .build();

        VehicleArguments truckRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.TRUCK)
                .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(HGV_VEHICLE_WEIGHT)
                .build();

        VehicleProperties expectedHgvCommercialVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .hgvAccessForbidden(true)
                .hgvAndBusAccessForbidden(true)
                .lcvAndHgvAccessForbidden(true)
                .axleLoad((double) VEHICLE_AXLE_LOAD)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) HGV_VEHICLE_WEIGHT)
                .build();

        VehicleArguments busRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.BUS)
                .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
                .vehicleWidth(VEHICLE_WIDTH)
                .vehicleHeight(VEHICLE_HEIGHT)
                .vehicleLength(VEHICLE_LENGTH)
                .vehicleWeight(HGV_VEHICLE_WEIGHT)
                .build();

        VehicleProperties expectedBusVehicleProperties = VehicleProperties
                .builder()
                .carAccessForbidden(true)
                .motorVehicleAccessForbidden(true)
                .busAccessForbidden(true)
                .hgvAndBusAccessForbidden(true)
                .axleLoad((double) VEHICLE_AXLE_LOAD)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) HGV_VEHICLE_WEIGHT)
                .build();

        VehicleArguments tractorRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.TRACTOR)
                .vehicleAxleLoad(VEHICLE_AXLE_LOAD)
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
                .tractorAccessForbidden(true)
                .slowVehicleAccessForbidden(true)
                .trailerAccessForbidden(true)
                .axleLoad((double) VEHICLE_AXLE_LOAD)
                .height((double) VEHICLE_HEIGHT)
                .width((double) VEHICLE_WIDTH)
                .length((double) VEHICLE_LENGTH)
                .width((double) VEHICLE_WIDTH)
                .weight((double) HGV_VEHICLE_WEIGHT)
                .build();

        VehicleArguments motorcycleRequest = VehicleArguments
                .builder()
                .vehicleType(VehicleTypeJson.MOTORCYCLE)
                .build();

        VehicleProperties expectedMotorcycleVehicleProperties = VehicleProperties
                .builder()
                .motorVehicleAccessForbidden(true)
                .motorcycleAccessForbidden(true)
                .build();

        return Stream.of(
                Arguments.of(named("carScenario",
                        new ImmutablePair<>(carRequest, expectedCarVehicleProperties))),

                Arguments.of(named("lightCommercialVehicleScenario",
                        new ImmutablePair<>(lightCommercialVehicleRequest, expectedLightCommercialVehicleProperties))),

                Arguments.of(named("truckScenario",
                        new ImmutablePair<>(truckRequest, expectedHgvCommercialVehicleProperties))),

                Arguments.of(named("busScenario",
                        new ImmutablePair<>(busRequest, expectedBusVehicleProperties))),

                Arguments.of(named("tractorScenario",
                        new ImmutablePair<>(tractorRequest, expectedTractorVehicleProperties))),

                Arguments.of(named("motorcycleScenario",
                        new ImmutablePair<>(motorcycleRequest, expectedMotorcycleVehicleProperties)))
        );
    }
}
