package nu.ndw.nls.accessibilitymap.jobs.nwb.services;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NwbRoadSectionService {

    private static final int FETCH_SIZE = 250;
    private static final EnumSet<CarriagewayTypeCode> EXCLUSIONS = EnumSet.of(
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
                .filter(carriagewayTypeCode -> !EXCLUSIONS.contains(carriagewayTypeCode))
                .collect(Collectors.toSet());

        CARRIAGE_WAY_TYPE_CODE_INCLUSIONS.add(null);
    }

    private final NwbRoadSectionCrudService nwbRoadSectionCrudService;

    /**
     * Make sure to add {@link Transactional} around the method in which you use this stream and close the stream after
     * use
     *
     * @param versionId version id
     * @return Lazy stream
     */
    public Stream<NwbRoadSectionDto> findLazyCar(int versionId) {
        return nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCode(versionId,
                CARRIAGE_WAY_TYPE_CODE_INCLUSIONS, FETCH_SIZE);
    }
}
