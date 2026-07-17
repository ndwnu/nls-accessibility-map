package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("java:S115")
public enum SupplementarySignType {
    C22A1("Emission class 3-6"),
    C22A2("Emission class 4-6"),
    C22A3("Emission class 5-6"),
    C22A4("Truck, emission class 5-6"),
    C22A5("Truck, emission class 6"),
    C22A6("Bus, emission class 4-6"),
    C22A7("Bus, emission class 6"),
    C22A8("Truck and bus, emission class 4-6"),
    C22A9("Truck and bus, emission class 6"),
    C22C1("Zero emission"),
    C22E1("Emission zone 3 and higher for van and trucks"),
    C22E4("Emission zone 4 and higher for cars"),
    C22E5("Emission zone 5 and higher for cars"),
    C22E6("Emission zone 4 and higher for cars and vans"),
    C22E7("Emission zone 5 and higher for cars and vans"),
    C22E8("Emission zone 6 and higher for trucks"),
    C22E9("Emission zone 6 and higher for busses"),
    C22E10("Emission zone 6 and higher for trucks and busses"),
    OB01("Rider (equestrian)"),
    OB02("Bicycle"),
    OB03("Moped"),
    OB04("(Moped) bicycle"),
    OB05("Agricultural traffic"),
    OB06("Motorcycle"),
    OB07("Motorcycle, car"),
    OB08("Car, front"),
    OB09("Car, side"),
    OB10("Semi-trailer"),
    OB11("Truck"),
    OB12("Bus"),
    OB13("Truck and bus"),
    OB14("Tram"),
    OB15("Microcar (moped car)"),
    OB16("Vehicle for the disabled"),
    OB17l("Overhanging branches, left"),
    OB17r("Overhanging branches, right"),
    OB18l("Soft verge, left"),
    OB18r("Soft verge, right"),
    OB19("Electric vehicles only"),
    OB51("Except riders (equestrians)"),
    OB52("Except bicycles"),
    OB53("Except mopeds"),
    OB54("Except (moped) bicycles"),
    OB55("Except agricultural traffic"),
    OB56("Except motorcycles"),
    OB57("Except motorcycles and cars"),
    OB58("Except cars, front"),
    OB59("Except cars, side"),
    OB60("Except semi-trailers"),
    OB61("Except trucks"),
    OB62("Except buses"),
    OB63("Except trucks and buses"),
    OB64("Except trams"),
    OB65("Except microcars (moped cars)"),
    OB66("Except vehicles for the disabled"),
    OB101("Overtaking agricultural traffic permitted"),
    OB102("Except verge"),
    OB103("Except police"),
    OB104("Except scheduled buses"),
    OB108("Except local traffic"),
    OB109("Except adjoining properties"),
    OB110("Only within marked bays"),
    OB113("Except trucks, restricted field of view"),
    OB115("Moped riders on the carriageway"),
    OB254("Period: Mon-Fri 06:00-10:00"),
    OB256("End of period: Mon-Fri 06:00-10:00"),
    OB259("Except during period: Mon-Fri 06:00-10:00"),
    OB301("Enforced with wheel clamps"),
    OB302("Enforced with wheel clamps"),
    OB303("Enforced with wheel clamps"),
    OB304("Tow-away zone"),
    OB305("Bicycles will be removed"),
    OB306("Chip card"),
    OB307("Debit card"),
    OB308("Free"),
    OB309("License plate: XXX-XX-X"),
    OB310("Parking prohibited during period"),
    OB311("Repeated sign"),
    OB313("Raised carriageway separation"),
    OB320("Text: school zone"),
    OB401("Distance indication: 400m"),
    OB411("Distance indication arrow: 500m"),
    OB501l("Arrow, left"),
    OB501r("Arrow, right"),
    OB502("Arrows, left and right"),
    OB503("Arrows, right and left"),
    OB504("Arrows, diagonal"),
    OB505("Arrows, oncoming traffic"),
    OB617("Bus gate"),
    OB618("Railway crossing, length 1127"),
    OB619("Bus lane"),
    OB620("Lane offset"),
    OB621("Raised carriageway separation"),
    OB627("Movable obstacle"),
    OB711("Priority intersection"),
    OB711l("Priority intersection, left"),
    OB711r("Priority intersection, right"),
    OB712("Priority fork"),
    OB712l("Priority fork, left"),
    OB712r("Priority fork, right"),
    OB713("Priority road, side street"),
    OB713l("Priority road, side street left"),
    OB713r("Priority road, side street right"),
    OB719("Peak-hour lane open"),
    OB720("Plus lane open"),
    OTHER("Free text: soft verge");

    private final String value;

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
