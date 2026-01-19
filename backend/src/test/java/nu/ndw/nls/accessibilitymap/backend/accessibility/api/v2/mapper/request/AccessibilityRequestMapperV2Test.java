package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.when;

import com.graphhopper.util.shapes.BBox;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.FuelTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v2.mapper.TransportTypeMapperV2;
import nu.ndw.nls.accessibilitymap.backend.exception.ResourceNotFoundException;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.generated.model.v2.AccessibilityRequestJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.DestinationRequestJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.EmissionClassJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.EmissionZoneTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.ExclusionsJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.FuelTypeJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.MunicipalityAreaRequestJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleCharacteristicsJson;
import nu.ndw.nls.accessibilitymap.generated.model.v2.VehicleTypeJson;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestMapperV2Test {

    private AccessibilityRequestMapperV2 accessibilityRequestMapperV2;

    @Mock
    private TransportTypeMapperV2 transportTypeMapperV2;

    @Mock
    private EmissionClassMapperV2 emissionClassMapperV2;

    @Mock
    private FuelTypeMapperV2 fuelTypeMapperV2;

    @Mock
    private EmissionZoneTypeMapperV2 emissionZoneTypeMapperV2;

    @Mock
    private ClockService clockService;

    @Mock
    private MunicipalityService municipalityService;

    @Mock
    private OffsetDateTime timestamp;

    private Municipality municipality;

    private AccessibilityRequestJson accessibilityRequestJson;

    private MunicipalityAreaRequestJson municipalityAreaRequestJson;

    @BeforeEach
    void setUp() {

        municipalityAreaRequestJson = MunicipalityAreaRequestJson.builder()
                .id("GM0001")
                .build();
                
        accessibilityRequestJson = AccessibilityRequestJson.builder()
                .vehicle(VehicleCharacteristicsJson.builder()
                        .axleLoad(1F)
                        .hasTrailer(true)
                        .emissionClass(EmissionClassJson.EURO_1)
                        .weight(2F)
                        .height(3F)
                        .length(4F)
                        .width(5F)
                        .type(VehicleTypeJson.CAR)
                        .fuelTypes(List.of(FuelTypeJson.PETROL))
                        .build())
                .exclusions(ExclusionsJson.builder()
                        .emissionZoneIds(List.of("id1"))
                        .emissionZoneTypes(List.of(EmissionZoneTypeJson.LOW_EMISSION_ZONE))
                        .build())
                .destination(DestinationRequestJson.builder()
                        .latitude(10D)
                        .longitude(11D)
                        .build())
                .area(municipalityAreaRequestJson)
                .build();

        municipality = Municipality.builder()
                .id("GM0001")
                .searchDistanceInMetres(2)
                .startCoordinateLatitude(3D)
                .startCoordinateLongitude(4D)
                .bounds(MunicipalityBoundingBox.builder()
                        .latitudeTo(5D)
                        .longitudeTo(6D)
                        .latitudeFrom(7D)
                        .longitudeFrom(8D)
                        .build())
                .build();

        accessibilityRequestMapperV2 = new AccessibilityRequestMapperV2(
                transportTypeMapperV2,
                emissionClassMapperV2,
                fuelTypeMapperV2,
                emissionZoneTypeMapperV2,
                clockService,
                municipalityService);
    }

    @Test
    void map() {

        when(clockService.now()).thenReturn(timestamp);

        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(municipality);

        when(transportTypeMapperV2.map(accessibilityRequestJson.getVehicle())).thenReturn(Set.of(TransportType.CAR));
        when(emissionClassMapperV2.map(EmissionClassJson.EURO_1)).thenReturn(Set.of(EmissionClass.EURO_1));
        when(fuelTypeMapperV2.map(FuelTypeJson.PETROL)).thenReturn(FuelType.PETROL);
        when(emissionZoneTypeMapperV2.map(EmissionZoneTypeJson.LOW_EMISSION_ZONE)).thenReturn(EmissionZoneType.LOW);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapperV2.map(accessibilityRequestJson);

        assertThat(accessibilityRequest).isEqualTo(AccessibilityRequest.builder()
                .timestamp(timestamp)
                .addMissingRoadsSectionsFromNwb(true)
                .boundingBox(BBox.fromPoints(
                        municipality.bounds().latitudeFrom(),
                        municipality.bounds().longitudeFrom(),
                        municipality.bounds().latitudeTo(),
                        municipality.bounds().longitudeTo()))
                .transportTypes(Set.of(TransportType.CAR))
                .vehicleHeightInCm(mapToDouble(accessibilityRequestJson.getVehicle().getHeight(), 100))
                .vehicleLengthInCm(mapToDouble(accessibilityRequestJson.getVehicle().getLength(), 100))
                .vehicleWidthInCm(mapToDouble(accessibilityRequestJson.getVehicle().getWidth(), 100))
                .vehicleWeightInKg(mapToDouble(accessibilityRequestJson.getVehicle().getWeight(), 1_000))
                .vehicleAxleLoadInKg(mapToDouble(accessibilityRequestJson.getVehicle().getAxleLoad(), 1_000))
                .startLocationLatitude(municipality.startCoordinateLatitude())
                .startLocationLongitude(municipality.startCoordinateLongitude())
                .endLocationLatitude(accessibilityRequestJson.getDestination().getLatitude())
                .endLocationLongitude(accessibilityRequestJson.getDestination().getLongitude())
                .municipalityId(municipality.idAsInteger())
                .emissionClasses(Set.of(EmissionClass.EURO_1))
                .fuelTypes(Set.of(FuelType.PETROL))
                .searchRadiusInMeters(Double.valueOf(municipality.searchDistanceInMetres()))
                .excludeRestrictionsWithEmissionZoneIds(Set.of("id1"))
                .excludeRestrictionsWithEmissionZoneTypes(Set.of(EmissionZoneType.LOW))
                .build());
    }

    @Test
    void map_noExclusions() {

        accessibilityRequestJson.setExclusions(null);

        when(clockService.now()).thenReturn(timestamp);
        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(municipality);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapperV2.map(accessibilityRequestJson);

        assertThat(accessibilityRequest.excludeRestrictionsWithEmissionZoneIds()).isNull();
        assertThat(accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes()).isNull();
    }

    @Test
    void map_noEmissionZoneTypes() {

        assertThat(accessibilityRequestJson.getExclusions()).isNotNull();
        accessibilityRequestJson.getExclusions().setEmissionZoneTypes(null);

        when(clockService.now()).thenReturn(timestamp);
        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(municipality);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapperV2.map(accessibilityRequestJson);

        assertThat(accessibilityRequest.excludeRestrictionsWithEmissionZoneTypes()).isNull();
    }

    @Test
    void map_noEmissionZoneIds() {

        assertThat(accessibilityRequestJson.getExclusions()).isNotNull();
        accessibilityRequestJson.getExclusions().setEmissionZoneIds(null);

        when(clockService.now()).thenReturn(timestamp);
        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(municipality);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapperV2.map(accessibilityRequestJson);

        assertThat(accessibilityRequest.excludeRestrictionsWithEmissionZoneIds()).isNull();
    }

    @Test
    void map_municipalityNotFound() {

        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(null);

        assertThat(catchThrowable(() -> accessibilityRequestMapperV2.map(accessibilityRequestJson)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Municipality with id 'GM0001' not found.");
    }

    @Test
    void map_noDoubleValueProvided() {

        accessibilityRequestJson.getVehicle().setHeight(null);

        when(clockService.now()).thenReturn(timestamp);
        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(municipality);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapperV2.map(accessibilityRequestJson);

        assertThat(accessibilityRequest.vehicleHeightInCm()).isNull();
    }

    @Test
    void map_noDestination() {

        accessibilityRequestJson.setDestination(null);

        when(clockService.now()).thenReturn(timestamp);
        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(municipality);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapperV2.map(accessibilityRequestJson);

        assertThat(accessibilityRequest.endLocationLatitude()).isNull();
        assertThat(accessibilityRequest.endLocationLongitude()).isNull();
    }

    @Test
    void map_noFuelTypes() {

        accessibilityRequestJson.getVehicle().setFuelTypes(null);

        when(clockService.now()).thenReturn(timestamp);
        when(municipalityService.getMunicipalityById(municipalityAreaRequestJson.getId())).thenReturn(municipality);

        AccessibilityRequest accessibilityRequest = accessibilityRequestMapperV2.map(accessibilityRequestJson);

        assertThat(accessibilityRequest.fuelTypes()).isNull();
    }

    private static Double mapToDouble(Float value, int multiplier) {

        return value != null ? (Double.valueOf(value) * multiplier) : null;
    }
}
