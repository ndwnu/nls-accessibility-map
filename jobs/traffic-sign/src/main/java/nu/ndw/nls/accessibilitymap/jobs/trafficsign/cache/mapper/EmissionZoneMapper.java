package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache.mapper;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZone;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneExemption;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.emission.EmissionZoneRestriction;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.service.EmissionService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmissionZoneMapper {

    private final EmissionService emissionService;

    private final MaximumWeightMapper maximumWeightMapper;

    private final EmissionClassMapper emissionClassMapper;

    private final FuelTypeMapper fuelTypeMapper;

    private final TransportTypeMapper transportTypeMapper;

    @Valid
    public EmissionZone map(String emissionZoneId) {

        return emissionService.findById(emissionZoneId)
                .filter(nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZone::isActive)
                .map(emissionZone -> {
                    try {
                        return EmissionZone.builder()
                                .startTime(emissionZone.startTime())
                                .endTime(Objects.nonNull(emissionZone.endTime()) ? emissionZone.endTime() : OffsetDateTime.MAX)
                                .exemptions(mapExceptions(emissionZone))
                                .restriction(mapRestriction(emissionZone))
                                .build();
                    } catch (RuntimeException exception) {
                        log.error("Emission zone with id '{}' is incomplete and will be skipped.", emissionZoneId, exception);
                        return null;
                    }
                })
                .orElse(null);
    }

    private EmissionZoneRestriction mapRestriction(nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZone emissionZone) {

        return EmissionZoneRestriction.builder()
                .id(emissionZone.restriction().id())
                .fuelTypes(fuelTypeMapper.map(emissionZone.restriction().fuelType()))
                .transportTypes(
                        transportTypeMapper.map(
                                emissionZone.restriction().vehicleType(),
                                emissionZone.restriction().vehicleCategories()))
                .vehicleWeightInKg(maximumWeightMapper.map(emissionZone.restriction().vehicleCategories()))
                .build();
    }

    private Set<EmissionZoneExemption> mapExceptions(nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EmissionZone emissionZone) {

        if (Objects.isNull(emissionZone.exemptions())) {
            return Set.of();
        }

        return emissionZone.exemptions().stream()
                .map(exemption -> EmissionZoneExemption.builder()
                        .startTime(Objects.nonNull(exemption.startTime()) ? exemption.startTime() : OffsetDateTime.MIN)
                        .endTime(Objects.nonNull(exemption.endTime()) ? exemption.endTime() : OffsetDateTime.MAX)
                        .transportTypes(transportTypeMapper.map(exemption.vehicleCategories()))
                        .vehicleWeightInKg(
                                maximumWeightMapper.map(exemption.vehicleCategories(), mapToDouble(exemption.vehicleWeightInKg())))
                        .emissionClasses(emissionClassMapper.map(exemption.euroClassifications()))
                        .build())
                .collect(Collectors.toSet());
    }

    private Double mapToDouble(Integer value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return Double.valueOf(value);
    }

}
