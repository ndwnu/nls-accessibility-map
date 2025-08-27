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
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.FuelType;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.Restriction;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
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

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.EXCLUDE, names = {"ALL", "BATTERY"})
    void findAll(FuelType fuelType) {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(fuelType)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));

        List<EmissionZone> emissionZones = emissionService.findAll();

        assertThat(emissionZones).containsExactly(emissionZone);
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.INCLUDE, names = {"ALL", "BATTERY"})
    void findAll_invalidEmissionZone(FuelType fuelType) {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(fuelType)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));

        List<EmissionZone> emissionZones = emissionService.findAll();

        assertThat(emissionZones).isEmpty();
        containsLogErrorFuelType();
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

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.EXCLUDE, names = {"ALL", "BATTERY"})
    void findAll_cached(FuelType fuelType) {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(fuelType)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));

        emissionService.findAll();
        List<EmissionZone> emissionZones = emissionService.findAll();

        assertThat(emissionZones).containsExactly(emissionZone);
        verify(emissionZoneClient).findAll();
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.EXCLUDE, names = {"ALL", "BATTERY"})
    void findById(FuelType fuelType) {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(fuelType)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));
        when(emissionZone.id()).thenReturn("id");

        Optional<EmissionZone> emissionZones = emissionService.findById("id");

        assertThat(emissionZones).contains(emissionZone);
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.INCLUDE, names = {"ALL", "BATTERY"})
    void findById_invalidEmissionZone(FuelType fuelType) {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(fuelType)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));

        Optional<EmissionZone> emissionZones = emissionService.findById("id");

        assertThat(emissionZones).isEmpty();
        containsLogErrorFuelType();
    }

    @Test
    void findById_notFound() {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(FuelType.DIESEL)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));
        when(emissionZone.id()).thenReturn("id");

        Optional<EmissionZone> emissionZones = emissionService.findById("otherId");

        assertThat(emissionZones).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.EXCLUDE, names = {"ALL", "BATTERY"})
    void findByTrafficRegulationOrderId(FuelType fuelType) {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(fuelType)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));
        when(emissionZone.trafficRegulationOrderId()).thenReturn("trafficRegulationOrderId");

        Optional<EmissionZone> emissionZones = emissionService.findByTrafficRegulationOrderId("trafficRegulationOrderId");

        assertThat(emissionZones).contains(emissionZone);
    }

    @ParameterizedTest
    @EnumSource(value = FuelType.class, mode = Mode.INCLUDE, names = {"ALL", "BATTERY"})
    void findByTrafficRegulationOrderId_invalidEmissionZone(FuelType fuelType) {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(fuelType)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));

        Optional<EmissionZone> emissionZones = emissionService.findByTrafficRegulationOrderId("trafficRegulationOrderId");

        assertThat(emissionZones).isEmpty();
        containsLogErrorFuelType();
    }

    @Test
    void findByTrafficRegulationOrderId_notFound() {

        when(emissionZone.restriction()).thenReturn(Restriction.builder()
                .fuelType(FuelType.DIESEL)
                .build());
        when(emissionZoneClient.findAll()).thenReturn(List.of(emissionZone));
        when(emissionZone.trafficRegulationOrderId()).thenReturn("trafficRegulationOrderId");

        Optional<EmissionZone> emissionZones = emissionService.findByTrafficRegulationOrderId("otherId");

        assertThat(emissionZones).isEmpty();
    }

    private void containsLogErrorFuelType() {

        loggerExtension.containsLog(Level.ERROR,
                "Fuel type is BATTERY and ALL are not a supported type. This is technically supported in the api specifications"
                        + " from the Emission zone api but it can never be used because there is no exemption available for vehicle types with a"
                        + " zero emission classification in the emission zone api in field"
                        + " `euVehicleCategoryAndEmissionClassificationRestrictionExemptions`. If this does occur than we should contact W&R and"
                        + " Edwin van Wilgenburg about why this is now suddenly possible in the data. We where guaranteed that this combination"
                        + " would never be used as a solution that the exemptions could never support a zero emission classification. For this"
                        + " reason this emission zone will be considered invalid and can not be used.");
    }
}