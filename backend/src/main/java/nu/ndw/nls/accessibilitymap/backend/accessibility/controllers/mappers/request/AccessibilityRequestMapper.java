package nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.request;

import com.graphhopper.util.shapes.BBox;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.services.dto.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.municipality.controllers.dto.Municipality;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityRequestMapper {

    private final TransportTypeMapper transportTypeMapper;

    private final EmissionClassificationMapper emissionClassificationMapper;

    private final FuelTypeMapper fuelTypeMapper;

    @Valid
    public AccessibilityRequest mapToAccessibilityRequest(
            OffsetDateTime timestamp,
            Municipality municipality,
            VehicleArguments vehicleArguments) {

        return AccessibilityRequest.builder()
                .timestamp(timestamp)
                .municipalityId(municipality.getMunicipalityIdInteger())
                .boundingBox(BBox.fromPoints(
                        municipality.getBounds().latitudeFrom(),
                        municipality.getBounds().longitudeFrom(),
                        municipality.getBounds().latitudeTo(),
                        municipality.getBounds().longitudeTo()
                ))
                .searchRadiusInMeters(municipality.getSearchDistanceInMetres())
                .startLocationLatitude(municipality.getStartPoint().getY())
                .startLocationLongitude(municipality.getStartPoint().getX())
                .vehicleAxleLoadInKg(mapToDouble(vehicleArguments.vehicleAxleLoad()))
                .vehicleHeightInCm(mapToDouble(vehicleArguments.vehicleHeight()))
                .vehicleLengthInCm(mapToDouble(vehicleArguments.vehicleLength()))
                .vehicleWidthInCm(mapToDouble(vehicleArguments.vehicleWidth()))
                .vehicleWeightInKg(mapToDouble(vehicleArguments.vehicleWeight()))
                .fuelTypes(fuelTypeMapper.mapFuelType(vehicleArguments.fuelType()))
                .emissionClasses(emissionClassificationMapper.mapEmissionClass(vehicleArguments.emissionClass()))
                .transportTypes(transportTypeMapper.mapToTransportType(vehicleArguments))
                .build();
    }

    private static Double mapToDouble(Float value) {
        return value != null ? Double.valueOf(value) : null;
    }
}
