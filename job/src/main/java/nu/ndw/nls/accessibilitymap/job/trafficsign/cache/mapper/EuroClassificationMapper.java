package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import org.springframework.stereotype.Component;

@Component
public class EuroClassificationMapper {

    @SuppressWarnings("java:S109")
    public EmissionClass map(Integer euroClassification) {

        if (euroClassification == null) {
            return null;
        }

        return switch (euroClassification) {
            case 1 -> EmissionClass.EURO_1;
            case 2 -> EmissionClass.EURO_2;
            case 3 -> EmissionClass.EURO_3;
            case 4 -> EmissionClass.EURO_4;
            case 5 -> EmissionClass.EURO_5;
            case 6 -> EmissionClass.EURO_6;
            default -> EmissionClass.UNKNOWN;
        };
    }
}
