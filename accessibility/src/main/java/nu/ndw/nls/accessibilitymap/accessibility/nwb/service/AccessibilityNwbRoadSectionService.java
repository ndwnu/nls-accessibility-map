package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.GraphHopperService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.springframework.stereotype.Service;

@Service
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

    private final SortedMap<Integer, List<AccessibilityNwbRoadSection>> roadSectionsCache;

    public AccessibilityNwbRoadSectionService(
            NwbRoadSectionCrudService nwbRoadSectionCrudService,
            AccessibilityNwbRoadSectionMapper accessibilityNwbRoadSectionMapper,
            GraphHopperService graphHopperService) {
        this.nwbRoadSectionCrudService = nwbRoadSectionCrudService;
        this.accessibilityNwbRoadSectionMapper = accessibilityNwbRoadSectionMapper;

        roadSectionsCache = new TreeMap<>();
        graphHopperService.registerUpdateListener(this::clearCache);
    }

    public List<AccessibilityNwbRoadSection> findAllByVersion(int versionId) {
        synchronized (roadSectionsCache) {
            roadSectionsCache.computeIfAbsent(
                    versionId,
                    version -> nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                                    versionId,
                                    CARRIAGE_WAY_TYPE_CODE_INCLUSIONS,
                                    null,
                                    FETCH_SIZE)
                            .map(accessibilityNwbRoadSectionMapper::map)
                            .toList());

            return roadSectionsCache.get(versionId);
        }
    }

    public List<AccessibilityNwbRoadSection> findAllByVersionAndMunicipalityId(int versionId, int municipalityId) {
        return findAllByVersion(versionId).stream()
                .filter(accessibilityNwbRoadSection -> accessibilityNwbRoadSection.municipalityId() == municipalityId)
                .toList();
    }

    private void clearCache() {
        synchronized (roadSectionsCache) {
            roadSectionsCache.clear();
        }
    }
}
