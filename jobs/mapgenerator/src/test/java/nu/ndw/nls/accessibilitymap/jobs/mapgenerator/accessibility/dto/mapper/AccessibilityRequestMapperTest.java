package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
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

        ExportProperties exportProperties = ExportProperties.builder()
                .trafficSignTypes(List.of(TrafficSignType.C7))
                .vehicleProperties(VehicleProperties.builder().build())
                .includeOnlyTimeWindowedSigns(true)
                .startLocationLatitude(52.12096528507054)
                .startLocationLongitude(5.334845116067081)
                .generateConfiguration(GenerateConfiguration.builder()
                        .searchRadiusInMeters(1000000)
                        .build())
                .build();

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapper.map(exportProperties);

        assertThat(accessibilityRequest).isNotNull();
        assertThat(accessibilityRequest.getVehicleProperties()).isEqualTo(exportProperties.vehicleProperties());
        assertThat(accessibilityRequest.getStartLocationLatitude())
                .isEqualTo(exportProperties.startLocationLatitude());
        assertThat(accessibilityRequest.getStartLocationLongitude())
                .isEqualTo(exportProperties.startLocationLongitude());
        assertThat(accessibilityRequest.getSearchRadiusInMeters())
                .isEqualTo(exportProperties.generateConfiguration().searchRadiusInMeters());
        assertThat(accessibilityRequest.getTrafficSignTypes()).isEqualTo(exportProperties.trafficSignTypes());
        assertThat(accessibilityRequest.isIncludeOnlyTimeWindowedSigns())
                .isEqualTo(exportProperties.includeOnlyTimeWindowedSigns());
    }
}
