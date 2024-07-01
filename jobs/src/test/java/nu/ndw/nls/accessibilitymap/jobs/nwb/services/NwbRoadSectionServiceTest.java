package nu.ndw.nls.accessibilitymap.jobs.nwb.services;

import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.services.NwbRoadSectionCrudService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NwbRoadSectionServiceTest {

    private static final int VERSION_ID = 20220131;
    private static final int FETCH_SIZE = 250;
    private static final Collection<CarriagewayTypeCode> CARRIAGEWAY_TYPE_CODES;

    @Mock
    private NwbRoadSectionCrudService nwbRoadSectionCrudService;

    @InjectMocks
    private NwbRoadSectionService roadSectionService;

    @Mock
    private Stream<NwbRoadSectionDto> nwbRoadSectionDtoStream;

    static {
        CARRIAGEWAY_TYPE_CODES = new HashSet<>();
        CARRIAGEWAY_TYPE_CODES.addAll(Set.of(RB, VWG, PAR, MRB, NRB, OPR, AFR, PST, VBD, VBI, VBS, VBR, VBK, VBW, DST,
                PKP, PKB, BST, BVP, HR, TN, YYY, TRB, PP, PC, PR, ERF, WIS, GRB, VDA, VD));
        CARRIAGEWAY_TYPE_CODES.add(null);
    }

    @Test
    void findLazyCar_ok() {
        when(nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCode(VERSION_ID, CARRIAGEWAY_TYPE_CODES,
                FETCH_SIZE)).thenReturn(nwbRoadSectionDtoStream);

        assertEquals(nwbRoadSectionDtoStream, roadSectionService.findLazyCar(VERSION_ID));
    }
}
