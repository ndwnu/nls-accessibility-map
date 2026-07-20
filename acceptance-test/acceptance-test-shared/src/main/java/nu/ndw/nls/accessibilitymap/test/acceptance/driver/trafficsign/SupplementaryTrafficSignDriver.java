package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.SupplementaryTrafficSign;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupplementaryTrafficSignDriver implements StateManagement {

    private final Map<String, SupplementaryTrafficSign> supplementaryTrafficSignDriverMap = new HashMap<>();

    public void addTraficSignConditions(List<SupplementaryTrafficSign> supplementaryTrafficSigns) {
        for (SupplementaryTrafficSign trafficSign : supplementaryTrafficSigns) {
            supplementaryTrafficSignDriverMap.put(trafficSign.name(), trafficSign);
        }
    }

    public Optional<SupplementaryTrafficSign> getSupplementaryTrafficSign(String name) {
        return Optional.ofNullable(supplementaryTrafficSignDriverMap.get(name));
    }

    @Override
    public void clearState() {
        supplementaryTrafficSignDriverMap.clear();
    }
}
