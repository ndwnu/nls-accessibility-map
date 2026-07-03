package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionPropertiesDtoV5Json;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrafficSignConditionDriver implements StateManagement {

    private final Map<String, ConditionPropertiesDtoV5Json> conditions = new HashMap<>();

    public void addCondition(String name, ConditionPropertiesDtoV5Json conditionPropertiesDtoV5Json) {
        this.conditions.put(name, conditionPropertiesDtoV5Json);
    }

    public Optional<ConditionPropertiesDtoV5Json> getCondition(String name) {
        return Optional.ofNullable(conditions.get(name));
    }

    @Override
    public void clearState() {
        conditions.clear();

    }
}
