package nu.ndw.nls.accessibilitymap.accessibility.nwb.service;

import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.AFR;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.BST;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.BVP;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.DST;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.ERF;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.GRB;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.HR;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.MRB;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.NRB;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.OPR;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.PAR;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.PC;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.PKB;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.PKP;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.PP;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.PR;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.PST;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.RB;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.TN;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.TRB;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VBD;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VBI;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VBK;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VBR;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VBS;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VBW;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VD;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VDA;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VWG;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.WIS;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.YYY;
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
    void findLazyCar() {
        when(nwbRoadSectionCrudService.findLazyByVersionIdAndCarriageWayTypeCodeAndMunicipality(VERSION_ID,
                CARRIAGEWAY_TYPE_CODES, null, FETCH_SIZE)).thenReturn(nwbRoadSectionDtoStream);

        assertEquals(nwbRoadSectionDtoStream, roadSectionService.findLazyCar(VERSION_ID));
    }
}
