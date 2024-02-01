package nu.ndw.nls.accessibilitymap.backend.mappers.vehiclerestriction;

import java.util.Optional;
import java.util.function.Predicate;
import nu.ndw.nls.accessibilitymap.backend.model.VehicleProperties;

public class NoEntryRestrictionMapper extends RestrictionMapper {

    private static final String EXPRESSION_TEMPLATE = "%s == %s";
    private final Predicate<VehicleProperties> vehiclePredicate;

    public NoEntryRestrictionMapper(String key, Predicate<VehicleProperties> vehiclePredicate) {
        super(EXPRESSION_TEMPLATE, key);
        this.vehiclePredicate = vehiclePredicate;
    }

    @Override
    Optional<String> getOptionalValueString(VehicleProperties vehicleProperties) {
        return vehiclePredicate.test(vehicleProperties) ? Optional.of("true") : Optional.empty();
    }

}
