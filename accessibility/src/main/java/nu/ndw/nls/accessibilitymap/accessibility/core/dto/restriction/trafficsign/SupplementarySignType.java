package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.EnumSet;
import java.util.Set;

@SuppressWarnings("java:S115")
public enum SupplementarySignType {
    C22A1,
    C22A2,
    C22A3,
    C22A4,
    C22A5,
    C22A6,
    C22A7,
    C22A8,
    C22A9,
    C22C1,
    C22E1,
    C22E4,
    C22E5,
    C22E6,
    C22E7,
    C22E8,
    C22E9,
    C22E10,
    OB01,
    OB02,
    OB03,
    OB04,
    OB05,
    OB06,
    OB07,
    OB08,
    OB09,
    OB10,
    OB11,
    OB12,
    OB13,
    OB14,
    OB15,
    OB16,
    OB17l,
    OB17r,
    OB18l,
    OB18r,
    OB19,
    OB51,
    OB52,
    OB53,
    OB54,
    OB55,
    OB56,
    OB57,
    OB58,
    OB59,
    OB60,
    OB61,
    OB62,
    OB63,
    OB64,
    OB65,
    OB66,
    OB101,
    OB102,
    OB103,
    OB104,
    OB108,
    OB109,
    OB110,
    OB113,
    OB115,
    OB254,
    OB256,
    OB259,
    OB301,
    OB302,
    OB303,
    OB304,
    OB305,
    OB306,
    OB307,
    OB308,
    OB309,
    OB310,
    OB311,
    OB313,
    OB320,
    OB401,
    OB411,
    OB501l,
    OB501r,
    OB502,
    OB503,
    OB504,
    OB505,
    OB617,
    OB618,
    OB619,
    OB620,
    OB621,
    OB627,
    OB711,
    OB711l,
    OB711r,
    OB712,
    OB712l,
    OB712r,
    OB713,
    OB713l,
    OB713r,
    OB719,
    OB720,
    OTHER;

    private static final Set<SupplementarySignType> PRE_ANNOUNCEMENTS = EnumSet.of(OB401, OB411);

    private static final Set<SupplementarySignType> TIME_WINDOWED_TYPES = EnumSet.of(OB254, OB256, OB259);

    public static Set<SupplementarySignType> getWindowTimeTypes() {
        return TIME_WINDOWED_TYPES;
    }

    public static Set<SupplementarySignType> getPreAnnouncementTypes() {
        return PRE_ANNOUNCEMENTS;
    }

    public boolean isWindowTime() {
        return getWindowTimeTypes().contains(this);
    }

}
