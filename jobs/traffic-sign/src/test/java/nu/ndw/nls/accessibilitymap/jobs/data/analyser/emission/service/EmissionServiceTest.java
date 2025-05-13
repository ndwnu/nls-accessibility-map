package nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import feign.FeignException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.client.EmissionZoneClient;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZone;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionServiceTest {

    private EmissionService emissionService;

    @Mock
    private EmissionZoneClient emissionZoneClient;

    @Mock
    private EmissionZone emissionZone;

    @Mock
    private ConstraintViolationException constraintViolationException;

    @Mock
    private FeignException.FeignClientException feignClientException;

    @Mock
    private FeignException.FeignServerException feignServerException;

    @RegisterExtension
    private final LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        emissionService = new EmissionService(emissionZoneClient);
    }

    @Test
    void findAll() {

        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));

        List<EmissionZone> emissionZones = emissionService.findAll();

        assertThat(emissionZones).containsExactly(emissionZone);
    }

    @Test
    void findAll_feignClientException() {

        when(emissionZoneClient.findAll()).thenThrow(feignClientException);
        when(feignClientException.getMessage()).thenReturn("message");

        assertThat(emissionService.findAll()).isEmpty();

        loggerExtension.containsLog(Level.ERROR, "No emission information available.", "message");
    }

    @Test
    void findAll_constraintViolationException() {

        when(emissionZoneClient.findAll()).thenThrow(constraintViolationException);
        when(constraintViolationException.getMessage()).thenReturn("message");

        assertThat(emissionService.findAll()).isEmpty();

        loggerExtension.containsLog(
                Level.ERROR,
                "Something went wrong with getting emission information from the Road Features Area API.",
                "message");
    }

    @Test
    void findAll_feignServerException() {

        when(emissionZoneClient.findAll()).thenThrow(feignServerException);
        when(feignServerException.getMessage()).thenReturn("message");

        assertThat(emissionService.findAll()).isEmpty();

        loggerExtension.containsLog(
                Level.ERROR,
                "Something went wrong with getting emission information from the Road Features Area API.",
                "message");
    }

    @Test
    void findAll_runtimeException() {

        RuntimeException runtimeException = new RuntimeException("message");

        when(emissionZoneClient.findAll()).thenThrow(runtimeException);

        assertThatThrownBy(() -> emissionService.findAll()).isSameAs(runtimeException);

        loggerExtension.containsLog(Level.WARN, "Error while retrieving emission. Retrying at a later moment.", "message");
    }

    @Test
    void findAll_cached() {

        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));

        emissionService.findAll();
        List<EmissionZone> emissionZones = emissionService.findAll();

        assertThat(emissionZones).containsExactly(emissionZone);
        verify(emissionZoneClient).findAll();
    }

    @Test
    void findById() {

        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));
        when(emissionZone.id()).thenReturn("id");

        Optional<EmissionZone> emissionZones = emissionService.findById("id");

        assertThat(emissionZones).contains(emissionZone);
    }

    @Test
    void findById_notFound() {

        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));
        when(emissionZone.id()).thenReturn("id");

        Optional<EmissionZone> emissionZones = emissionService.findById("otherId");

        assertThat(emissionZones).isEmpty();
    }
}