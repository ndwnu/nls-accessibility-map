package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.mappers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.TrafficSignSnap;
import nu.ndw.nls.accessibilitymap.shared.network.services.NetworkMetaDataService;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrafficSingSnapMapper {
    private final NwbRoadSectionCrudService roadSectionService;
    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;
    private final NetworkMetaDataService networkMetaDataService;
    private final CrsTransformer crsTransformer;
}
