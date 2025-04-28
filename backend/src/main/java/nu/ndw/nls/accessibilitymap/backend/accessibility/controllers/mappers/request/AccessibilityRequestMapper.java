package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import com.graphhopper.util.shapes.BBox;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityRequestMapper {

    private static final int MULTIPLIER_FROM_CM_TO_METERS = 100;

    private static final int MULTIPLIER_FROM_TON_TO_KILO_GRAM = 1000;

    private final TransportTypeMapper transportTypeMapper;

    private final EmissionClassMapper emissionClassMapper;

    private final FuelTypeMapper fuelTypeMapper;


    @Valid
    public AccessibilityRequest mapToAccessibilityRequest(
            OffsetDateTime timestamp,
            Municipality municipality,
            VehicleArguments vehicleArguments) {

        return AccessibilityRequest.builder()
                .timestamp(timestamp)
                .municipalityId(municipality.municipalityIdAsInteger())
                .boundingBox(BBox.fromPoints(
                        municipality.bounds().latitudeFrom(),
                        municipality.bounds().longitudeFrom(),
                        municipality.bounds().latitudeTo(),
                        municipality.bounds().longitudeTo()
                ))
                .searchRadiusInMeters(Double.valueOf(municipality.searchDistanceInMetres()))
                .startLocationLatitude(municipality.startCoordinateLatitude())
                .startLocationLongitude(municipality.startCoordinateLongitude())
                .vehicleHeightInCm(mapToDouble(vehicleArguments.vehicleHeight(), MULTIPLIER_FROM_CM_TO_METERS))
                .vehicleLengthInCm(mapToDouble(vehicleArguments.vehicleLength(), MULTIPLIER_FROM_CM_TO_METERS))
                .vehicleWidthInCm(mapToDouble(vehicleArguments.vehicleWidth(), MULTIPLIER_FROM_CM_TO_METERS))
                .vehicleWeightInKg(mapToDouble(vehicleArguments.vehicleWeight(), MULTIPLIER_FROM_TON_TO_KILO_GRAM))
                .vehicleAxleLoadInKg(mapToDouble(vehicleArguments.vehicleAxleLoad(), MULTIPLIER_FROM_TON_TO_KILO_GRAM))
                .fuelTypes(fuelTypeMapper.mapFuelType(vehicleArguments.fuelType()))
                .emissionClasses(emissionClassMapper.mapEmissionClass(vehicleArguments.emissionClass()))
                .transportTypes(transportTypeMapper.mapToTransportType(vehicleArguments))
                .build();
    }

    private static Double mapToDouble(Float value, int multiplier) {
        return value != null ? (Double.valueOf(value) * multiplier) : null;
    }
}
