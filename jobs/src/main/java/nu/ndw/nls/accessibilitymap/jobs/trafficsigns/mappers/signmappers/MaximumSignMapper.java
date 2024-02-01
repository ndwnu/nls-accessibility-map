package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.signmappers;

import java.util.Optional;
import java.util.function.BinaryOperator;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;

@Slf4j
public class MaximumSignMapper extends SignMapper<Double> {

    private static final double DEFAULT_VALUE = Double.POSITIVE_INFINITY;
    private static final BinaryOperator<Double> ACCUMULATOR = Double::min;

    public MaximumSignMapper(String rvvCode, DtoSetter<Double> setter) {
        super(rvvCode, setter, DEFAULT_VALUE, ACCUMULATOR);
    }

    @Override
    Optional<Double> getValue(TrafficSignJsonDtoV3 trafficSign) {
        if (trafficSign.getBlackCode() == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(trafficSign.getBlackCode().replace(",", ".")));
        } catch (NumberFormatException ignored) {
            log.debug("Unprocessable value {} for traffic sign with RVV code {} on road section {}",
                    trafficSign.getBlackCode(), trafficSign.getRvvCode(),
                    trafficSign.getLocation().getRoad().getRoadSectionId());
            return Optional.empty();
        }
    }

}
