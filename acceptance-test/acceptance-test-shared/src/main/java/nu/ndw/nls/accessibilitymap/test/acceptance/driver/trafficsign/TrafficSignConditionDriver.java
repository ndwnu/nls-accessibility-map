package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSignCondition;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrafficSignConditionDriver implements StateManagement {

    private final Map<String, TrafficSignCondition> trafficSignConditionsMap = new HashMap<>();

    public void addTraficSignConditions(List<TrafficSignCondition> trafficSignConditions) {
        for (TrafficSignCondition trafficSignCondition : trafficSignConditions) {
            trafficSignConditionsMap.put(trafficSignCondition.name(), trafficSignCondition);
        }
    }

    public Optional<TrafficSignCondition> getTrafficSignCondition(String name) {
        return Optional.ofNullable(trafficSignConditionsMap.get(name));
    }

    @Override
    public void clearState() {
        trafficSignConditionsMap.clear();

    }
}
