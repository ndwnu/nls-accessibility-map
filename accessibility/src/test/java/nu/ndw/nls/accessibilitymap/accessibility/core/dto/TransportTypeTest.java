package nu.ndw.nls.accessibilitymap.accessibility.core.dto;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.BUS;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType.CAR;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportTypeTest {

    @ParameterizedTest
    @EnumSource(value = TransportType.class)
    void allExcept(TransportType excludingTransportType) {

        Set<TransportType> expectedTransportTypes = Stream.of(TransportType.values())
                .filter(transportType -> excludingTransportType != transportType)
                .collect(Collectors.toSet());

        assertThat(TransportType.allExcept(excludingTransportType)).doesNotContain(excludingTransportType);
        assertThat(TransportType.allExcept(excludingTransportType)).containsAll(expectedTransportTypes);
    }

    @Test
    void allExcept() {
        Set<TransportType> expectedTransportTypes = Stream.of(TransportType.values())
                .filter(transportType -> CAR != transportType)
                .filter(transportType -> BUS != transportType)
                .collect(Collectors.toSet());

        assertThat(TransportType.allExcept(CAR, BUS)).doesNotContain(CAR, BUS);
        assertThat(TransportType.allExcept(CAR, BUS)).containsAll(expectedTransportTypes);
    }

    @ParameterizedTest
    @EnumSource(value = TransportType.class)
    void getType(TransportType transportType) {
        assertThat(transportType.getType()).isEqualTo(expectedType(transportType));
    }

    @ParameterizedTest
    @EnumSource(value = TransportType.class)
    void fromValue(TransportType transportType) {
        assertThat(TransportType.fromValue(transportType.getType())).isEqualTo(transportType);
    }

    @Test
    void fromValue_unknown_throwsException() {
        assertThatThrownBy(() -> TransportType.fromValue("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected value 'unknown'");
    }

    private static String expectedType(TransportType transportType) {
        return switch (transportType) {
            case PEDESTRIAN -> "Pedestrian";
            case BICYCLE -> "Bicycle";
            case MOPED -> "Moped";
            case MOTORCYCLE -> "Motorcycle";
            case CARAVAN -> "Caravan";
            case CAR -> "Car";
            case TRUCK -> "Truck";
            case TRACTOR -> "Tractor";
            case VEHICLE_WITH_TRAILER -> "VehicleWithTrailer";
            case VEHICLE_WITH_DANGEROUS_SUPPLIES -> "VehicleWithDangerousSupplies";
            case DELIVERY_VAN -> "DeliveryVan";
            case RIDERS -> "Riders";
            case CONDUCTORS -> "Conductors";
            case BUS -> "Bus";
            case TRAM -> "Tram";
            case TAXI -> "Taxi";
        };
    }
}
