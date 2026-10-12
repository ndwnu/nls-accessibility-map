package nu.ndw.nls.accessibilitymap.job.speedlimits.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import nu.ndw.nls.roadattributesapi.client.feign.generated.model.v1.SpeedLimitsRoadSectionResponseJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SpeedLimitsServiceTest {

    private SpeedLimitsService speedLimitsService;

    @Mock
    private SpeedLimitsClient speedLimitsClient;

    @Mock
    private ResponseEntity<SpeedLimitsRoadSectionResponseJson> speedLimitsResponse;

    @BeforeEach
    void setUp() {
        speedLimitsService = new SpeedLimitsService(speedLimitsClient);
    }

    @Test
    void getSpeedLimits() {
        when(speedLimitsClient.getSpeedLimits(LocalDate.of(1999, 12, 1), 2, 1000)).thenReturn(speedLimitsResponse);

        assertThat(speedLimitsService.getSpeedLimits(LocalDate.of(1999, 12, 1), 2, 1000)).isEqualTo(speedLimitsResponse);
    }
}
