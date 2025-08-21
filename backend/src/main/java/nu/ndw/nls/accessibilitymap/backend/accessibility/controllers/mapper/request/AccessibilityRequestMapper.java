package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.request;

import com.graphhopper.util.shapes.BBox;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.Exemptions;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.EmissionZoneTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.FuelTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mapper.TransportTypeMapper;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
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

    @Valid
    public AccessibilityRequest mapToAccessibilityRequest(
            OffsetDateTime timestamp,
            Municipality municipality,
            VehicleArguments vehicleArguments,
            @Valid Exemptions exemptions,
            Double endPointLatitude,
            Double endPointLongitude) {

        return AccessibilityRequest.builder()
                .timestamp(timestamp)
                .municipalityId(municipality.municipalityIdAsInteger())
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
                                        .map(fuelTypeMapper::mapFuelType)
                                        .collect(Collectors.toSet()))
                .emissionClasses(emissionClassMapper.mapEmissionClass(vehicleArguments.emissionClass()))
                .transportTypes(transportTypeMapper.mapToTransportType(vehicleArguments))
                .excludeEmissionZoneIds(mapEmissionZoneIds(exemptions))
                .excludeEmissionZoneTypes(mapEmissionZoneTypes(exemptions))
                .build();
    }

    private static Set<String> mapEmissionZoneIds(Exemptions exemptions) {

        return Objects.isNull(exemptions) || Objects.isNull(exemptions.emissionZone().ids())
                ? null
                : exemptions.emissionZone().ids();
    }

    private Set<EmissionZoneType> mapEmissionZoneTypes(Exemptions exemptions) {

        return Objects.isNull(exemptions) || Objects.isNull(exemptions.emissionZone().types())
                ? null
                : exemptions.emissionZone().types().stream()
                        .map(emissionZoneTypeMapper::mapEmissionZoneType)
                        .collect(Collectors.toSet());
    }

    private static Double mapToDouble(Float value, int multiplier) {

        return value != null ? (Double.valueOf(value) * multiplier) : null;
    }
}
