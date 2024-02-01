package nu.ndw.nls.accessibilitymap.backend.mappers.vehiclerestriction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.graphhopper.json.Statement;
import com.graphhopper.json.Statement.Op;
import java.util.Optional;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoEntryRestrictionMapperTest {

    private static final String KEY = "car_access_forbidden";

    @Mock
    private VehicleProperties vehicleProperties;

    private NoEntryRestrictionMapper noEntryRestrictionMapper;

    @BeforeEach
    void setup() {
        this.noEntryRestrictionMapper = new NoEntryRestrictionMapper(KEY, VehicleProperties::carAccessForbidden);
    }

    @Test
    void getStatement_ok_isRestricted() {
        when(vehicleProperties.carAccessForbidden()).thenReturn(true);

        Optional<Statement> result = noEntryRestrictionMapper.getStatement(vehicleProperties);
        assertThat(result).isPresent()
                .hasValueSatisfying(statement -> {
                    assertEquals("car_access_forbidden == true", statement.getCondition());
                    assertEquals("0", statement.getValue());
                    assertEquals(Op.MULTIPLY, statement.getOperation());
                });
    }

    @Test
    void getStatement_ok_isNotRestricted() {
        when(vehicleProperties.carAccessForbidden()).thenReturn(false);

        Optional<Statement> result = noEntryRestrictionMapper.getStatement(vehicleProperties);
        assertThat(result).isEmpty();
    }

}