package nu.ndw.nls.accessibilitymap.jobs.data.analyser.cache.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneExemption;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZoneStatus;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EuroClassification;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.Exemption;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.Restriction;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.VehicleCategory;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.VehicleType;
import nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.service.EmissionService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionZoneMapperTest {

    private static final String EMISSION_ZONE_ID = "emissionZoneId";

    private EmissionZoneMapper emissionZoneMapper;

    @Mock
    private EmissionService emissionService;

    @Mock
    private MaximumWeightMapper maximumWeightMapper;

    @Mock
    private EmissionClassMapper emissionClassMapper;

    @Mock
    private EmissionZoneTypeMapper emissionZoneTypeMapper;

    @Mock
    private FuelTypeMapper fuelTypeMapper;

    @Mock
    private TransportTypeMapper transportTypeMapper;

    private nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZone emissionZoneDto;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUp() {

        emissionZoneDto = nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.EmissionZone.builder()
                .id(EMISSION_ZONE_ID)
                .type(EmissionZoneType.LOW_EMISSION_ZONE)
                .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .endTime(OffsetDateTime.parse("2022-03-11T10:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .status(EmissionZoneStatus.ACTIVE)
                .exemptions(List.of(Exemption.builder()
                        .id("exemption")
                        .startTime(OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .endTime(OffsetDateTime.parse("2022-03-11T10:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .euroClassifications(Set.of(EuroClassification.EURO_1))
                        .vehicleWeightInKg(2)
                        .vehicleCategories(Set.of(VehicleCategory.M_1))
                        .build()))
                .restriction(Restriction.builder()
                        .id("restrictionId")
                        .vehicleCategories(Set.of(VehicleCategory.M_2))
                        .fuelType(nu.ndw.nls.accessibilitymap.jobs.data.analyser.emission.dto.FuelType.DIESEL)
                        .vehicleType(VehicleType.CAR)
                        .build())
                .build();
        emissionZoneMapper = new EmissionZoneMapper(emissionService, maximumWeightMapper, emissionClassMapper, fuelTypeMapper,
                emissionZoneTypeMapper, transportTypeMapper);
    }

    @Test
    void map() {

        when(emissionService.findById(EMISSION_ZONE_ID)).thenReturn(Optional.of(emissionZoneDto));

        when(maximumWeightMapper.map(emissionZoneDto.exemptions().getFirst().vehicleCategories(), 2D)).thenReturn(
                Maximum.builder().value(2D).build());
        when(maximumWeightMapper.map(emissionZoneDto.restriction().vehicleCategories())).thenReturn(Maximum.builder().value(3D).build());
        when(emissionClassMapper.map(emissionZoneDto.exemptions().getFirst().euroClassifications())).thenReturn(
                Set.of(EmissionClass.EURO_1));
        when(fuelTypeMapper.map(emissionZoneDto.restriction().fuelType())).thenReturn(Set.of(FuelType.DIESEL));
        when(transportTypeMapper.map(emissionZoneDto.restriction().vehicleType(),
                emissionZoneDto.restriction().vehicleCategories())).thenReturn(Set.of(TransportType.BUS));
        when(transportTypeMapper.map(emissionZoneDto.exemptions().getFirst().vehicleCategories())).thenReturn(Set.of(TransportType.CAR));
        when(emissionZoneTypeMapper.map(emissionZoneDto.type())).thenReturn(
                nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType.LOW);

        EmissionZone emissionZone = emissionZoneMapper.map(EMISSION_ZONE_ID);

        validate(emissionZone);
    }

    @Test
    void map_endTime_null() {

        emissionZoneDto = emissionZoneDto.withEndTime(null);

        map();
    }

    @Test
    void map_exemption_startTime_null() {

        emissionZoneDto = emissionZoneDto.withExemptions(List.of(emissionZoneDto.exemptions().getFirst().withStartTime(null)));

        map();
    }

    @Test
    void map_exemption_endTime_null() {

        emissionZoneDto = emissionZoneDto.withExemptions(List.of(emissionZoneDto.exemptions().getFirst().withEndTime(null)));

        map();
    }

    @Test
    void map_unexpectedErrorOccurred() {

        when(emissionService.findById(EMISSION_ZONE_ID)).thenReturn(Optional.of(emissionZoneDto));

        when(maximumWeightMapper.map(any(), any())).thenThrow(new RuntimeException("Unexpected error occurred"));
        assertThat(emissionZoneMapper.map(EMISSION_ZONE_ID)).isNull();

        loggerExtension.containsLog(
                Level.ERROR,
                "Emission zone with id '%s' is incomplete and will be skipped.".formatted(EMISSION_ZONE_ID),
                "Unexpected error occurred");
    }

    @Test
    void map_exemptions_null() {

        emissionZoneDto = emissionZoneDto.withExemptions(null);

        when(emissionService.findById(EMISSION_ZONE_ID)).thenReturn(Optional.of(emissionZoneDto));

        when(maximumWeightMapper.map(emissionZoneDto.restriction().vehicleCategories())).thenReturn(Maximum.builder().value(3D).build());
        when(fuelTypeMapper.map(emissionZoneDto.restriction().fuelType())).thenReturn(Set.of(FuelType.DIESEL));
        when(transportTypeMapper.map(emissionZoneDto.restriction().vehicleType(),
                emissionZoneDto.restriction().vehicleCategories())).thenReturn(Set.of(TransportType.BUS));

        EmissionZone emissionZone = emissionZoneMapper.map(EMISSION_ZONE_ID);

        assertThat(emissionZone.startTime()).isEqualTo(emissionZoneDto.startTime());
        assertThat(emissionZone.endTime()).isEqualTo(emissionZoneDto.endTime());
        assertThat(emissionZone.exemptions()).isEmpty();
        assertThat(emissionZone.restriction()).isEqualTo(
                EmissionZoneRestriction.builder()
                        .id("restrictionId")
                        .transportTypes(Set.of(TransportType.BUS))
                        .vehicleWeightInKg(Maximum.builder().value(3D).build())
                        .fuelTypes(Set.of(FuelType.DIESEL))
                        .build()
        );
    }

    @Test
    void map_exemptions_vehicleWeightInKg_null() {

        emissionZoneDto = emissionZoneDto.withExemptions(List.of(emissionZoneDto.exemptions().getFirst().withVehicleWeightInKg(null)));

        when(emissionService.findById(EMISSION_ZONE_ID)).thenReturn(Optional.of(emissionZoneDto));

        when(maximumWeightMapper.map(emissionZoneDto.exemptions().getFirst().vehicleCategories(), null)).thenReturn(
                Maximum.builder().value(2D).build());
        when(maximumWeightMapper.map(emissionZoneDto.restriction().vehicleCategories())).thenReturn(Maximum.builder().value(3D).build());
        when(emissionClassMapper.map(emissionZoneDto.exemptions().getFirst().euroClassifications())).thenReturn(
                Set.of(EmissionClass.EURO_1));
        when(fuelTypeMapper.map(emissionZoneDto.restriction().fuelType())).thenReturn(Set.of(FuelType.DIESEL));
        when(transportTypeMapper.map(emissionZoneDto.restriction().vehicleType(),
                emissionZoneDto.restriction().vehicleCategories())).thenReturn(Set.of(TransportType.BUS));
        when(transportTypeMapper.map(emissionZoneDto.exemptions().getFirst().vehicleCategories())).thenReturn(Set.of(TransportType.CAR));
        when(emissionZoneTypeMapper.map(emissionZoneDto.type())).thenReturn(
                nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType.LOW);

        EmissionZone emissionZone = emissionZoneMapper.map(EMISSION_ZONE_ID);

        validate(emissionZone);
    }

    private void validate(EmissionZone emissionZone) {

        assertThat(emissionZone.id()).isEqualTo(EMISSION_ZONE_ID);
        assertThat(emissionZone.type()).isEqualTo(nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType.LOW);
        assertThat(emissionZone.startTime()).isEqualTo(emissionZoneDto.startTime());
        assertThat(Objects.nonNull(emissionZoneDto.endTime())
                ? OffsetDateTime.parse("2022-03-11T10:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : OffsetDateTime.MAX).isEqualTo(emissionZone.endTime());
        assertThat(emissionZone.exemptions()).containsExactlyElementsOf(Set.of(
                EmissionZoneExemption.builder()
                        .startTime(
                                Objects.nonNull(emissionZoneDto.exemptions().getFirst().startTime())
                                        ? OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                        : OffsetDateTime.MIN)
                        .endTime(
                                Objects.nonNull(emissionZoneDto.exemptions().getFirst().endTime())
                                        ? OffsetDateTime.parse("2022-03-11T10:00:00.000-01:00", DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                        : OffsetDateTime.MAX)
                        .emissionClasses(Set.of(EmissionClass.EURO_1))
                        .transportTypes(Set.of(TransportType.CAR))
                        .vehicleWeightInKg(Maximum.builder().value(2D).build())
                        .build()));
        assertThat(emissionZone.restriction()).isEqualTo(
                EmissionZoneRestriction.builder()
                        .id("restrictionId")
                        .transportTypes(Set.of(TransportType.BUS))
                        .vehicleWeightInKg(Maximum.builder().value(3D).build())
                        .fuelTypes(Set.of(FuelType.DIESEL))
                        .build()
        );
    }
}