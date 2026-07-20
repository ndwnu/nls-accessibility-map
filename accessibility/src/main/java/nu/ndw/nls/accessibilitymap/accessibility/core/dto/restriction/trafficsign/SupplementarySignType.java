package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("java:S115")
public enum SupplementarySignType {
    C22A1("C22a1", "Emission class 3-6"),
    C22A2("C22a2", "Emission class 4-6"),
    C22A3("C22a3", "Emission class 5-6"),
    C22A4("C22a4", "Truck, emission class 5-6"),
    C22A5("C22a5", "Truck, emission class 6"),
    C22A6("C22a6", "Bus, emission class 4-6"),
    C22A7("C22a7", "Bus, emission class 6"),
    C22A8("C22a8", "Truck and bus, emission class 4-6"),
    C22A9("C22a9", "Truck and bus, emission class 6"),
    C22C1("C22c1", "Zero emission"),
    C22E1("C22e1", "Emission zone 3 and higher for van and trucks"),
    C22E4("C22e4", "Emission zone 4 and higher for cars"),
    C22E5("C22e5", "Emission zone 5 and higher for cars"),
    C22E6("C22e6", "Emission zone 4 and higher for cars and vans"),
    C22E7("C22e7", "Emission zone 5 and higher for cars and vans"),
    C22E8("C22e8", "Emission zone 6 and higher for trucks"),
    C22E9("C22e9", "Emission zone 6 and higher for busses"),
    C22E10("C22e10", "Emission zone 6 and higher for trucks and busses"),
    OB01("OB01", "Rider (equestrian)"),
    OB02("OB02", "Bicycle"),
    OB03("OB03", "Moped"),
    OB04("OB04", "(Moped) bicycle"),
    OB05("OB05", "Agricultural traffic"),
    OB06("OB06", "Motorcycle"),
    OB07("OB07", "Motorcycle, car"),
    OB08("OB08", "Car, front"),
    OB09("OB09", "Car, side"),
    OB10("OB10", "Semi-trailer"),
    OB11("OB11", "Truck"),
    OB12("OB12", "Bus"),
    OB13("OB13", "Truck and bus"),
    OB14("OB14", "Tram"),
    OB15("OB15", "Microcar (moped car)"),
    OB16("OB16", "Vehicle for the disabled"),
    OB17L("OB17l", "Overhanging branches, left"),
    OB17R("OB17r", "Overhanging branches, right"),
    OB18L("OB18l", "Soft verge, left"),
    OB18R("OB18r", "Soft verge, right"),
    OB19("OB19", "Electric vehicles only"),
    OB51("OB51", "Except riders (equestrians)"),
    OB52("OB52", "Except bicycles"),
    OB53("OB53", "Except mopeds"),
    OB54("OB54", "Except (moped) bicycles"),
    OB55("OB55", "Except agricultural traffic"),
    OB56("OB56", "Except motorcycles"),
    OB57("OB57", "Except motorcycles and cars"),
    OB58("OB58", "Except cars, front"),
    OB59("OB59", "Except cars, side"),
    OB60("OB60", "Except semi-trailers"),
    OB61("OB61", "Except trucks"),
    OB62("OB62", "Except buses"),
    OB63("OB63", "Except trucks and buses"),
    OB64("OB64", "Except trams"),
    OB65("OB65", "Except microcars (moped cars)"),
    OB66("OB66", "Except vehicles for the disabled"),
    OB101("OB101", "Overtaking agricultural traffic permitted"),
    OB102("OB102", "Except verge"),
    OB103("OB103", "Except police"),
    OB104("OB104", "Except scheduled buses"),
    OB108("OB108", "Except local traffic"),
    OB109("OB109", "Except adjoining properties"),
    OB110("OB110", "Only within marked bays"),
    OB113("OB113", "Except trucks, restricted field of view"),
    OB115("OB115", "Moped riders on the carriageway"),
    OB254("OB254", "Period: Mon-Fri 06:00-10:00"),
    OB256("OB256", "End of period: Mon-Fri 06:00-10:00"),
    OB259("OB259", "Except during period: Mon-Fri 06:00-10:00"),
    OB301("OB301", "Enforced with wheel clamps 1"),
    OB302("OB302", "Enforced with wheel clamps 2"),
    OB303("OB303", "Tow-away zone 1"),
    OB304("OB304", "Tow-away zone 2"),
    OB305("OB305", "Bicycles will be removed"),
    OB306("OB306", "Chip card"),
    OB307("OB307", "Debit card"),
    OB308("OB308", "Free"),
    OB309("OB309", "License plate: XXX-XX-X"),
    OB310("OB310", "Parking prohibited during period"),
    OB311("OB311", "Repeated sign"),
    OB313("OB313", "Raised carriageway separation"),
    OB320("OB320", "Text: school zone"),
    OB401("OB401", "Distance indication: 400m"),
    OB411("OB411", "Distance indication arrow: 500m"),
    OB501L("OB501l", "Arrow, left"),
    OB501R("OB501r", "Arrow, right"),
    OB502("OB502", "Arrows, left and right"),
    OB503("OB503", "Arrows, right and left"),
    OB504("OB504", "Arrows, diagonal"),
    OB505("OB505", "Arrows, oncoming traffic"),
    OB617("OB617", "Bus gate"),
    OB618("OB618", "Railway crossing, length 1127"),
    OB619("OB619", "Bus lane"),
    OB620("OB620", "Lane offset"),
    OB621("OB621", "Raised carriageway separation"),
    OB627("OB627", "Movable obstacle"),
    OB711("OB711", "Priority intersection"),
    OB711L("OB711l", "Priority intersection, left"),
    OB711R("OB711r", "Priority intersection, right"),
    OB712("OB712", "Priority fork"),
    OB712L("OB712l", "Priority fork, left"),
    OB712R("OB712r", "Priority fork, right"),
    OB713("OB713", "Priority road, side street"),
    OB713L("OB713l", "Priority road, side street left"),
    OB713R("OB713r", "Priority road, side street right"),
    OB719("OB719", "Peak-hour lane open"),
    OB720("OB720", "Plus lane open"),
    OTHER("OTHER", "Free text: soft verge");

    private static final Set<SupplementarySignType> PRE_ANNOUNCEMENTS = EnumSet.of(OB401, OB411);

    private static final Set<SupplementarySignType> TIME_WINDOWED_TYPES = EnumSet.of(OB254, OB256, OB259);

    private final String value;

    private final String description;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SupplementarySignType fromValue(String value) {
        for (SupplementarySignType supplementarySignType : SupplementarySignType.values()) {
            if (supplementarySignType.value.equals(value)) {
                return supplementarySignType;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

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
