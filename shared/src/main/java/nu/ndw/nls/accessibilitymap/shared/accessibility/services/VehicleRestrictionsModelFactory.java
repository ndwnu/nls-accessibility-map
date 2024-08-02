package nu.ndw.nls.accessibilitymap.shared.accessibility.services;

import com.graphhopper.util.CustomModel;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.shared.accessibility.model.VehicleProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleRestrictionsModelFactory {

    private final RestrictionMapperProvider restrictionMapperProvider;

    public CustomModel getModel(VehicleProperties vehicleProperties) {
        CustomModel vehicleRestrictionsModel = new CustomModel();
        if (Objects.nonNull(vehicleProperties)) {
            restrictionMapperProvider.getMappers().stream()
                    .map(mapper -> mapper.getStatement(vehicleProperties))
                    .flatMap(Optional::stream)
                    .forEach(vehicleRestrictionsModel::addToPriority);
        }
        return vehicleRestrictionsModel;
    }

}
