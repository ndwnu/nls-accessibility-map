package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VehiclePropertiesMapperTest {

    private VehiclePropertiesMapper vehiclePropertiesMapper;

    @BeforeEach
    void setUp() {
        vehiclePropertiesMapper = new VehiclePropertiesMapper();
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    void map_ok(List<TrafficSignType> trafficSignTypes, boolean includeOnlyWindowTimes, VehicleProperties expected) {
        assertThat(vehiclePropertiesMapper.map(trafficSignTypes, includeOnlyWindowTimes))
                .isEqualTo(expected);
    }

    @Test
    void map_ok_allTrafficSignsInTestCase() {
        Set<TrafficSignType> types = (Set<TrafficSignType>) provideTestData().map(a -> a.get()[0])
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Set<TrafficSignType> missingTrafficSignTypes = Sets.difference(Arrays.stream(TrafficSignType.values())
                        .collect(Collectors.toSet()),
                types);
        assertThat(missingTrafficSignTypes)
                .withFailMessage("Missing traffic sign mapping in vehiclePropertiesMapper or in testcase {}", missingTrafficSignTypes)
                .isEmpty();
    }

    private static Stream<Arguments> provideTestData() {
        return Stream.of(
                Arguments.of(List.of(TrafficSignType.C6), true, VehicleProperties.builder()
                        .carAccessForbiddenWt(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C6), false, VehicleProperties.builder()
                        .carAccessForbiddenWt(true)
                        .carAccessForbidden(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C7), true, VehicleProperties.builder()
                        .hgvAccessForbiddenWt(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C7), false, VehicleProperties.builder()
                        .hgvAccessForbiddenWt(true)
                        .hgvAccessForbidden(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C7A), false, VehicleProperties.builder()
                        .busAccessForbidden(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C7B), true, VehicleProperties.builder()
                        .hgvAndBusAccessForbiddenWt(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C7B), false, VehicleProperties.builder()
                        .hgvAndBusAccessForbiddenWt(true)
                        .hgvAndBusAccessForbidden(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C10), false, VehicleProperties.builder()
                        .trailerAccessForbidden(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C12), true, VehicleProperties.builder()
                        .motorVehicleAccessForbiddenWt(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C12), false, VehicleProperties.builder()
                        .motorVehicleAccessForbidden(true)
                        .motorVehicleAccessForbiddenWt(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C22C), true, VehicleProperties.builder()
                        .lcvAndHgvAccessForbiddenWt(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C22C), false, VehicleProperties.builder()
                        .lcvAndHgvAccessForbidden(true)
                        .lcvAndHgvAccessForbiddenWt(true)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C17), false, VehicleProperties.builder()
                        .length(22D)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C18), false, VehicleProperties.builder()
                        .width(3D)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C19), false, VehicleProperties.builder()
                        .height(4D)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C20), false, VehicleProperties.builder()
                        .axleLoad(12D)
                        .build()),
                Arguments.of(List.of(TrafficSignType.C21), false, VehicleProperties.builder()
                        .weight(60D)
                        .build())

        );

    }

}
