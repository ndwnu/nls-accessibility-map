package nu.ndw.nls.accessibilitymap.backend.mappers.vehiclerestriction;

import com.graphhopper.json.Statement;
import com.graphhopper.json.Statement.Op;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;

@RequiredArgsConstructor
public abstract class RestrictionMapper {

    private final String expressionTemplate;
    private final String key;

    public Optional<Statement> getStatement(VehicleProperties vehicleProperties) {
        return this.getOptionalValueString(vehicleProperties).map(this::getNonNullStatement);
    }

    private Statement getNonNullStatement(String valueString) {
        String expression = expressionTemplate.formatted(key, valueString);
        return Statement.If(expression, Op.MULTIPLY, "0");
    }

    abstract Optional<String> getOptionalValueString(VehicleProperties vehicleProperties);

}
