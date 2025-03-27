package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.RecordComponent;
import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.mappers.vehiclerestriction.RestrictionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import org.junit.jupiter.api.Test;

class RestrictionMapperProviderTest {

    RestrictionMapperProvider restrictionMapperProvider = new RestrictionMapperProvider();

    @Test
    void getMappers_allFieldsCovered() {
        List<RestrictionMapper> mappers = restrictionMapperProvider.getMappers();
        RecordComponent[] vehiclePropertiesComponents = VehicleProperties.class.getRecordComponents();
        assertEquals(vehiclePropertiesComponents.length, mappers.size());
    }

}