package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import io.micrometer.core.annotation.Timed;
import java.util.Collection;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AccessibilityNwbRoadSectionService {

    private static final int FETCH_SIZE = 250;

    private static final EnumSet<CarriagewayTypeCode> CARRIAGE_WAY_TYPE_CODE_EXCLUSIONS = EnumSet.of(
            CarriagewayTypeCode.BU,
            CarriagewayTypeCode.BUS,
            CarriagewayTypeCode.FP,
            CarriagewayTypeCode.VP,
            CarriagewayTypeCode.VZ,
            CarriagewayTypeCode.OVB,
            CarriagewayTypeCode.CADO,
            CarriagewayTypeCode.RP,
            CarriagewayTypeCode.VV,
            CarriagewayTypeCode.VDF,
            CarriagewayTypeCode.VDV);

    private static final Collection<CarriagewayTypeCode> CARRIAGE_WAY_TYPE_CODE_INCLUSIONS;

    static {
        CARRIAGE_WAY_TYPE_CODE_INCLUSIONS = Stream.of(CarriagewayTypeCode.values())
                .filter(carriagewayTypeCode -> !CARRIAGE_WAY_TYPE_CODE_EXCLUSIONS.contains(carriagewayTypeCode))
                .collect(Collectors.toSet());

        CARRIAGE_WAY_TYPE_CODE_INCLUSIONS.add(null);
    }

    private final NwbRoadSectionCrudService nwbRoadSectionCrudService;

    private final AccessibilityNwbRoadSectionMapper accessibilityNwbRoadSectionMapper;

    private final SortedMap<Integer, SortedMap<Long, AccessibilityNwbRoadSection>> roadSectionsCacheByVersionById;

    public AccessibilityNwbRoadSectionService(
            NwbRoadSectionCrudService nwbRoadSectionCrudService,
            AccessibilityNwbRoadSectionMapper accessibilityNwbRoadSectionMapper,
            GraphHopperService graphHopperService) {
        this.nwbRoadSectionCrudService = nwbRoadSectionCrudService;
        this.accessibilityNwbRoadSectionMapper = accessibilityNwbRoadSectionMapper;

        roadSectionsCacheByVersionById = new TreeMap<>();
        graphHopperService.registerUpdateListener(this::clearCache);
    }

    @Transactional(readOnly = true)
    public SortedMap<Long, AccessibilityNwbRoadSection> getRoadSectionsByIdForNwbVersion(int nwbVersionId) {
        synchronized (roadSectionsCacheByVersionById) {
            roadSectionsCacheByVersionById.computeIfAbsent(nwbVersionId, loadNwbRoadSections());

            return roadSectionsCacheByVersionById.get(nwbVersionId);
        }
    }

    @Timed(value = "accessibilitymap.nwb.loadNwbRoadSections")
    private @NonNull Function<Integer, SortedMap<Long, AccessibilityNwbRoadSection>> loadNwbRoadSections() {
        return nwbVersionId -> nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                        nwbVersionId,
                        CARRIAGE_WAY_TYPE_CODE_INCLUSIONS,
                        null,
                        FETCH_SIZE)
                .map(accessibilityNwbRoadSectionMapper::map)
                .collect(Collectors.toMap(
                        AccessibilityNwbRoadSection::roadSectionId,               // key mapper (id)
                        Function.identity(),           // value mapper (the object)
                        (a, b) -> a,                   // merge function if duplicate ids occur (pick first; adjust if needed)
                        TreeMap::new
                ));
    }

    private void clearCache() {
        synchronized (roadSectionsCacheByVersionById) {
            log.info("Clearing road sections cache");
            roadSectionsCacheByVersionById.clear();
        }
    }
}
