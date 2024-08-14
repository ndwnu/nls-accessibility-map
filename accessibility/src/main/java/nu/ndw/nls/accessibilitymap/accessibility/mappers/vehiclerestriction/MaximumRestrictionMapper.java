package nu.ndw.nls.accessibilitymap.accessibility.mappers.vehiclerestriction;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import nu.ndw.nls.accessibilitymap.accessibility.model.VehicleProperties;

public class MaximumRestrictionMapper extends RestrictionMapper {

    private static final String EXPRESSION_TEMPLATE = "%s < %s";
    private final Function<VehicleProperties, Double> vehiclePredicate;

    public MaximumRestrictionMapper(String key, Function<VehicleProperties, Double> doubleGetter) {
        super(EXPRESSION_TEMPLATE, key);
        this.vehiclePredicate = doubleGetter;
    }

    @Override
    Optional<String> getOptionalValueString(VehicleProperties vehicleProperties) {
        Double value = vehiclePredicate.apply(vehicleProperties);
        return Optional.ofNullable(value).map(d -> String.format(Locale.US, "%f", d));
    }

}
