package nu.ndw.nls.accessibilitymap.accessibility.service.dto;

import com.graphhopper.util.shapes.BBox;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import lombok.Builder;
import lombok.With;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.springframework.validation.annotation.Validated;

@Builder
@With
@Validated
public record AccessibilityRequest(
        @NotNull OffsetDateTime timestamp,
        BBox boundingBox,
        Integer municipalityId,
        boolean addMissingRoadsSectionsFromNwb,
        @NotNull Double searchRadiusInMeters,
        @NotNull Double startLocationLatitude,
        @NotNull Double startLocationLongitude,
        Double endLocationLatitude,
        Double endLocationLongitude,
        Double vehicleLengthInCm,
        Double vehicleHeightInCm,
        Double vehicleWidthInCm,
        Double vehicleWeightInKg,
        Double vehicleAxleLoadInKg,
        Set<FuelType> fuelTypes,
        Set<EmissionClass> emissionClasses,
        Set<TransportType> transportTypes,
        Set<TrafficSignType> trafficSignTypes,
        Set<TextSignType> trafficSignTextSignTypes,
        Set<TextSignType> excludeTrafficSignTextSignTypes,
        Set<ZoneCodeType> excludeTrafficSignZoneCodeTypes,
        Set<String> excludeRestrictionsWithEmissionZoneIds,
        Set<EmissionZoneType> excludeRestrictionsWithEmissionZoneTypes) {

    public Set<TextSignType> excludeTrafficSignTextSignTypes() {

        if (Objects.nonNull(excludeTrafficSignTextSignTypes)) {
            return excludeTrafficSignTextSignTypes;
        } else {
            return Set.of(TextSignType.EXCLUDING, TextSignType.PRE_ANNOUNCEMENT);
        }
    }

    public Set<ZoneCodeType> excludeTrafficSignZoneCodeTypes() {

        if (Objects.nonNull(excludeTrafficSignZoneCodeTypes)) {
            return excludeTrafficSignZoneCodeTypes;
        } else {
            return Set.of(ZoneCodeType.END);
        }
    }

    public boolean hasEndLocation() {
        return Objects.nonNull(endLocationLatitude)
                && Objects.nonNull(endLocationLongitude);
    }
}
