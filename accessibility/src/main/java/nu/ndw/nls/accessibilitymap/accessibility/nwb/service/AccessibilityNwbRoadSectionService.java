package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.mapper.AccessibilityNwbRoadSectionMapper;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
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

    private final NwbVersionCrudService nwbVersionCrudService;

    private final AccessibilityNwbRoadSectionMapper accessibilityNwbRoadSectionMapper;

    public NwbData getLatestNwbData() {
        log.info("Fetching latest NWB data");
        int nwbVersionId = nwbVersionCrudService.findLatestVersionId();
        var accessibilityNwbRoadSections = nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(
                        nwbVersionId,
                        CARRIAGE_WAY_TYPE_CODE_INCLUSIONS,
                        null,
                        FETCH_SIZE)
                .map(accessibilityNwbRoadSectionMapper::map)
                .toList();
        log.info("Fetched {} accessibility road sections", accessibilityNwbRoadSections.size());

        return new NwbData(nwbVersionId, accessibilityNwbRoadSections);
    }
}
