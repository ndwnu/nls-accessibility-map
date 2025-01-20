package nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.accessibility.services.accessibility.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.jobs.trafficsignanalyser.command.dto.AnalyseProperties;
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

        AnalyseProperties analyseProperties = AnalyseProperties.builder()
                .trafficSignType(TrafficSignType.C7)
                .vehicleProperties(VehicleProperties.builder().build())
                .startLocationLatitude(52.12096528507054)
                .startLocationLongitude(5.334845116067081)
                .searchRadiusInMeters(1000000)
                .build();

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapper.map(analyseProperties);

        assertThat(accessibilityRequest).isNotNull();
        assertThat(accessibilityRequest.getVehicleProperties()).isEqualTo(analyseProperties.vehicleProperties());
        assertThat(accessibilityRequest.getStartLocationLatitude()).isEqualTo(analyseProperties.startLocationLatitude());
        assertThat(accessibilityRequest.getStartLocationLongitude()).isEqualTo(analyseProperties.startLocationLongitude());
        assertThat(accessibilityRequest.getSearchRadiusInMeters()).isEqualTo(analyseProperties.searchRadiusInMeters());
        assertThat(accessibilityRequest.getTrafficSignTypes()).isEqualTo(List.of(analyseProperties.trafficSignType()));
        assertThat(accessibilityRequest.isIncludeOnlyTimeWindowedSigns()).isFalse();
    }
}