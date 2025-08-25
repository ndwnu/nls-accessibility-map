package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.TransportTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.VehicleTypeJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransportTypeMapperTest {

    private TransportTypeMapper transportTypeMapper;

    @BeforeEach
    void setup() {
        transportTypeMapper = new TransportTypeMapper();
    }

    /**
     * Verifies mapping when vehicle type is CAR and has no trailer.
     */
    @Test
    void mapToTransportType_ok_carWithoutTrailer() {
        VehicleArguments vehicleArguments = VehicleArguments.builder()
                .vehicleType(VehicleTypeJson.CAR)
                .vehicleHasTrailer(false)
                .build();

        Set<TransportType> result = transportTypeMapper.mapToTransportType(vehicleArguments);

        Assertions.assertEquals(Set.of(TransportType.CAR), result);
    }

    /**
     * Verifies mapping when vehicle type is TRUCK and has a trailer.
     */
    @Test
    void mapToTransportType_ok_truckWithTrailer() {
        VehicleArguments vehicleArguments = VehicleArguments.builder()
                .vehicleType(VehicleTypeJson.TRUCK)
                .vehicleHasTrailer(true)
                .build();

        Set<TransportType> result = transportTypeMapper.mapToTransportType(vehicleArguments);

        Assertions.assertEquals(Set.of(TransportType.TRUCK, TransportType.VEHICLE_WITH_TRAILER), result);
    }

    /**
     * Verifies mapping when vehicle type is BUS and has no trailer.
     */
    @Test
    void mapToTransportType_ok_busWithoutTrailer() {
        VehicleArguments vehicleArguments = VehicleArguments.builder()
                .vehicleType(VehicleTypeJson.BUS)
                .vehicleHasTrailer(false)
                .build();

        Set<TransportType> result = transportTypeMapper.mapToTransportType(vehicleArguments);

        Assertions.assertEquals(Set.of(TransportType.BUS), result);
    }

    /**
     * Verifies mapping when vehicle type is LIGHT_COMMERCIAL_VEHICLE and has a trailer.
     */
    @Test
    void mapToTransportType_ok_lightCommercialVehicleWithTrailer() {
        VehicleArguments vehicleArguments = VehicleArguments.builder()
                .vehicleType(VehicleTypeJson.LIGHT_COMMERCIAL_VEHICLE)
                .vehicleHasTrailer(true)
                .build();

        Set<TransportType> result = transportTypeMapper.mapToTransportType(vehicleArguments);

        Assertions.assertEquals(Set.of(TransportType.DELIVERY_VAN, TransportType.VEHICLE_WITH_TRAILER), result);
    }

    /**
     * Verifies mapping when vehicle type is MOTORCYCLE without a trailer.
     */
    @Test
    void mapToTransportType_ok_motorcycleWithoutTrailer() {
        VehicleArguments vehicleArguments = VehicleArguments.builder()
                .vehicleType(VehicleTypeJson.MOTORCYCLE)
                .vehicleHasTrailer(false)
                .build();

        Set<TransportType> result = transportTypeMapper.mapToTransportType(vehicleArguments);

        Assertions.assertEquals(Set.of(TransportType.MOTORCYCLE), result);
    }

    /**
     * Verifies mapping when vehicle type is TRACTOR with a trailer.
     */
    @Test
    void mapToTransportType_ok_tractorWithTrailer() {
        VehicleArguments vehicleArguments = VehicleArguments.builder()
                .vehicleType(VehicleTypeJson.TRACTOR)
                .vehicleHasTrailer(true)
                .build();

        Set<TransportType> result = transportTypeMapper.mapToTransportType(vehicleArguments);

        Assertions.assertEquals(Set.of(TransportType.TRACTOR, TransportType.VEHICLE_WITH_TRAILER), result);
    }

    @Test
    void mapTransportTypeToJson_ok() {
        Set<TransportType> transportTypeSet = Set.of(TransportType.CAR, TransportType.BUS, TransportType.MOTORCYCLE);
        List<VehicleTypeJson> expectedList = List.of(VehicleTypeJson.CAR, VehicleTypeJson.BUS, VehicleTypeJson.MOTORCYCLE);

        List<VehicleTypeJson> actualList = transportTypeMapper.mapTransportTypeToJson(transportTypeSet);

        assertThat(actualList).containsExactlyInAnyOrderElementsOf(expectedList);
    }
}
