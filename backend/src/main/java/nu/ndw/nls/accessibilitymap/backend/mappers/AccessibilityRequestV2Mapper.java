package nu.ndw.nls.accessibilitymap.backend.mappers;

import com.graphhopper.util.shapes.BBox;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityRequestV2Mapper {

    private final TransportTypeV2Mapper transportTypeV2Mapper;

    public AccessibilityRequest mapToAccessibilityRequest(Municipality municipality, VehicleArguments vehicleArguments) {

        return AccessibilityRequest
                .builder()
                .municipalityId(municipality.getMunicipalityIdInteger())
                .boundingBox(BBox.fromPoints(
                        municipality.getBounds().latitudeFrom(),
                        municipality.getBounds().latitudeFrom(),
                        municipality.getBounds().latitudeTo(),
                        municipality.getBounds().longitudeTo()
                ))
                .searchRadiusInMeters(municipality.getSearchDistanceInMetres())
                .startLocationLatitude(municipality.getStartPoint().getY())
                .startLocationLongitude(municipality.getStartPoint().getX())
                .vehicleAxleLoadInKg(
                        vehicleArguments.vehicleAxleLoad() != null ? Double.valueOf(vehicleArguments.vehicleAxleLoad()) : null)
                .vehicleHeightInCm(
                        vehicleArguments.vehicleHeight() != null ? Double.valueOf(vehicleArguments.vehicleHeight()) : null)
                .vehicleLengthInCm(
                        vehicleArguments.vehicleLength() != null ? Double.valueOf(vehicleArguments.vehicleLength()) : null)
                .vehicleWidthInCm(vehicleArguments.vehicleWidth() != null ? Double.valueOf(vehicleArguments.vehicleWidth()) : null)
                .vehicleWeightInKg(
                        vehicleArguments.vehicleWeight() != null ? Double.valueOf(vehicleArguments.vehicleWeight()) : null)
                .transportTypes(transportTypeV2Mapper.mapToTransportType(vehicleArguments))
                .build();
    }
}
