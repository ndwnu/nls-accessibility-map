package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.querygraph;

import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.MUNICIPALITY_CODE;
import static nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink.TRAFFIC_SIGN_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.REVERSED_LINK_ID;
import static nu.ndw.nls.routingmapmatcher.network.model.Link.WAY_ID_KEY;

import com.graphhopper.storage.EdgeIteratorStateReverseExtractor;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import java.util.List;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.routingmapmatcher.network.annotations.mappers.EncodedValuesMapper;
import nu.ndw.nls.routingmapmatcher.network.annotations.model.EncodedValueDto;
import nu.ndw.nls.routingmapmatcher.network.annotations.model.EncodedValuesByTypeDto;
import org.springframework.stereotype.Component;

@Component
public class EdgeManager {

    private static final List<String> EXCLUDED_KEYS = List.of(TRAFFIC_SIGN_ID, WAY_ID_KEY, REVERSED_LINK_ID,
            MUNICIPALITY_CODE);

    private final EncodedValuesByTypeDto<?> encodedValuesByTypeDto;

    private final EdgeSetterRegistry edgeSetterRegistry;

    private final EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor;

    public EdgeManager(EncodedValuesMapper encodedValuesMapper, EdgeSetterRegistry edgeSetterRegistry,
            EdgeIteratorStateReverseExtractor edgeIteratorStateReverseExtractor) {
        this.encodedValuesByTypeDto = encodedValuesMapper.map(AccessibilityLink.class);
        this.edgeSetterRegistry = edgeSetterRegistry;
        this.edgeIteratorStateReverseExtractor = edgeIteratorStateReverseExtractor;
    }

    void setValueOnEdge(EdgeIteratorState edgeIterator, String key, Object value) {
        boolean reverse = edgeIteratorStateReverseExtractor.hasReversed(edgeIterator);
        Class<?> datatypeClass = getDatatypeClassFromKey(key);
        EdgeSetter<?, ?> edgeSetter = getEdgeSetter(datatypeClass);
        edgeSetter.setValue(edgeIterator, key, reverse, value);
    }

    void resetRestrictionsOnEdge(EdgeIterator edgeIterator) {
        boolean reverse = edgeIteratorStateReverseExtractor.hasReversed(edgeIterator);
        encodedValuesByTypeDto.getNetworkEncodedValueNameKeySet().stream()
                .filter(key -> !EXCLUDED_KEYS.contains(key))
                .map(this::mapToEncodedValueDto)
                .forEach(encodedValueDto -> getEdgeSetter(encodedValueDto.valueType())
                        .setDefaultValue(edgeIterator, encodedValueDto.key(), reverse));

    }

    private EncodedValueDto<?, ?> mapToEncodedValueDto(String key) {
        return encodedValuesByTypeDto.getByKey(getDatatypeClassFromKey(key), key)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find value by class %s and key: %s".formatted(getDatatypeClassFromKey(key), key)));
    }

    private EdgeSetter<?, ?> getEdgeSetter(Class<?> datatypeClass) {
        return edgeSetterRegistry.getEdgeSetter(datatypeClass)
                .orElseThrow(() -> new IllegalArgumentException("No EdgeSetter found for %s"
                        .formatted(datatypeClass)));
    }

    private Class<?> getDatatypeClassFromKey(String key) {
        return encodedValuesByTypeDto.getValueTypeByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Invalid key: %s"
                        .formatted(key)));
    }

}
