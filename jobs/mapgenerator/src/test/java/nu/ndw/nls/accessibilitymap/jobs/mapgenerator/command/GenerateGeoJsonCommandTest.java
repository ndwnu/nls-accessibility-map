package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import nu.ndw.nls.accessibilitymap.accessibility.AccessibilityConfiguration;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.configuration.GenerateConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.model.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.time.ClockService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.services.MapGeneratorService;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util.LoggerExtension;
import nu.ndw.nls.accessibilitymap.shared.network.dtos.AccessibilityGraphhopperMetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class GenerateGeoJsonCommandTest {


    private GenerateGeoJsonCommand generateGeoJsonCommand;


    @Mock
    private MapGeneratorService mapGeneratorService;


    @Mock
    private AccessibilityConfiguration accessibilityConfiguration;

    @Mock
    private GenerateConfiguration generateConfiguration;

    @Mock
    private ClockService clockService;

    @Mock
    private AccessibilityGraphhopperMetaData accessibilityGraphhopperMetaData;

    @BeforeEach
    void setUp() {

        generateGeoJsonCommand = new GenerateGeoJsonCommand(mapGeneratorService, accessibilityConfiguration,
                generateConfiguration, clockService);
    }

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @ParameterizedTest
    @EnumSource(value = TrafficSignType.class)
    void call_ok(TrafficSignType trafficSignType) {

        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(accessibilityConfiguration.accessibilityGraphhopperMetaData())
                .thenReturn(accessibilityGraphhopperMetaData);
        when(accessibilityGraphhopperMetaData.nwbVersion())
                .thenReturn(123);
        when(clockService.now())
                .thenReturn(startTime);

        assertThat(new CommandLine(generateGeoJsonCommand)
                .execute(
                        "--traffic-sign=%s".formatted(trafficSignType.name()),
                        "--include-only-time-windowed-signs",
                        "--publish-events")
        ).isZero();

        ArgumentCaptor<GeoGenerationProperties> geoGenerationPropertiesCaptor = ArgumentCaptor.forClass(
                GeoGenerationProperties.class);
        verify(mapGeneratorService).generate(geoGenerationPropertiesCaptor.capture());

        GeoGenerationProperties geoGenerationProperties = geoGenerationPropertiesCaptor.getValue();

        validateGeoGenerationPropertiesValid(
                trafficSignType,
                geoGenerationProperties,
                startTime,
                true,
                true);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            true, true,
            true, false,
            false, true,
            false, false
            """)
    void call_ok_booleanArguments(boolean includeOnlyTimeWindowedSigns, boolean publishEvents) {

        TrafficSignType trafficSignType = TrafficSignType.C6;
        OffsetDateTime startTime = OffsetDateTime.parse("2022-03-11T09:00:00.000-01:00");

        when(accessibilityConfiguration.accessibilityGraphhopperMetaData())
                .thenReturn(accessibilityGraphhopperMetaData);
        when(accessibilityGraphhopperMetaData.nwbVersion())
                .thenReturn(123);
        when(clockService.now())
                .thenReturn(startTime);

        ArrayList<String> arguments = new ArrayList<>();
        arguments.add("--traffic-sign=%s".formatted(trafficSignType.name()));
        if (includeOnlyTimeWindowedSigns) {
            arguments.add("--include-only-time-windowed-signs");
        }
        if (publishEvents) {
            arguments.add("--publish-events");
        }

        assertThat(new CommandLine(generateGeoJsonCommand).execute(arguments.toArray(String[]::new))
        ).isZero();

        ArgumentCaptor<GeoGenerationProperties> geoGenerationPropertiesCaptor = ArgumentCaptor.forClass(
                GeoGenerationProperties.class);
        verify(mapGeneratorService).generate(geoGenerationPropertiesCaptor.capture());

        GeoGenerationProperties geoGenerationProperties = geoGenerationPropertiesCaptor.getValue();

        validateGeoGenerationPropertiesValid(
                trafficSignType,
                geoGenerationProperties,
                startTime,
                includeOnlyTimeWindowedSigns,
                publishEvents);
    }

    @Test
    void call_errorOccurred() {

        when(clockService.now()).thenThrow(new RuntimeException("MyException"));

        assertThat(new CommandLine(generateGeoJsonCommand)
                .execute(
                        "--traffic-sign=%s".formatted(TrafficSignType.C6.name()),
                        "--include-only-time-windowed-signs",
                        "--publish-events")
        ).isOne();

        loggerExtension.containsLog(
                Level.ERROR,
                "Could not generate GeoJson because of: ",
                "MyException"
        );
    }


    private void validateGeoGenerationPropertiesValid(
            TrafficSignType trafficSignType,
            GeoGenerationProperties geoGenerationProperties,
            OffsetDateTime startTime,
            boolean includeOnlyTimeWindowedSigns,
            boolean publishEvents) {

        assertThat(geoGenerationProperties.startTime()).isEqualTo(startTime);
        assertThat(geoGenerationProperties.trafficSignType()).isEqualTo(trafficSignType);
        assertThat(geoGenerationProperties.includeOnlyTimeWindowedSigns()).isEqualTo(includeOnlyTimeWindowedSigns);
        assertThat(geoGenerationProperties.exportVersion()).isEqualTo(20220311);
        assertThat(geoGenerationProperties.publishEvents()).isEqualTo(publishEvents);
        assertThat(geoGenerationProperties.generateConfiguration()).isEqualTo(generateConfiguration);

        validateVehicleProperties(geoGenerationProperties.vehicleProperties(), trafficSignType);

        loggerExtension.containsLog(
                Level.INFO,
                "Generating GeoJson"
        );
    }

    private void validateVehicleProperties(VehicleProperties vehicleProperties, TrafficSignType trafficSignType) {

        switch (trafficSignType) {
            case C6 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C7 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C7B -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C12 -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isTrue();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isFalse();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
            case C22C -> {
                assertThat(vehicleProperties.carAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.hgvAndBusAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.motorVehicleAccessForbiddenWt()).isFalse();
                assertThat(vehicleProperties.lcvAndHgvAccessForbiddenWt()).isTrue();
                validateVehiclePropertiesDefaultValues(vehicleProperties);
            }
        }
    }

    private void validateVehiclePropertiesDefaultValues(VehicleProperties vehicleProperties) {

        assertThat(vehicleProperties.hgvAccessForbidden()).isFalse();
        assertThat(vehicleProperties.busAccessForbidden()).isFalse();
        assertThat(vehicleProperties.hgvAndBusAccessForbidden()).isFalse();
        assertThat(vehicleProperties.tractorAccessForbidden()).isFalse();
        assertThat(vehicleProperties.slowVehicleAccessForbidden()).isFalse();
        assertThat(vehicleProperties.trailerAccessForbidden()).isFalse();
        assertThat(vehicleProperties.motorcycleAccessForbidden()).isFalse();
        assertThat(vehicleProperties.motorVehicleAccessForbidden()).isFalse();
        assertThat(vehicleProperties.lcvAndHgvAccessForbidden()).isFalse();
    }
}