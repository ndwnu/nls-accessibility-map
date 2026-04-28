package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting;

import static nu.ndw.nls.accessibilitymap.accessibility.graphhopper.weighting.EdgeAccessHandler.isAccessible;
import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EdgeAccessHandlerTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            RB,true,true,true,true
            RB,false,true,true,true
            RB,true,false,false,true
            RB,true,false,true,false
            RB,false,true,false,false
            RB,false,false,false,false
            
            PAR,true,true,true,true
            PP,true,true,true,true
            WIS,true,true,true,true
            PR,true,true,true,true
            TRB,true,true,true,true
            HR,true,true,true,true
            AFR,true,true,true,true
            OPR,true,true,true,true
            VDA,true,true,true,true
            VBD,true,true,true,true
            VBI,true,true,true,true
            VBK,true,true,true,true
            DST,true,true,true,true
            VBS,true,true,true,true
            PKB,true,true,true,true
            VBR,true,true,true,true
            VBW,true,true,true,true
            PST,true,true,true,true
            PC,true,true,true,true
            PKP,true,true,true,true
            GRB,true,true,true,true
            ERF,true,true,true,true
            TN,true,true,true,true
            BVP,true,true,true,true
            NRB,true,true,true,true
            
            FP,true,true,true,false
            """)
    void isAccessible_ok(CarriagewayTypeCode carriagewayTypeCode,
            boolean forwardAccess,
            boolean backwardAccess,
            boolean reversed,
            boolean expectedAccessible
    ) {
        assertThat(isAccessible(carriagewayTypeCode, forwardAccess, backwardAccess, reversed)).isEqualTo(expectedAccessible);
    }
}
