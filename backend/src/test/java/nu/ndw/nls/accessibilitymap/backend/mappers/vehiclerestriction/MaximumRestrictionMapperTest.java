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
class MaximumRestrictionMapperTest {

    private static final String KEY = "max_length";
    private static final double LENGTH = 5.4;

    @Mock
    private VehicleProperties vehicleProperties;

    private MaximumRestrictionMapper maximumRestrictionMapper;

    @BeforeEach
    void setup() {
        this.maximumRestrictionMapper = new MaximumRestrictionMapper(KEY, VehicleProperties::length);
    }

    @Test
    void getStatement_ok_isRestricted() {
        when(vehicleProperties.length()).thenReturn(LENGTH);

        Optional<Statement> result = maximumRestrictionMapper.getStatement(vehicleProperties);
        assertThat(result).isPresent()
                .hasValueSatisfying(statement -> {
                    assertEquals("max_length < 5.400000", statement.getCondition());
                    assertEquals("0", statement.getValue());
                    assertEquals(Op.MULTIPLY, statement.getOperation());
                });
    }

    @Test
    void getStatement_ok_isNotRestricted() {
        when(vehicleProperties.length()).thenReturn(null);

        Optional<Statement> result = maximumRestrictionMapper.getStatement(vehicleProperties);
        assertThat(result).isEmpty();
    }

}