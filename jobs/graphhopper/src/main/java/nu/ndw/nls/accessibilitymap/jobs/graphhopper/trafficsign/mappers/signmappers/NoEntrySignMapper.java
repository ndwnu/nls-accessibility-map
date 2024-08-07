package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.signmappers;

import java.util.Optional;
import java.util.function.BinaryOperator;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;

@Slf4j
public class NoEntrySignMapper extends SignMapper<Boolean> {

    private static final boolean DEFAULT_VALUE = false;
    private static final BinaryOperator<Boolean> ACCUMULATOR = Boolean::logicalOr;

    public NoEntrySignMapper(String rvvCode, DtoSetter<Boolean> setter) {
        super(rvvCode, setter, DEFAULT_VALUE, ACCUMULATOR);
    }

    @Override
    Optional<Boolean> getValue(TrafficSignGeoJsonDto trafficSign) {
        return Optional.of(true);
    }

}
