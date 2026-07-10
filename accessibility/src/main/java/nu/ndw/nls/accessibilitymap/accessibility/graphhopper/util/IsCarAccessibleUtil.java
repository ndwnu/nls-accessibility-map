package nu.ndw.nls.accessibilitymap.accessibility.graphhopper.util;

import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.AFR;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.BVP;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.DST;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.ERF;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.GRB;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.HR;
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
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.VDA;
import static nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode.WIS;

import java.util.EnumSet;
import java.util.Set;
import lombok.experimental.UtilityClass;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSection;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;

@UtilityClass
public class IsCarAccessibleUtil {

    private static final Set<CarriagewayTypeCode> CAR_ACCESSIBLE_ROADS_CARRIAGEWAY_TYPE_CODES = EnumSet.of(
            PAR,
            PP,
            WIS,
            PR,
            TRB,
            HR,
            AFR,
            OPR,
            VDA,
            VBD,
            VBI,
            VBK,
            DST,
            VBS,
            PKB,
            VBR,
            VBW,
            RB,
            PST,
            PC,
            PKP,
            GRB,
            ERF,
            TN,
            BVP,
            NRB);

    public static boolean isAccessible(AccessibilityNwbRoadSection accessibilityNwbRoadSection, boolean traversingInReversedDirection) {
        if (!isAccessible(accessibilityNwbRoadSection.carriagewayTypeCode())) {
            return false;
        }

        return traversingInReversedDirection
                ? accessibilityNwbRoadSection.backwardAccessible()
                : accessibilityNwbRoadSection.forwardAccessible();
    }

    public static boolean isAccessible(CarriagewayTypeCode carriagewayTypeCode) {
        return CAR_ACCESSIBLE_ROADS_CARRIAGEWAY_TYPE_CODES.contains(carriagewayTypeCode);
    }
}
