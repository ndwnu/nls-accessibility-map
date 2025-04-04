package nu.ndw.nls.accessibilitymap.backend.mappers;

import com.graphhopper.util.shapes.BBox;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.backend.controllers.dto.VehicleArguments;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import org.springframework.stereotype.Component;

/**
 * The AccessibilityRequestV2Mapper class is responsible for mapping a given Municipality
 * and VehicleArguments to an instance of AccessibilityRequest. This class acts as a
 * transformation utility that constructs a request object containing accessibility
 * parameters derived from the input data.
 *<p></p>
 * Dependencies:
 * - TransportTypeV2Mapper: Used to convert vehicle information into transport types.
 *<p></p>
 * Responsibilities:
 * - Convert the input Municipality details into corresponding fields in AccessibilityRequest.
 * - Transform vehicle-related dimensions and attributes from VehicleArguments to the AccessibilityRequest object.
 * - Compute and populate bounding box, search radius, and vehicle-attribute-related fields.
 * - Handle null safety by converting nullable Float values to Double where necessary.
 */
@Component
@RequiredArgsConstructor
public class AccessibilityRequestV2Mapper {

    private final TransportTypeV2Mapper transportTypeV2Mapper;

    /**
     * Maps a Municipality and VehicleArguments object to an AccessibilityRequest instance.
     * This method transforms input data into an AccessibilityRequest, preparing it for
     * further use in processes requiring accessibility information.
     *<p></p>
     * @param municipality the Municipality object containing geographical and search metadata
     * @param vehicleArguments the VehicleArguments object containing vehicle specifications and attributes
     * @return an AccessibilityRequest object built using the provided Municipality and VehicleArguments data
     */
    public AccessibilityRequest mapToAccessibilityRequest(Municipality municipality, VehicleArguments vehicleArguments) {

        return AccessibilityRequest
                .builder()
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
                .transportTypes(transportTypeV2Mapper.mapToTransportType(vehicleArguments))
                .build();
    }

    private static Double mapToDouble(Float value) {
        return value != null ? Double.valueOf(value) : null;
    }
}
