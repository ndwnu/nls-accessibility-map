package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.VehicleTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VehicleToTransportTypeMapperTest {

    private final VehicleToTransportTypeMapper vehicleToTransportTypeMapper = new VehicleToTransportTypeMapper();

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
            MICROCAR,               null
            PEDESTRIAN,             PEDESTRIAN
            TRUCK,                  TRUCK
            DELIVERY_VAN,           DELIVERY_VAN
            RIDER,                  RIDERS
            TRAM,                   TRAM
            TAXI,                   TAXI,
            null,                   null
            """, nullValues = "null")
    void map(VehicleTypeEnum vehicleTypeEnum,  TransportType transportType) {
        assertThat(vehicleToTransportTypeMapper.map(vehicleTypeEnum)).isEqualTo(transportType);
    }

}