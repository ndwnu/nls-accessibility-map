package nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.predicates;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class RestrictionIsAbsoluteFilterPredicate implements Predicate<TrafficSign> {

    private static final List<TextSignType> IGNORED_TEXT_SIGN_TYPES = List.of(
            // Sign has exceptions, e.g. for local traffic.
            TextSignType.EXCLUDING,
            // Sign is a pre-announcement, e.g. restriction starts in 800 metres.
            TextSignType.PRE_ANNOUNCEMENT,
            // Sign text has not been categorized by George. In practice, many of these fall in the former categories,
            // so it's safer to exclude them as well.
            TextSignType.FREE_TEXT);

    @Override
    public boolean test(TrafficSign trafficSign) {
        if (CollectionUtils.isEmpty(trafficSign.textSigns())) {
            return true;
        }
        return trafficSign.textSigns().stream()
                .map(TextSign::getType)
                .filter(Objects::nonNull)
                .noneMatch(IGNORED_TEXT_SIGN_TYPES::contains);
    }
}
