package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportConditions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransportConditionsMapper {

    private static final int MULTIPLIER_FROM_METERS_TO_CM = 100;

    private static final int MULTIPLIER_FROM_TONNE_TO_KILO_GRAM = 1000;

    private final VehicleToTransportTypeMapper vehicleToTransportTypeMapper;

    private final CategoryMapper categoryMapper;

    private final EuroClassificationMapper euroClassificationMapper;

    private final FuelTypeMapper fuelTypeMapper;

    public TransportConditions map(ConditionPropertiesDtoV5Json conditionPropertiesDtoV5Json) {
        if (conditionPropertiesDtoV5Json == null) {
            return TransportConditions.unrestricted();
        }

        Set<TransportType> transportTypes;

        if (CollectionUtils.isEmpty(conditionPropertiesDtoV5Json.getVehicleType())) {
            transportTypes = Collections.emptySet();
        } else {
            transportTypes = conditionPropertiesDtoV5Json.getVehicleType()
                    .stream()
                    .map(vehicleToTransportTypeMapper::map)
                    .collect(Collectors.toSet());
        }

        return TransportConditions.builder()
                .transportTypes(transportTypes)
                .categories(mapToCategories(conditionPropertiesDtoV5Json.getCategory()))
                .timeValidity(conditionPropertiesDtoV5Json.getTimeValidity())
                .emissionClass(euroClassificationMapper.map(conditionPropertiesDtoV5Json.getEmissionClass()))
                .fuelType(fuelTypeMapper.map(conditionPropertiesDtoV5Json.getFuelType()))
                .vehicleAxleLoadInKg(mapMaximum(conditionPropertiesDtoV5Json.getAxleWeight(), MULTIPLIER_FROM_TONNE_TO_KILO_GRAM))
                .vehicleHeightInCm(mapMaximum(conditionPropertiesDtoV5Json.getHeight(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleLengthInCm(mapMaximum(conditionPropertiesDtoV5Json.getLength(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleWeightInKg(mapMaximum(conditionPropertiesDtoV5Json.getWeight(), MULTIPLIER_FROM_METERS_TO_CM))
                .vehicleWidthInCm(mapMaximum(conditionPropertiesDtoV5Json.getWidth(), MULTIPLIER_FROM_METERS_TO_CM))
                .build();
    }

    private Set<Category> mapToCategories(Set<CategoryEnum> categoryEnums) {
        if (CollectionUtils.isEmpty(categoryEnums)) {
            return Collections.emptySet();
        }

        return categoryEnums.stream()
                .map(categoryMapper::map)
                .collect(Collectors.toSet());
    }

    private static Maximum mapMaximum(Double maximum, int multiplier) {
        if (maximum == null) {
            return null;
        }

        return Maximum.builder().value(maximum * multiplier).build();
    }

}


