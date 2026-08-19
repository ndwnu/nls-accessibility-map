package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.VisitingWindow;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.osm.dto.Window;
import nu.ndw.nls.accessibilitymap.accessibility.osm.openinghours.OpeningHoursVisitor;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@Builder
public record TransportConditions(
        Set<TransportType> transportTypes,
        Set<Category> categories,
        String timeValidity,
        EmissionClass emissionClass,
        FuelType fuelType,
        Maximum vehicleLengthInCm,
        Maximum vehicleHeightInCm,
        Maximum vehicleWidthInCm,
        Maximum vehicleWeightInKg,
        Maximum vehicleAxleLoadInKg
) {

    private static final TransportConditions TRANSPORT_CONDITIONS_UNRESTRICTED = TransportConditions.builder().build();

    private static final ZoneId ZONE_ID_EUROPE_AMSTERDAM = ZoneId.of("Europe/Amsterdam");

    public static TransportConditions unrestricted() {
        return TRANSPORT_CONDITIONS_UNRESTRICTED;
    }

    public boolean conditionsApply(AccessibilityRequest accessibilityRequest) {
        if (this == TRANSPORT_CONDITIONS_UNRESTRICTED) {
            return false;
        }

        List<Predicate<AccessibilityRequest>> activeConditions = getActiveConditions(accessibilityRequest);
        if (activeConditions.isEmpty()) {
            return false;
        }

        return activeConditions.stream()
                .allMatch(restriction -> restriction.test(accessibilityRequest));
    }

    @SuppressWarnings("java:S3776")
    private List<Predicate<AccessibilityRequest>> getActiveConditions(AccessibilityRequest accessibilityRequest) {

        if (Objects.nonNull(timeValidity)
            && !timeValidity.isEmpty()
            && Objects.nonNull(accessibilityRequest.visitingWindow())
            && !isActive().test(accessibilityRequest)) {
            return List.of();
        }

        List<Predicate<AccessibilityRequest>> activeRestrictions = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(transportTypes) && CollectionUtils.isNotEmpty(accessibilityRequest.transportTypes())) {
            activeRestrictions.add(containsTransportType());
        }

        if (CollectionUtils.isNotEmpty(categories) && CollectionUtils.isNotEmpty(accessibilityRequest.categories())) {
            activeRestrictions.add(containsCategory());
        }

        if (CollectionUtils.isNotEmpty(categories) && CollectionUtils.isNotEmpty(accessibilityRequest.categories())) {
            activeRestrictions.add(containsCategory());
        }

        if (Objects.nonNull(fuelType) && CollectionUtils.isNotEmpty(accessibilityRequest.fuelTypes())) {
            activeRestrictions.add(isMatchingFuelType());
        }

        if (Objects.nonNull(emissionClass) && CollectionUtils.isNotEmpty(accessibilityRequest.emissionClasses())) {
            activeRestrictions.add(isMatchingEmissionClass());
        }

        if (Objects.nonNull(vehicleLengthInCm) && Objects.nonNull(accessibilityRequest.vehicleLengthInCm())) {
            activeRestrictions.add(isExceedingVehicleLength());
        }

        if (Objects.nonNull(vehicleWidthInCm) && Objects.nonNull(accessibilityRequest.vehicleWidthInCm())) {
            activeRestrictions.add(isExceedingVehicleWidth());
        }

        if (Objects.nonNull(vehicleHeightInCm) && Objects.nonNull(accessibilityRequest.vehicleHeightInCm())) {
            activeRestrictions.add(isExceedingVehicleHeight());
        }

        if (Objects.nonNull(vehicleWeightInKg) && Objects.nonNull(accessibilityRequest.vehicleWeightInKg())) {
            activeRestrictions.add(isExceedingVehicleWeight());
        }

        if (Objects.nonNull(vehicleAxleLoadInKg) && Objects.nonNull(accessibilityRequest.vehicleAxleLoadInKg())) {
            activeRestrictions.add(isExceedingVehicleAxleLoad());
        }

        return activeRestrictions;
    }

    private Predicate<AccessibilityRequest> isMatchingFuelType() {
        return accessibilityRequest -> accessibilityRequest.fuelTypes().contains(fuelType);
    }

    private Predicate<AccessibilityRequest> isMatchingEmissionClass() {
        return accessibilityRequest -> accessibilityRequest.emissionClasses().contains(emissionClass);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleLength() {
        return accessibilityRequest -> vehicleLengthInCm.isExceeding(accessibilityRequest.vehicleLengthInCm(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleHeight() {
        return accessibilityRequest -> vehicleHeightInCm.isExceeding(accessibilityRequest.vehicleHeightInCm(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleWidth() {
        return accessibilityRequest -> vehicleWidthInCm.isExceeding(accessibilityRequest.vehicleWidthInCm(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleWeight() {
        return accessibilityRequest -> vehicleWeightInKg.isExceeding(accessibilityRequest.vehicleWeightInKg(), false);
    }

    private Predicate<AccessibilityRequest> isExceedingVehicleAxleLoad() {
        return accessibilityRequest -> vehicleAxleLoadInKg.isExceeding(accessibilityRequest.vehicleAxleLoadInKg(), false);
    }

    private Predicate<AccessibilityRequest> containsTransportType() {
        return accessibilityRequest -> transportTypes.stream().anyMatch(accessibilityRequest.transportTypes()::contains);
    }

    private Predicate<AccessibilityRequest> isActive() {
        Optional<List<Window>> parsedWindows = OpeningHoursVisitor.parse(timeValidity);
        if (parsedWindows.isEmpty()) {
            log.warn("Time validity '{}' could not be parsed, assuming the transport conditions are always active.", timeValidity);
            return accessibilityRequest -> true;
        }

        List<Window> windows = parsedWindows.get();

        return accessibilityRequest -> {
            VisitingWindow visitingWindow = accessibilityRequest.visitingWindow();
            LocalDateTime start = visitingWindow.start().atZoneSameInstant(ZONE_ID_EUROPE_AMSTERDAM).toLocalDateTime();
            LocalDateTime end = visitingWindow.end().atZoneSameInstant(ZONE_ID_EUROPE_AMSTERDAM).toLocalDateTime();

            return windows.stream().anyMatch(window -> window.matches(start, end));
        };
    }

    private Predicate<AccessibilityRequest> containsCategory() {
        return accessibilityRequest -> categories.stream().anyMatch(accessibilityRequest.categories()::contains);
    }
}
