package nu.ndw.nls.accessibilitymap.accessibility.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.FuelType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityReason;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.AccessibilityRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.FuelTypeRestriction;
import nu.ndw.nls.accessibilitymap.accessibility.service.dto.reasons.TransportTypeRestriction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class FuelTypeRestrictionReducerTest {

    private FuelTypeRestrictionReducer fuelTypeRestrictionReducer;

    @BeforeEach
    void setUp() {

        fuelTypeRestrictionReducer = new FuelTypeRestrictionReducer();
    }

    @Test
    void reduceRestrictions_unique() {
        // this restriction will not be in the result
        TransportTypeRestriction transportTypeRestriction1 = TransportTypeRestriction
                .builder()
                .value(Set.of(TransportType.CAR))
                .build();
        FuelTypeRestriction fuelTypeRestriction1 = createFuelTypeRestriction(Set.of(FuelType.ELECTRIC));
        FuelTypeRestriction fuelTypeRestriction2 = createFuelTypeRestriction(Set.of(FuelType.DIESEL));
        AccessibilityReason accessibilityReason1 = createAccessibilityReason(List.of(transportTypeRestriction1, fuelTypeRestriction1));
        AccessibilityReason accessibilityReason2 = createAccessibilityReason(List.of(fuelTypeRestriction2));

        transportTypeRestriction1.setAccessibilityReason(accessibilityReason1);
        fuelTypeRestriction1.setAccessibilityReason(accessibilityReason1);
        fuelTypeRestriction2.setAccessibilityReason(accessibilityReason2);
        List<AccessibilityReason> result = fuelTypeRestrictionReducer.reduceRestrictions(
                List.of(fuelTypeRestriction1, fuelTypeRestriction2));
        assertThat(result)
                .satisfiesExactly(item1 -> assertThat(item1.restrictions()).containsExactlyInAnyOrder(fuelTypeRestriction1),
                        item2 -> assertThat(item2.restrictions()).containsExactlyInAnyOrder(fuelTypeRestriction2));

    }

    @Test
    void reduceRestrictions_notUnique() {

        FuelTypeRestriction fuelTypeRestriction1 = createFuelTypeRestriction(Set.of(FuelType.DIESEL));
        FuelTypeRestriction fuelTypeRestriction2 = createFuelTypeRestriction(Set.of(FuelType.DIESEL));
        AccessibilityReason accessibilityReason1 = createAccessibilityReason(List.of(fuelTypeRestriction1));
        AccessibilityReason accessibilityReason2 = createAccessibilityReason(List.of(fuelTypeRestriction2));
        fuelTypeRestriction1.setAccessibilityReason(accessibilityReason1);
        fuelTypeRestriction2.setAccessibilityReason(accessibilityReason2);
        List<AccessibilityReason> result = fuelTypeRestrictionReducer.reduceRestrictions(
                List.of(fuelTypeRestriction1, fuelTypeRestriction2));

        assertThat(result)
                .satisfiesExactly(item1 -> assertThat(item1.restrictions()).containsExactlyInAnyOrder(fuelTypeRestriction1));
    }

    public AccessibilityReason createAccessibilityReason(List<AccessibilityRestriction> restrictions) {

        return AccessibilityReason.builder()
                .restrictions(restrictions)
                .build();
    }

    public FuelTypeRestriction createFuelTypeRestriction(Set<FuelType> fuelTypes) {

        return FuelTypeRestriction.builder()
                .value(fuelTypes)
                .build();

    }

    @Test
    void getType() {

        assertThat(fuelTypeRestrictionReducer.getType())
                .isEqualTo(FuelTypeRestriction.class);
    }
}
