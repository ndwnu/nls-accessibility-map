package nu.ndw.nls.accessibilitymap.jobs.trafficsign.cache.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.dto.EuroClassification;
import org.springframework.stereotype.Component;

@Component
public class EmissionClassMapper {

    public Set<EmissionClass> map(Set<EuroClassification> euroClassifications) {

        return euroClassifications.stream()
                .map(euroClassification -> switch (euroClassification) {
                    case EURO_1 -> EmissionClass.ONE;
                    case EURO_2 -> EmissionClass.TWO;
                    case EURO_3 -> EmissionClass.THREE;
                    case EURO_4 -> EmissionClass.FOUR;
                    case EURO_5 -> EmissionClass.FIVE;
                    case EURO_6 -> EmissionClass.SIX;
                    case UNKNOWN -> throw new IllegalStateException("Unknown euro classification '%s'." .formatted(euroClassification));
                })
                .collect(Collectors.toSet());
    }

}
