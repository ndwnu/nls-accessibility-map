package nu.ndw.nls.accessibilitymap.accessibility.service.debug;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.util.StringUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.springboot.test.graph.exporter.geojson.dto.Properties;
import org.apache.commons.collections4.CollectionUtils;

@Getter
@Builder
public class RestrictionProperties implements Properties {

    private final String type;

    private final long roadSectionId;

    private final Direction direction;

    private final double fraction;

    private final Integer trafficSignId;

    private final String trafficSignExternalId;

    private final TrafficSignType trafficSignType;

    @JsonIgnore
    private final ConditionsProperties restrictions;

    @JsonIgnore
    private final List<ConditionsProperties> exemptions;

    @JsonAnyGetter
    public Map<String, String> getConditions(){
        Map<String, String> values = new HashMap<>();
        values.putAll(mapConditions("restrictions.", restrictions));

        if (exemptions != null) {
            AtomicInteger counter = new AtomicInteger();
            exemptions.forEach(exemption -> values.putAll(mapConditions("exemptions" + counter.incrementAndGet() + ".", exemption)));
        }

        return values;
    }

    private Map<String, String> mapConditions(String keyPrefix, ConditionsProperties conditions) {
        if (conditions == null) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        if (CollectionUtils.isNotEmpty(conditions.categories())) {
            values.put(keyPrefix + "categories", conditions.categories()
                            .stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(",")));
        }

        if (CollectionUtils.isNotEmpty(conditions.transportTypes())) {
            values.put(keyPrefix + "transportTypes", conditions.transportTypes()
                    .stream()
                    .map(Enum::name)
                    .collect(Collectors.joining(",")));
        }

        if (conditions.emissionClass() != null) {
            values.put(keyPrefix + "emissionClass", conditions.emissionClass().name());
        }

        if (conditions.fuelType() != null) {
            values.put(keyPrefix + "fuelType", conditions.fuelType().name());
        }

        if (StringUtils.isNotBlank(conditions.timeValidity())) {
            values.put(keyPrefix + "timeValidity", conditions.timeValidity());
        }

        if (conditions.vehicleAxleLoadInKg() != null) {
            values.put(keyPrefix + "vehicleAxleLoadInKg", "" + conditions.vehicleAxleLoadInKg());
        }

        if (conditions.vehicleWeightInKg() != null) {
            values.put(keyPrefix + "vehicleWeightInKg", "" + conditions.vehicleWeightInKg());
        }

        if (conditions.vehicleLengthInCm() != null) {
            values.put(keyPrefix + "vehicleLengthInCm", "" + conditions.vehicleLengthInCm());
        }

        if (conditions.vehicleWidthInCm() != null) {
            values.put(keyPrefix + "vehicleWidthInCm", "" + conditions.vehicleWidthInCm());
        }

        if (conditions.vehicleHeightInCm() != null) {
            values.put(keyPrefix + "vehicleHeightInCm", "" + conditions.vehicleHeightInCm());
        }

        return values;
    }

}
