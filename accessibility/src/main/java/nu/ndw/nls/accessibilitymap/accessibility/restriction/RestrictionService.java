package nu.ndw.nls.accessibilitymap.accessibility.restriction;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restriction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class RestrictionService {

    private final TrafficSignDataService trafficSignDataService;

    public Restrictions findAllBy(AccessibilityRequest accessibilityRequest) {

        return new Restrictions(Stream.concat(
                        trafficSignDataService.findAll().stream(),
                        findAllDynamicRestrictions(accessibilityRequest))
                .filter(restriction -> restriction.isRestrictive(accessibilityRequest))
                .collect(Collectors.toSet()));
    }

    private Stream<Restriction> findAllDynamicRestrictions(AccessibilityRequest accessibilityRequest) {

        // Todo: Implement dynamic restrictions
        return Stream.empty();
    }
}
