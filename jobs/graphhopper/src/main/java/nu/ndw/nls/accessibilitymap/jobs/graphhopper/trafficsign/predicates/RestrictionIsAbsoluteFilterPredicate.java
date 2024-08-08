package nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.predicates;

import java.util.List;
import java.util.Objects;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.trafficsign.mappers.TrafficSignToDtoMapper.TrafficSignIncludedFilterPredicate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class RestrictionIsAbsoluteFilterPredicate implements TrafficSignIncludedFilterPredicate {

    private static final List<String> IGNORED_TEXT_SIGN_TYPES = List.of(
            // Sign has exceptions, e.g. for local traffic.
            "UIT",
            // Sign is a pre-announcement, e.g. restriction starts in 800 metres.
            "VOOR",
            // Sign text has not been categorized by George. In practice, many of these fall in the former categories,
            // so it's safer to exclude them as well.
            "VRIJ");

    @Override
    public boolean test(TrafficSignGeoJsonDto trafficSignJsonDto) {
        if (CollectionUtils.isEmpty(trafficSignJsonDto.getProperties().getTextSigns())) {
            return true;
        }
        return trafficSignJsonDto.getProperties().getTextSigns().stream()
                .map(TextSignDto::getType)
                .filter(Objects::nonNull)
                .noneMatch(IGNORED_TEXT_SIGN_TYPES::contains);
    }
}
