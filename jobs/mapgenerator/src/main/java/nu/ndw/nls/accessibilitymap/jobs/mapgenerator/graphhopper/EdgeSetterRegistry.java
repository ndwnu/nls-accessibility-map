package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EdgeSetterRegistry {

    private final Map<Class<?>, EdgeSetter<?, ?>> edgeSettersByDataType;

    public EdgeSetterRegistry(List<EdgeSetter<?, ?>> edgeSetters) {
        this.edgeSettersByDataType = edgeSetters
                .stream()
                .collect(Collectors.toMap(EdgeSetter::getGetDataTypeClass,
                        Function.identity()));
    }

    public Optional<EdgeSetter<?, ?>> getEdgeSetter(Class<?> dataType) {
        return Optional.ofNullable(edgeSettersByDataType.get(dataType));
    }
}
