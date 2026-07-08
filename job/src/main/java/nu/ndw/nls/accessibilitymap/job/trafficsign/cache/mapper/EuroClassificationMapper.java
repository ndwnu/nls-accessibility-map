package nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper;

import nu.ndw.nls.accessibilitymap.accessibility.core.dto.EmissionClass;
import org.springframework.stereotype.Component;

@Component
public class EuroClassificationMapper {

    public EmissionClass map(Integer euroClassification) {

        if (euroClassification == null) {
            return null;
        }

        if ( euroClassification < 1 || euroClassification >= EmissionClass.values().length) {
            return EmissionClass.UNKNOWN;
        }

        return EmissionClass.values()[euroClassification-1];
    }

}
