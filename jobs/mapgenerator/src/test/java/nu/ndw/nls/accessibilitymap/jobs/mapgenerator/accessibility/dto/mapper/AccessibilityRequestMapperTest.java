package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestMapperTest {

    private AccessibilityRequestMapper accessibilityRequestMapper;

    @BeforeEach
    void setUp() {

        accessibilityRequestMapper = new AccessibilityRequestMapper();
    }

    @Test
    void map() {

        GeoGenerationProperties geoGenerationProperties = GeoGenerationProperties.builder()
                .trafficSignType(TrafficSignType.C7)
                .vehicleProperties(VehicleProperties.builder().build())
                .includeOnlyTimeWindowedSigns(true)
                .startLocationLatitude(52.12096528507054)
                .startLocationLongitude(5.334845116067081)
                .generateConfiguration(GenerateConfiguration.builder()
                        .searchRadiusInMeters(1000000)
                        .build())
                .build();

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapper.map(geoGenerationProperties);

        assertThat(accessibilityRequest).isNotNull();
        assertThat(accessibilityRequest.getVehicleProperties()).isEqualTo(geoGenerationProperties.vehicleProperties());
        assertThat(accessibilityRequest.getStartLocationLatitude())
                .isEqualTo(geoGenerationProperties.startLocationLatitude());
        assertThat(accessibilityRequest.getStartLocationLongitude())
                .isEqualTo(geoGenerationProperties.startLocationLongitude());
        assertThat(accessibilityRequest.getSearchRadiusInMeters())
                .isEqualTo(geoGenerationProperties.generateConfiguration().searchRadiusInMeters());
        assertThat(accessibilityRequest.getTrafficSignType()).isEqualTo(geoGenerationProperties.trafficSignType());
        assertThat(accessibilityRequest.isIncludeOnlyTimeWindowedSigns())
                .isEqualTo(geoGenerationProperties.includeOnlyTimeWindowedSigns());
    }
}