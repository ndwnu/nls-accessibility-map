package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import nu.ndw.nls.accessibilitymap.job.trafficsign.emission.dto.EuroClassification;
import org.springframework.stereotype.Component;

@Component
public class EmissionClassMapper {

    public Set<EmissionClass> map(Set<EuroClassification> euroClassifications) {

        return euroClassifications.stream()
                .map(euroClassification -> switch (euroClassification) {
                    case EURO_1 -> EmissionClass.EURO_1;
                    case EURO_2 -> EmissionClass.EURO_2;
                    case EURO_3 -> EmissionClass.EURO_3;
                    case EURO_4 -> EmissionClass.EURO_4;
                    case EURO_5 -> EmissionClass.EURO_5;
                    case EURO_6 -> EmissionClass.EURO_6;
                    default -> throw new IllegalStateException("Euro classification '%s' is not supported." .formatted(euroClassification));
                })
                .collect(Collectors.toSet());
    }

}
