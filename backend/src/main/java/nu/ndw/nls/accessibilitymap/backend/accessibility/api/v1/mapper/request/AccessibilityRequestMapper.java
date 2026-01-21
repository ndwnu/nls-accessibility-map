package nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.request;

import com.graphhopper.util.shapes.BBox;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto.Excludes;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.FuelTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.api.v1.mapper.TransportTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.springboot.core.time.ClockService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityRequestMapper {

    private static final int MULTIPLIER_FROM_METERS_TO_CM = 100;

    private static final int MULTIPLIER_FROM_TONNE_TO_KILO_GRAM = 1000;

    private final TransportTypeMapper transportTypeMapper;

    private final EmissionClassMapper emissionClassMapper;

    private final FuelTypeMapper fuelTypeMapper;

    private final EmissionZoneTypeMapper emissionZoneTypeMapper;

    private final ClockService clockService;

    private final MunicipalityService municipalityService;

    @Valid
    public AccessibilityRequest map(
            Municipality municipality,
            VehicleArguments vehicleArguments,
            Excludes excludes,
            Double endPointLatitude,
            Double endPointLongitude) {

        return AccessibilityRequest.builder()
                .timestamp(clockService.now())
                .municipalityId(municipality.idAsInteger())
                .addMissingRoadsSectionsFromNwb(true)
                .boundingBox(BBox.fromPoints(
                        municipality.bounds().latitudeFrom(),
                        municipality.bounds().longitudeFrom(),
                        municipality.bounds().latitudeTo(),
                        municipality.bounds().longitudeTo()
                ))
                .searchRadiusInMeters(Double.valueOf(municipality.searchDistanceInMetres()))
                .startLocationLatitude(municipality.startCoordinateLatitude())
                .startLocationLongitude(municipality.startCoordinateLongitude())
                .endLocationLatitude(endPointLatitude)
                .endLocationLongitude(endPointLongitude)
                .vehicleHeightInCm(mapToDouble(vehicleArguments.vehicleHeight(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleLengthInCm(mapToDouble(vehicleArguments.vehicleLength(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleWidthInCm(mapToDouble(vehicleArguments.vehicleWidth(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleWeightInKg(mapToDouble(vehicleArguments.vehicleWeight(), MULTIPLIER_FROM_TONNE_TO_KILO_GRAM))
                .vehicleAxleLoadInKg(mapToDouble(vehicleArguments.vehicleAxleLoad(), MULTIPLIER_FROM_TONNE_TO_KILO_GRAM))
                .fuelTypes(
                        Objects.isNull(vehicleArguments.fuelTypes())
                                ? null
                                : vehicleArguments.fuelTypes().stream()
                                        .map(fuelTypeMapper::map)
                                        .collect(Collectors.toSet()))
                .emissionClasses(emissionClassMapper.map(vehicleArguments.emissionClass()))
                .transportTypes(transportTypeMapper.map(vehicleArguments))
                .excludeRestrictionsWithEmissionZoneIds(mapEmissionZoneIds(excludes))
                .excludeRestrictionsWithEmissionZoneTypes(mapEmissionZoneTypes(excludes))
                .build();
    }

    private static Set<String> mapEmissionZoneIds(Excludes excludes) {

        return Objects.isNull(excludes)
                ? null
                : excludes.emissionZoneIds();
    }

    private Set<EmissionZoneType> mapEmissionZoneTypes(Excludes excludes) {

        return Objects.isNull(excludes) || Objects.isNull(excludes.emissionZoneTypes())
                ? null
                : excludes.emissionZoneTypes().stream()
                        .map(emissionZoneTypeMapper::map)
                        .collect(Collectors.toSet());
    }

    private static Double mapToDouble(Float value, int multiplier) {

        return Objects.nonNull(value) ? (Double.valueOf(value) * multiplier) : null;
    }
}
