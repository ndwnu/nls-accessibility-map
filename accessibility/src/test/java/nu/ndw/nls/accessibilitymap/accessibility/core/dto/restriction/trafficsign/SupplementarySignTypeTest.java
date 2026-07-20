package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign;

import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A2;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A3;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A4;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A5;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A6;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A7;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A8;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22A9;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22C1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E1;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E10;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E4;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E5;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E6;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E7;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E8;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.C22E9;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB01;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB02;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB03;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB04;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB05;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB06;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB07;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB08;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB09;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB10;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB101;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB102;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB103;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB104;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB108;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB109;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB11;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB110;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB113;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB115;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB12;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB13;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB14;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB15;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB16;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB17L;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB17R;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB18L;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB18R;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB19;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB254;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB256;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB259;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB301;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB302;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB303;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB304;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB305;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB306;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB307;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB308;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB309;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB310;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB311;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB313;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB320;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB401;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB411;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB501L;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB501R;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB502;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB503;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB504;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB505;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB51;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB52;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB53;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB54;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB55;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB56;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB57;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB58;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB59;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB60;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB61;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB617;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB618;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB619;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB62;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB620;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB621;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB627;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB63;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB64;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB65;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB66;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB711;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB711L;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB711R;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB712;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB712L;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB712R;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB713;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB713L;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB713R;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB719;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB720;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

class SupplementarySignTypeTest {

    private static final Set<SupplementarySignType> PRE_ANNOUNCEMENTS = EnumSet.of(OB401, OB411);

    private static final Set<SupplementarySignType> TIME_WINDOWED_TYPES = EnumSet.of(OB254, OB256, OB259);

    @Test
    void getDescription() {
        assertThat(C22A1.getDescription()).isEqualTo("Emission class 3-6");
        assertThat(C22A2.getDescription()).isEqualTo("Emission class 4-6");
        assertThat(C22A3.getDescription()).isEqualTo("Emission class 5-6");
        assertThat(C22A4.getDescription()).isEqualTo("Truck, emission class 5-6");
        assertThat(C22A5.getDescription()).isEqualTo("Truck, emission class 6");
        assertThat(C22A6.getDescription()).isEqualTo("Bus, emission class 4-6");
        assertThat(C22A7.getDescription()).isEqualTo("Bus, emission class 6");
        assertThat(C22A8.getDescription()).isEqualTo("Truck and bus, emission class 4-6");
        assertThat(C22A9.getDescription()).isEqualTo("Truck and bus, emission class 6");
        assertThat(C22C1.getDescription()).isEqualTo("Zero emission");
        assertThat(C22E1.getDescription()).isEqualTo("Emission zone 3 and higher for van and trucks");
        assertThat(C22E4.getDescription()).isEqualTo("Emission zone 4 and higher for cars");
        assertThat(C22E5.getDescription()).isEqualTo("Emission zone 5 and higher for cars");
        assertThat(C22E6.getDescription()).isEqualTo("Emission zone 4 and higher for cars and vans");
        assertThat(C22E7.getDescription()).isEqualTo("Emission zone 5 and higher for cars and vans");
        assertThat(C22E8.getDescription()).isEqualTo("Emission zone 6 and higher for trucks");
        assertThat(C22E9.getDescription()).isEqualTo("Emission zone 6 and higher for busses");
        assertThat(C22E10.getDescription()).isEqualTo("Emission zone 6 and higher for trucks and busses");
        assertThat(OB01.getDescription()).isEqualTo("Rider (equestrian)");
        assertThat(OB02.getDescription()).isEqualTo("Bicycle");
        assertThat(OB03.getDescription()).isEqualTo("Moped");
        assertThat(OB04.getDescription()).isEqualTo("(Moped) bicycle");
        assertThat(OB05.getDescription()).isEqualTo("Agricultural traffic");
        assertThat(OB06.getDescription()).isEqualTo("Motorcycle");
        assertThat(OB07.getDescription()).isEqualTo("Motorcycle, car");
        assertThat(OB08.getDescription()).isEqualTo("Car, front");
        assertThat(OB09.getDescription()).isEqualTo("Car, side");
        assertThat(OB10.getDescription()).isEqualTo("Semi-trailer");
        assertThat(OB11.getDescription()).isEqualTo("Truck");
        assertThat(OB12.getDescription()).isEqualTo("Bus");
        assertThat(OB13.getDescription()).isEqualTo("Truck and bus");
        assertThat(OB14.getDescription()).isEqualTo("Tram");
        assertThat(OB15.getDescription()).isEqualTo("Microcar (moped car)");
        assertThat(OB16.getDescription()).isEqualTo("Vehicle for the disabled");
        assertThat(OB17L.getDescription()).isEqualTo("Overhanging branches, left");
        assertThat(OB17R.getDescription()).isEqualTo("Overhanging branches, right");
        assertThat(OB18L.getDescription()).isEqualTo("Soft verge, left");
        assertThat(OB18R.getDescription()).isEqualTo("Soft verge, right");
        assertThat(OB19.getDescription()).isEqualTo("Electric vehicles only");
        assertThat(OB51.getDescription()).isEqualTo("Except riders (equestrians)");
        assertThat(OB52.getDescription()).isEqualTo("Except bicycles");
        assertThat(OB53.getDescription()).isEqualTo("Except mopeds");
        assertThat(OB54.getDescription()).isEqualTo("Except (moped) bicycles");
        assertThat(OB55.getDescription()).isEqualTo("Except agricultural traffic");
        assertThat(OB56.getDescription()).isEqualTo("Except motorcycles");
        assertThat(OB57.getDescription()).isEqualTo("Except motorcycles and cars");
        assertThat(OB58.getDescription()).isEqualTo("Except cars, front");
        assertThat(OB59.getDescription()).isEqualTo("Except cars, side");
        assertThat(OB60.getDescription()).isEqualTo("Except semi-trailers");
        assertThat(OB61.getDescription()).isEqualTo("Except trucks");
        assertThat(OB62.getDescription()).isEqualTo("Except buses");
        assertThat(OB63.getDescription()).isEqualTo("Except trucks and buses");
        assertThat(OB64.getDescription()).isEqualTo("Except trams");
        assertThat(OB65.getDescription()).isEqualTo("Except microcars (moped cars)");
        assertThat(OB66.getDescription()).isEqualTo("Except vehicles for the disabled");
        assertThat(OB101.getDescription()).isEqualTo("Overtaking agricultural traffic permitted");
        assertThat(OB102.getDescription()).isEqualTo("Except verge");
        assertThat(OB103.getDescription()).isEqualTo("Except police");
        assertThat(OB104.getDescription()).isEqualTo("Except scheduled buses");
        assertThat(OB108.getDescription()).isEqualTo("Except local traffic");
        assertThat(OB109.getDescription()).isEqualTo("Except adjoining properties");
        assertThat(OB110.getDescription()).isEqualTo("Only within marked bays");
        assertThat(OB113.getDescription()).isEqualTo("Except trucks, restricted field of view");
        assertThat(OB115.getDescription()).isEqualTo("Moped riders on the carriageway");
        assertThat(OB254.getDescription()).isEqualTo("Period: Mon-Fri 06:00-10:00");
        assertThat(OB256.getDescription()).isEqualTo("End of period: Mon-Fri 06:00-10:00");
        assertThat(OB259.getDescription()).isEqualTo("Except during period: Mon-Fri 06:00-10:00");
        assertThat(OB301.getDescription()).isEqualTo("Enforced with wheel clamps 1");
        assertThat(OB302.getDescription()).isEqualTo("Enforced with wheel clamps 2");
        assertThat(OB303.getDescription()).isEqualTo("Tow-away zone 1");
        assertThat(OB304.getDescription()).isEqualTo("Tow-away zone 2");
        assertThat(OB305.getDescription()).isEqualTo("Bicycles will be removed");
        assertThat(OB306.getDescription()).isEqualTo("Chip card");
        assertThat(OB307.getDescription()).isEqualTo("Debit card");
        assertThat(OB308.getDescription()).isEqualTo("Free");
        assertThat(OB309.getDescription()).isEqualTo("License plate: XXX-XX-X");
        assertThat(OB310.getDescription()).isEqualTo("Parking prohibited during period");
        assertThat(OB311.getDescription()).isEqualTo("Repeated sign");
        assertThat(OB313.getDescription()).isEqualTo("Raised carriageway separation");
        assertThat(OB320.getDescription()).isEqualTo("Text: school zone");
        assertThat(OB401.getDescription()).isEqualTo("Distance indication: 400m");
        assertThat(OB411.getDescription()).isEqualTo("Distance indication arrow: 500m");
        assertThat(OB501L.getDescription()).isEqualTo("Arrow, left");
        assertThat(OB501R.getDescription()).isEqualTo("Arrow, right");
        assertThat(OB502.getDescription()).isEqualTo("Arrows, left and right");
        assertThat(OB503.getDescription()).isEqualTo("Arrows, right and left");
        assertThat(OB504.getDescription()).isEqualTo("Arrows, diagonal");
        assertThat(OB505.getDescription()).isEqualTo("Arrows, oncoming traffic");
        assertThat(OB617.getDescription()).isEqualTo("Bus gate");
        assertThat(OB618.getDescription()).isEqualTo("Railway crossing, length 1127");
        assertThat(OB619.getDescription()).isEqualTo("Bus lane");
        assertThat(OB620.getDescription()).isEqualTo("Lane offset");
        assertThat(OB621.getDescription()).isEqualTo("Raised carriageway separation");
        assertThat(OB627.getDescription()).isEqualTo("Movable obstacle");
        assertThat(OB711.getDescription()).isEqualTo("Priority intersection");
        assertThat(OB711L.getDescription()).isEqualTo("Priority intersection, left");
        assertThat(OB711R.getDescription()).isEqualTo("Priority intersection, right");
        assertThat(OB712.getDescription()).isEqualTo("Priority fork");
        assertThat(OB712L.getDescription()).isEqualTo("Priority fork, left");
        assertThat(OB712R.getDescription()).isEqualTo("Priority fork, right");
        assertThat(OB713.getDescription()).isEqualTo("Priority road, side street");
        assertThat(OB713L.getDescription()).isEqualTo("Priority road, side street left");
        assertThat(OB713R.getDescription()).isEqualTo("Priority road, side street right");
        assertThat(OB719.getDescription()).isEqualTo("Peak-hour lane open");
        assertThat(OB720.getDescription()).isEqualTo("Plus lane open");
        assertThat(OTHER.getDescription()).isEqualTo("Free text: soft verge");
    }

    @Test
    void getValue() {
        assertThat(C22A1.getValue()).isEqualTo("C22a1");
        assertThat(C22A2.getValue()).isEqualTo("C22a2");
        assertThat(C22A3.getValue()).isEqualTo("C22a3");
        assertThat(C22A4.getValue()).isEqualTo("C22a4");
        assertThat(C22A5.getValue()).isEqualTo("C22a5");
        assertThat(C22A6.getValue()).isEqualTo("C22a6");
        assertThat(C22A7.getValue()).isEqualTo("C22a7");
        assertThat(C22A8.getValue()).isEqualTo("C22a8");
        assertThat(C22A9.getValue()).isEqualTo("C22a9");
        assertThat(C22C1.getValue()).isEqualTo("C22c1");
        assertThat(C22E1.getValue()).isEqualTo("C22e1");
        assertThat(C22E4.getValue()).isEqualTo("C22e4");
        assertThat(C22E5.getValue()).isEqualTo("C22e5");
        assertThat(C22E6.getValue()).isEqualTo("C22e6");
        assertThat(C22E7.getValue()).isEqualTo("C22e7");
        assertThat(C22E8.getValue()).isEqualTo("C22e8");
        assertThat(C22E9.getValue()).isEqualTo("C22e9");
        assertThat(C22E10.getValue()).isEqualTo("C22e10");
        assertThat(OB01.getValue()).isEqualTo("OB01");
        assertThat(OB02.getValue()).isEqualTo("OB02");
        assertThat(OB03.getValue()).isEqualTo("OB03");
        assertThat(OB04.getValue()).isEqualTo("OB04");
        assertThat(OB05.getValue()).isEqualTo("OB05");
        assertThat(OB06.getValue()).isEqualTo("OB06");
        assertThat(OB07.getValue()).isEqualTo("OB07");
        assertThat(OB08.getValue()).isEqualTo("OB08");
        assertThat(OB09.getValue()).isEqualTo("OB09");
        assertThat(OB10.getValue()).isEqualTo("OB10");
        assertThat(OB11.getValue()).isEqualTo("OB11");
        assertThat(OB12.getValue()).isEqualTo("OB12");
        assertThat(OB13.getValue()).isEqualTo("OB13");
        assertThat(OB14.getValue()).isEqualTo("OB14");
        assertThat(OB15.getValue()).isEqualTo("OB15");
        assertThat(OB16.getValue()).isEqualTo("OB16");
        assertThat(OB17L.getValue()).isEqualTo("OB17l");
        assertThat(OB17R.getValue()).isEqualTo("OB17r");
        assertThat(OB18L.getValue()).isEqualTo("OB18l");
        assertThat(OB18R.getValue()).isEqualTo("OB18r");
        assertThat(OB19.getValue()).isEqualTo("OB19");
        assertThat(OB51.getValue()).isEqualTo("OB51");
        assertThat(OB52.getValue()).isEqualTo("OB52");
        assertThat(OB53.getValue()).isEqualTo("OB53");
        assertThat(OB54.getValue()).isEqualTo("OB54");
        assertThat(OB55.getValue()).isEqualTo("OB55");
        assertThat(OB56.getValue()).isEqualTo("OB56");
        assertThat(OB57.getValue()).isEqualTo("OB57");
        assertThat(OB58.getValue()).isEqualTo("OB58");
        assertThat(OB59.getValue()).isEqualTo("OB59");
        assertThat(OB60.getValue()).isEqualTo("OB60");
        assertThat(OB61.getValue()).isEqualTo("OB61");
        assertThat(OB62.getValue()).isEqualTo("OB62");
        assertThat(OB63.getValue()).isEqualTo("OB63");
        assertThat(OB64.getValue()).isEqualTo("OB64");
        assertThat(OB65.getValue()).isEqualTo("OB65");
        assertThat(OB66.getValue()).isEqualTo("OB66");
        assertThat(OB101.getValue()).isEqualTo("OB101");
        assertThat(OB102.getValue()).isEqualTo("OB102");
        assertThat(OB103.getValue()).isEqualTo("OB103");
        assertThat(OB104.getValue()).isEqualTo("OB104");
        assertThat(OB108.getValue()).isEqualTo("OB108");
        assertThat(OB109.getValue()).isEqualTo("OB109");
        assertThat(OB110.getValue()).isEqualTo("OB110");
        assertThat(OB113.getValue()).isEqualTo("OB113");
        assertThat(OB115.getValue()).isEqualTo("OB115");
        assertThat(OB254.getValue()).isEqualTo("OB254");
        assertThat(OB256.getValue()).isEqualTo("OB256");
        assertThat(OB259.getValue()).isEqualTo("OB259");
        assertThat(OB301.getValue()).isEqualTo("OB301");
        assertThat(OB302.getValue()).isEqualTo("OB302");
        assertThat(OB303.getValue()).isEqualTo("OB303");
        assertThat(OB304.getValue()).isEqualTo("OB304");
        assertThat(OB305.getValue()).isEqualTo("OB305");
        assertThat(OB306.getValue()).isEqualTo("OB306");
        assertThat(OB307.getValue()).isEqualTo("OB307");
        assertThat(OB308.getValue()).isEqualTo("OB308");
        assertThat(OB309.getValue()).isEqualTo("OB309");
        assertThat(OB310.getValue()).isEqualTo("OB310");
        assertThat(OB311.getValue()).isEqualTo("OB311");
        assertThat(OB313.getValue()).isEqualTo("OB313");
        assertThat(OB320.getValue()).isEqualTo("OB320");
        assertThat(OB401.getValue()).isEqualTo("OB401");
        assertThat(OB411.getValue()).isEqualTo("OB411");
        assertThat(OB501L.getValue()).isEqualTo("OB501l");
        assertThat(OB501R.getValue()).isEqualTo("OB501r");
        assertThat(OB502.getValue()).isEqualTo("OB502");
        assertThat(OB503.getValue()).isEqualTo("OB503");
        assertThat(OB504.getValue()).isEqualTo("OB504");
        assertThat(OB505.getValue()).isEqualTo("OB505");
        assertThat(OB617.getValue()).isEqualTo("OB617");
        assertThat(OB618.getValue()).isEqualTo("OB618");
        assertThat(OB619.getValue()).isEqualTo("OB619");
        assertThat(OB620.getValue()).isEqualTo("OB620");
        assertThat(OB621.getValue()).isEqualTo("OB621");
        assertThat(OB627.getValue()).isEqualTo("OB627");
        assertThat(OB711.getValue()).isEqualTo("OB711");
        assertThat(OB711L.getValue()).isEqualTo("OB711l");
        assertThat(OB711R.getValue()).isEqualTo("OB711r");
        assertThat(OB712.getValue()).isEqualTo("OB712");
        assertThat(OB712L.getValue()).isEqualTo("OB712l");
        assertThat(OB712R.getValue()).isEqualTo("OB712r");
        assertThat(OB713.getValue()).isEqualTo("OB713");
        assertThat(OB713L.getValue()).isEqualTo("OB713l");
        assertThat(OB713R.getValue()).isEqualTo("OB713r");
        assertThat(OB719.getValue()).isEqualTo("OB719");
        assertThat(OB720.getValue()).isEqualTo("OB720");
        assertThat(OTHER.getValue()).isEqualTo("OTHER");
    }

    @Test
    void getWindowTimeTypes() {
        assertThat(SupplementarySignType.getWindowTimeTypes())
                .as("getWindowTimeTypes() should always return the same Set instance with window times: %s", TIME_WINDOWED_TYPES)
                .isSameAs(SupplementarySignType.getWindowTimeTypes())
                .isEqualTo(TIME_WINDOWED_TYPES);
    }

    @Test
    void getPreAnnouncementTypes() {
        assertThat(SupplementarySignType.getPreAnnouncementTypes())
                .as("getPreAnnouncementTypes() should always return the same Set instance with pre-announcements: %s", PRE_ANNOUNCEMENTS)
                .isSameAs(SupplementarySignType.getPreAnnouncementTypes())
                .isEqualTo(PRE_ANNOUNCEMENTS);
    }

    @ParameterizedTest
    @EnumSource(value = SupplementarySignType.class, names = {"OB254","OB256","OB259"}, mode = EnumSource.Mode.EXCLUDE)
    void isTimeWindowed_false(SupplementarySignType supplementarySignType) {
        assertThat(supplementarySignType.isWindowTime()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = SupplementarySignType.class, names = {"OB254","OB256","OB259"}, mode = Mode.INCLUDE)
    void isTimeWindowed_true(SupplementarySignType supplementarySignType) {
        assertThat(supplementarySignType.isWindowTime()).isTrue();
    }

    @Test
    void hasWindowTime_true() {
        SupplementaryTrafficSign supplementaryTrafficSign = SupplementaryTrafficSign.builder()
                .type(OB254)
                .build();

        assertThat(supplementaryTrafficSign.hasWindowTime()).isTrue();
    }

    @Test
    void hasWindowTime_false() {
        SupplementaryTrafficSign supplementaryTrafficSign = SupplementaryTrafficSign.builder()
                .type(OB01)
                .build();

        assertThat(supplementaryTrafficSign.hasWindowTime()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = SupplementarySignType.class)
    void fromValue(SupplementarySignType supplementarySignType) {
        assertThat(SupplementarySignType.fromValue(supplementarySignType.getValue())).isEqualTo(supplementarySignType);
    }

    @Test
    void fromValue_unknown_throwsException() {
        assertThatThrownBy(() -> SupplementarySignType.fromValue("unknown-value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected value 'unknown-value'");
    }
}
