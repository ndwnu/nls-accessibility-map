package nu.ndw.nls.accessibilitymap.accessibility.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import java.util.List;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.accessibility.mappers.vehiclerestriction.RestrictionMapper;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleRestrictionsModelFactoryTest {

    @Mock
    RestrictionMapper mapperA;
    @Mock
    RestrictionMapper mapperB;
    @Mock
    RestrictionMapper mapperC;

    @Mock
    Statement statementA;
    @Mock
    Statement statementB;

    @Mock
    VehicleProperties vehicleProperties;

    @Mock
    RestrictionMapperProvider restrictionMapperProvider;
    @InjectMocks
    VehicleRestrictionsModelFactory vehicleRestrictionsModelFactory;

    @Test
    void getModel_ok_nonNull() {
        when(restrictionMapperProvider.getMappers()).thenReturn(List.of(mapperA, mapperB, mapperC));
        when(mapperA.getStatement(vehicleProperties)).thenReturn(Optional.of(statementA));
        when(mapperB.getStatement(vehicleProperties)).thenReturn(Optional.of(statementB));
        when(mapperC.getStatement(vehicleProperties)).thenReturn(Optional.empty());

        CustomModel result = vehicleRestrictionsModelFactory.getModel(vehicleProperties);
        assertThat(result.getPriority()).containsExactlyInAnyOrder(statementA, statementB);
    }

    @Test
    void getModel_ok_null() {
        CustomModel result = vehicleRestrictionsModelFactory.getModel(null);
        assertThat(result.getPriority()).isEmpty();
        verify(restrictionMapperProvider, never()).getMappers();
    }

}