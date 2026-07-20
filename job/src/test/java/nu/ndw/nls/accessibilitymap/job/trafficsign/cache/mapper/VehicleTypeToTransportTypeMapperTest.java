package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.VehicleTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VehicleTypeToTransportTypeMapperTest {

    private final VehicleTypeToTransportTypeMapper vehicleTypeToTransportTypeMapper = new VehicleTypeToTransportTypeMapper();

    @ParameterizedTest
    @CsvSource(textBlock = """
            BICYCLE,                BICYCLE
            CAR,                    CAR
            BUS,                    BUS
            MOPED,                  MOPED
            MOTORCYCLE,             MOTORCYCLE
            AGRICULTURAL_VEHICLE,   TRACTOR
            CARAVAN,                CARAVAN
            TRAILER,                VEHICLE_WITH_TRAILER
            MICROCAR,               CAR
            PEDESTRIAN,             PEDESTRIAN
            TRUCK,                  TRUCK
            DELIVERY_VAN,           DELIVERY_VAN
            RIDER,                  RIDERS
            TRAM,                   TRAM
            TAXI,                   TAXI,
            """)
    void map_singleValues(VehicleTypeEnum vehicleTypeEnum, TransportType transportType) {
        assertThat(vehicleTypeToTransportTypeMapper.map(vehicleTypeEnum))
                .containsExactly(transportType);
    }

    @Test
    void map_null() {
        assertThat(vehicleTypeToTransportTypeMapper.map(null))
                .isEmpty();
    }

    @Test
    void map_all() {
        assertThat(vehicleTypeToTransportTypeMapper.map(VehicleTypeEnum.ALL))
                .containsExactlyInAnyOrder(TransportType.values());
    }
}
