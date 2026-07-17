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
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB17l;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB17r;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB18l;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB18r;
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
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB501l;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB501r;
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
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB711l;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB711r;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB712;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB712l;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB712r;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB713;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB713l;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB713r;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB719;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OB720;
import static nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementarySignType.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupplementarySignTypeTest {

    private static final Set<SupplementarySignType> PRE_ANNOUNCEMENTS = EnumSet.of(OB401, OB411);

    private static final Set<SupplementarySignType> TIME_WINDOWED_TYPES = EnumSet.of(OB254, OB256, OB259);

    @Mock
    private SupplementarySignType supplementarySignType;

    @Test
    void getValue() {
        assertThat(C22A1.getValue()).isEqualTo("Emission class 3-6");
        assertThat(C22A2.getValue()).isEqualTo("Emission class 4-6");
        assertThat(C22A3.getValue()).isEqualTo("Emission class 5-6");
        assertThat(C22A4.getValue()).isEqualTo("Truck, emission class 5-6");
        assertThat(C22A5.getValue()).isEqualTo("Truck, emission class 6");
        assertThat(C22A6.getValue()).isEqualTo("Bus, emission class 4-6");
        assertThat(C22A7.getValue()).isEqualTo("Bus, emission class 6");
        assertThat(C22A8.getValue()).isEqualTo("Truck and bus, emission class 4-6");
        assertThat(C22A9.getValue()).isEqualTo("Truck and bus, emission class 6");
        assertThat(C22C1.getValue()).isEqualTo("Zero emission");
        assertThat(C22E1.getValue()).isEqualTo("Emission zone 3 and higher for van and trucks");
        assertThat(C22E4.getValue()).isEqualTo("Emission zone 4 and higher for cars");
        assertThat(C22E5.getValue()).isEqualTo("Emission zone 5 and higher for cars");
        assertThat(C22E6.getValue()).isEqualTo("Emission zone 4 and higher for cars and vans");
        assertThat(C22E7.getValue()).isEqualTo("Emission zone 5 and higher for cars and vans");
        assertThat(C22E8.getValue()).isEqualTo("Emission zone 6 and higher for trucks");
        assertThat(C22E9.getValue()).isEqualTo("Emission zone 6 and higher for busses");
        assertThat(C22E10.getValue()).isEqualTo("Emission zone 6 and higher for trucks and busses");
        assertThat(OB01.getValue()).isEqualTo("Rider (equestrian)");
        assertThat(OB02.getValue()).isEqualTo("Bicycle");
        assertThat(OB03.getValue()).isEqualTo("Moped");
        assertThat(OB04.getValue()).isEqualTo("(Moped) bicycle");
        assertThat(OB05.getValue()).isEqualTo("Agricultural traffic");
        assertThat(OB06.getValue()).isEqualTo("Motorcycle");
        assertThat(OB07.getValue()).isEqualTo("Motorcycle, car");
        assertThat(OB08.getValue()).isEqualTo("Car, front");
        assertThat(OB09.getValue()).isEqualTo("Car, side");
        assertThat(OB10.getValue()).isEqualTo("Semi-trailer");
        assertThat(OB11.getValue()).isEqualTo("Truck");
        assertThat(OB12.getValue()).isEqualTo("Bus");
        assertThat(OB13.getValue()).isEqualTo("Truck and bus");
        assertThat(OB14.getValue()).isEqualTo("Tram");
        assertThat(OB15.getValue()).isEqualTo("Microcar (moped car)");
        assertThat(OB16.getValue()).isEqualTo("Vehicle for the disabled");
        assertThat(OB17l.getValue()).isEqualTo("Overhanging branches, left");
        assertThat(OB17r.getValue()).isEqualTo("Overhanging branches, right");
        assertThat(OB18l.getValue()).isEqualTo("Soft verge, left");
        assertThat(OB18r.getValue()).isEqualTo("Soft verge, right");
        assertThat(OB19.getValue()).isEqualTo("Electric vehicles only");
        assertThat(OB51.getValue()).isEqualTo("Except riders (equestrians)");
        assertThat(OB52.getValue()).isEqualTo("Except bicycles");
        assertThat(OB53.getValue()).isEqualTo("Except mopeds");
        assertThat(OB54.getValue()).isEqualTo("Except (moped) bicycles");
        assertThat(OB55.getValue()).isEqualTo("Except agricultural traffic");
        assertThat(OB56.getValue()).isEqualTo("Except motorcycles");
        assertThat(OB57.getValue()).isEqualTo("Except motorcycles and cars");
        assertThat(OB58.getValue()).isEqualTo("Except cars, front");
        assertThat(OB59.getValue()).isEqualTo("Except cars, side");
        assertThat(OB60.getValue()).isEqualTo("Except semi-trailers");
        assertThat(OB61.getValue()).isEqualTo("Except trucks");
        assertThat(OB62.getValue()).isEqualTo("Except buses");
        assertThat(OB63.getValue()).isEqualTo("Except trucks and buses");
        assertThat(OB64.getValue()).isEqualTo("Except trams");
        assertThat(OB65.getValue()).isEqualTo("Except microcars (moped cars)");
        assertThat(OB66.getValue()).isEqualTo("Except vehicles for the disabled");
        assertThat(OB101.getValue()).isEqualTo("Overtaking agricultural traffic permitted");
        assertThat(OB102.getValue()).isEqualTo("Except verge");
        assertThat(OB103.getValue()).isEqualTo("Except police");
        assertThat(OB104.getValue()).isEqualTo("Except scheduled buses");
        assertThat(OB108.getValue()).isEqualTo("Except local traffic");
        assertThat(OB109.getValue()).isEqualTo("Except adjoining properties");
        assertThat(OB110.getValue()).isEqualTo("Only within marked bays");
        assertThat(OB113.getValue()).isEqualTo("Except trucks, restricted field of view");
        assertThat(OB115.getValue()).isEqualTo("Moped riders on the carriageway");
        assertThat(OB254.getValue()).isEqualTo("Period: Mon-Fri 06:00-10:00");
        assertThat(OB256.getValue()).isEqualTo("End of period: Mon-Fri 06:00-10:00");
        assertThat(OB259.getValue()).isEqualTo("Except during period: Mon-Fri 06:00-10:00");
        assertThat(OB301.getValue()).isEqualTo("Enforced with wheel clamps 1");
        assertThat(OB302.getValue()).isEqualTo("Enforced with wheel clamps 2");
        assertThat(OB303.getValue()).isEqualTo("Tow-away zone 1");
        assertThat(OB304.getValue()).isEqualTo("Tow-away zone 2");
        assertThat(OB305.getValue()).isEqualTo("Bicycles will be removed");
        assertThat(OB306.getValue()).isEqualTo("Chip card");
        assertThat(OB307.getValue()).isEqualTo("Debit card");
        assertThat(OB308.getValue()).isEqualTo("Free");
        assertThat(OB309.getValue()).isEqualTo("License plate: XXX-XX-X");
        assertThat(OB310.getValue()).isEqualTo("Parking prohibited during period");
        assertThat(OB311.getValue()).isEqualTo("Repeated sign");
        assertThat(OB313.getValue()).isEqualTo("Raised carriageway separation");
        assertThat(OB320.getValue()).isEqualTo("Text: school zone");
        assertThat(OB401.getValue()).isEqualTo("Distance indication: 400m");
        assertThat(OB411.getValue()).isEqualTo("Distance indication arrow: 500m");
        assertThat(OB501l.getValue()).isEqualTo("Arrow, left");
        assertThat(OB501r.getValue()).isEqualTo("Arrow, right");
        assertThat(OB502.getValue()).isEqualTo("Arrows, left and right");
        assertThat(OB503.getValue()).isEqualTo("Arrows, right and left");
        assertThat(OB504.getValue()).isEqualTo("Arrows, diagonal");
        assertThat(OB505.getValue()).isEqualTo("Arrows, oncoming traffic");
        assertThat(OB617.getValue()).isEqualTo("Bus gate");
        assertThat(OB618.getValue()).isEqualTo("Railway crossing, length 1127");
        assertThat(OB619.getValue()).isEqualTo("Bus lane");
        assertThat(OB620.getValue()).isEqualTo("Lane offset");
        assertThat(OB621.getValue()).isEqualTo("Raised carriageway separation");
        assertThat(OB627.getValue()).isEqualTo("Movable obstacle");
        assertThat(OB711.getValue()).isEqualTo("Priority intersection");
        assertThat(OB711l.getValue()).isEqualTo("Priority intersection, left");
        assertThat(OB711r.getValue()).isEqualTo("Priority intersection, right");
        assertThat(OB712.getValue()).isEqualTo("Priority fork");
        assertThat(OB712l.getValue()).isEqualTo("Priority fork, left");
        assertThat(OB712r.getValue()).isEqualTo("Priority fork, right");
        assertThat(OB713.getValue()).isEqualTo("Priority road, side street");
        assertThat(OB713l.getValue()).isEqualTo("Priority road, side street left");
        assertThat(OB713r.getValue()).isEqualTo("Priority road, side street right");
        assertThat(OB719.getValue()).isEqualTo("Peak-hour lane open");
        assertThat(OB720.getValue()).isEqualTo("Plus lane open");
        assertThat(OTHER.getValue()).isEqualTo("Free text: soft verge");
    }

    @Test
    void getWindowTimeTypes() {
        assertThat(SupplementarySignType.getWindowTimeTypes())
                .isSameAs(SupplementarySignType.getWindowTimeTypes())
                .isEqualTo(TIME_WINDOWED_TYPES);
    }

    @Test
    void getPreAnnouncementTypes() {
        assertThat(SupplementarySignType.getPreAnnouncementTypes())
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
        when(supplementarySignType.isWindowTime()).thenReturn(true);
        SupplementaryTrafficSign supplementaryTrafficSign = SupplementaryTrafficSign.builder()
                .type(supplementarySignType)
                .build();

        assertThat(supplementaryTrafficSign.hasWindowTime()).isTrue();
    }

    @Test
    void hasWindowTime_false() {
        when(supplementarySignType.isWindowTime()).thenReturn(false);
        SupplementaryTrafficSign supplementaryTrafficSign = SupplementaryTrafficSign.builder()
                .type(supplementarySignType)
                .build();

        assertThat(supplementaryTrafficSign.hasWindowTime()).isFalse();
    }
}