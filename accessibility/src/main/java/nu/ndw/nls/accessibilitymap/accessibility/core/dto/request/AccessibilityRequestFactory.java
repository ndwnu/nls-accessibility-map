package nu.ndw.nls.accessibilitymap.accessibility.core.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers.TrafficSignRestrictionsBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessibilityRequestFactory {

    private final TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;

    public AccessibilityRequest create(
            List<TrafficSignType> trafficSignTypes,
            @NotNull Double startLocationLatitude,
            @NotNull Double startLocationLongitude,
            double searchRadiusInMeters) {

        List<Restrictions> restrictionsList = trafficSignTypes.stream()
                .map(trafficSignType -> {
                    TrafficSign trafficSign = TrafficSign.builder()
                            .trafficSignType(trafficSignType)
                            .blackCode(0D) //The highest restriction possible for dynamic fields
                            .build();
                    return trafficSignRestrictionsBuilder.buildFor(trafficSign);
                })
                .toList();

        return AccessibilityRequest.builder()
                .vehicleLengthInCm(getValue(restrictionsList, Restrictions::vehicleLengthInCm))
                .vehicleWidthInCm(getValue(restrictionsList, Restrictions::vehicleWidthInCm))
                .vehicleHeightInCm(getValue(restrictionsList, Restrictions::vehicleHeightInCm))
                .vehicleWeightInKg(getValue(restrictionsList, Restrictions::vehicleWeightInKg))
                .vehicleAxleLoadInKg(getValue(restrictionsList, Restrictions::vehicleAxleLoadInKg))
                .transportTypes(getValueTransportTypes(restrictionsList))
                .startLocationLatitude(startLocationLatitude)
                .startLocationLongitude(startLocationLongitude)
                .searchRadiusInMeters(searchRadiusInMeters)
                .municipalityId(null)
                .build();
    }

    private Double getValue(List<Restrictions> restrictionsList, Function<Restrictions, Maximum> getValueFunction) {
        OptionalDouble minValue = restrictionsList.stream()
                .map(getValueFunction)
                .filter(Objects::nonNull)
                .mapToDouble(Maximum::value)
                .min();

        if (minValue.isPresent()) {
            return minValue.getAsDouble();
        } else {
            return null;
        }
    }

    private List<TransportType> getValueTransportTypes(List<Restrictions> restrictionsList) {

        return restrictionsList.stream()
                .map(Restrictions::transportTypes)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toList();
    }
}
