package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass.EURO_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.Category;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportConditions;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.CategoryEnum;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json.VehicleTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransportConditionsMapperTest {

    private static final int MULTIPLIER_FROM_METERS_TO_CM = 100;

    private static final int MULTIPLIER_FROM_TONNE_TO_KILO_GRAM = 1000;

    private static final double WEIGHT = 5.0;

    private static final double LENGTH = 4.0;

    private static final double HEIGHT = 3.0;

    private static final double AXLE_WEIGHT = 2.0;

    private static final String FUEL_TYPE_STRING = "fuel-type";

    private static final int EMISSION_CLASS_NUMBER = 1;

    private static final EmissionClass EMISSION_CLASS = EURO_1;

    private static final String TIME_VALIDITY = "time-validity";

    private static final double WIDTH = 6.0;

    private static final FuelType FUEL_TYPE = FuelType.UNKNOWN;

    @Mock
    private VehicleTypeToTransportTypeMapper vehicleTypeToTransportTypeMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private EuroClassificationMapper euroClassificationMapper;

    @Mock
    private FuelTypeMapper fuelTypeMapper;

    @InjectMocks
    private TransportConditionsMapper transportConditionsMapper;

    @Mock
    private VehicleTypeEnum vehicleTypeEnumA;

    @Mock
    private VehicleTypeEnum vehicleTypeEnumB;

    @Mock
    private TransportType transportTypeA;

    @Mock
    private TransportType transportTypeB;

    @Mock
    private CategoryEnum categoryEnumA;

    @Mock
    private CategoryEnum categoryEnumB;

    @Mock
    private Category categoryA;

    @Mock
    private Category categoryB;

    @Test
    void map() {

        when(vehicleTypeToTransportTypeMapper.map(vehicleTypeEnumA)).thenReturn(Set.of(transportTypeA));
        when(vehicleTypeToTransportTypeMapper.map(vehicleTypeEnumB)).thenReturn(Set.of(transportTypeB));
        when(categoryMapper.map(categoryEnumA)).thenReturn(categoryA);
        when(categoryMapper.map(categoryEnumB)).thenReturn(categoryB);

        when(euroClassificationMapper.map(EMISSION_CLASS_NUMBER)).thenReturn(EMISSION_CLASS);
        when(fuelTypeMapper.map(FUEL_TYPE_STRING)).thenReturn(FUEL_TYPE);

        TransportConditions result = transportConditionsMapper.map(ConditionPropertiesDtoV5Json.builder()
                .vehicleType(Set.of(vehicleTypeEnumA, vehicleTypeEnumB))
                .category(Set.of(categoryEnumA, categoryEnumB))
                .timeValidity(TIME_VALIDITY)
                .emissionClass(EMISSION_CLASS_NUMBER)
                .fuelType(FUEL_TYPE_STRING)
                .axleWeight(AXLE_WEIGHT)
                .height(HEIGHT)
                .length(LENGTH)
                .weight(WEIGHT)
                .width(WIDTH)
                .build());

        assertThat(result.transportTypes()).containsExactlyInAnyOrder(transportTypeA, transportTypeB);
        assertThat(result.categories()).containsExactlyInAnyOrder(categoryA, categoryB);
        assertThat(result.emissionClass()).isEqualTo(EMISSION_CLASS);
        assertThat(result.fuelType()).isEqualTo(FUEL_TYPE);

        assertThat(result.vehicleAxleLoadInKg().isExceeding(AXLE_WEIGHT * MULTIPLIER_FROM_TONNE_TO_KILO_GRAM, true)).isTrue();
        assertThat(result.vehicleAxleLoadInKg().isExceeding(AXLE_WEIGHT * MULTIPLIER_FROM_TONNE_TO_KILO_GRAM, false)).isFalse();
        assertThat(result.vehicleWeightInKg().isExceeding(WEIGHT * MULTIPLIER_FROM_TONNE_TO_KILO_GRAM, true)).isTrue();
        assertThat(result.vehicleWeightInKg().isExceeding(WEIGHT * MULTIPLIER_FROM_TONNE_TO_KILO_GRAM, false)).isFalse();
        assertThat(result.vehicleHeightInCm().isExceeding(HEIGHT * MULTIPLIER_FROM_METERS_TO_CM, true)).isTrue();
        assertThat(result.vehicleHeightInCm().isExceeding(HEIGHT * MULTIPLIER_FROM_METERS_TO_CM, false)).isFalse();
        assertThat(result.vehicleLengthInCm().isExceeding(LENGTH * MULTIPLIER_FROM_METERS_TO_CM, true)).isTrue();
        assertThat(result.vehicleLengthInCm().isExceeding(LENGTH * MULTIPLIER_FROM_METERS_TO_CM, false)).isFalse();
        assertThat(result.vehicleWidthInCm().isExceeding(WIDTH * MULTIPLIER_FROM_METERS_TO_CM, true)).isTrue();
        assertThat(result.vehicleWidthInCm().isExceeding(WIDTH * MULTIPLIER_FROM_METERS_TO_CM, false)).isFalse();
    }

    @Test
    void map_categories_filterOutNullValues() {
        HashSet<CategoryEnum> categories = new HashSet<>();
        categories.add(null);
        TransportConditions result = transportConditionsMapper.map(ConditionPropertiesDtoV5Json.builder()
                        .category(categories)
                .build());

        assertThat(result.categories()).isEmpty();
    }

    @Test
    void map_null() {
        assertThat(transportConditionsMapper.map(null)).isEqualTo(TransportConditions.unrestricted());
    }

    @Test
    void map_nullValues() {
        TransportConditions result = transportConditionsMapper.map(ConditionPropertiesDtoV5Json.builder()
                .build());

        assertThat(result.transportTypes()).isEmpty();
        assertThat(result.categories()).isEmpty();
        assertThat(result.vehicleAxleLoadInKg()).isNull();
        assertThat(result.vehicleWeightInKg()).isNull();
        assertThat(result.vehicleHeightInCm()).isNull();
        assertThat(result.vehicleLengthInCm()).isNull();
        assertThat(result.vehicleWidthInCm()).isNull();
    }
}
