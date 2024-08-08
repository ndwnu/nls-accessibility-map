package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers;

import java.util.Optional;
import java.util.function.BinaryOperator;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;

@Slf4j
public class MaximumSignMapper extends SignMapper<Double> {

    private static final double DEFAULT_VALUE = Double.POSITIVE_INFINITY;
    private static final BinaryOperator<Double> ACCUMULATOR = Double::min;

    public MaximumSignMapper(String rvvCode, DtoSetter<Double> setter) {
        super(rvvCode, setter, DEFAULT_VALUE, ACCUMULATOR);
    }

    @Override
    Optional<Double> getValue(TrafficSignGeoJsonDto trafficSign) {
        if (trafficSign.getProperties().getBlackCode() == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(trafficSign.getProperties().getBlackCode().replace(",", ".")));
        } catch (NumberFormatException ignored) {
            log.debug("Unprocessable value {} for traffic sign with RVV code {} on road section {}",
                    trafficSign.getProperties().getBlackCode(), trafficSign.getProperties().getRvvCode(),
                    trafficSign.getProperties().getRoadSectionId());
            return Optional.empty();
        }
    }

}
