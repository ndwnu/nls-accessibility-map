package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.trafficsignapi.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.TrafficSignType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers.TrafficSignApiRvvCodeMapper;
import org.junit.jupiter.api.Test;

class TrafficSignApiRvvCodeMapperTest {

    private final TrafficSignApiRvvCodeMapper mapper =
            new TrafficSignApiRvvCodeMapper();

    @Test
    void mapRvvCode_ok_allEnumValuesAreMapped() {
        assertThat(CmdGenerateGeoJsonType.values().length).isEqualTo(5);
    }

    @Test
    void mapRvvCode_ok_c6() {
        assertThat(mapper.mapRvvCode(CmdGenerateGeoJsonType.C6)).isEqualTo(Set.of("C6"));
    }

    @Test
    void mapRvvCode_ok_c7() {
        assertThat(mapper.mapRvvCode(CmdGenerateGeoJsonType.C7)).isEqualTo(Set.of("C7"));
    }

    @Test
    void mapRvvCode_ok_c7b() {
        assertThat(mapper.mapRvvCode(CmdGenerateGeoJsonType.C7B)).isEqualTo(Set.of("C7B"));
    }

    @Test
    void mapRvvCode_ok_c12() {
        assertThat(mapper.mapRvvCode(CmdGenerateGeoJsonType.C12)).isEqualTo(Set.of("C12"));
    }

    @Test
    void mapRvvCode_ok_c22c() {
        assertThat(mapper.mapRvvCode(CmdGenerateGeoJsonType.C22C)).isEqualTo(Set.of("C22C"));
    }

    @Test
    void mapTrafficType_ok_c6() {
        assertThat(mapper.map("C6")).isEqualTo(TrafficSignType.C6);
    }

    @Test
    void mapTrafficType_ok_c7() {
        assertThat(mapper.map("C7")).isEqualTo(TrafficSignType.C7);
    }

    @Test
    void mapTrafficType_ok_c7b() {
        assertThat(mapper.map("C7B")).isEqualTo(TrafficSignType.C7B);
    }

    @Test
    void mapTrafficType_ok_c12() {
        assertThat(mapper.map("C12")).isEqualTo(TrafficSignType.C12);
    }

    @Test
    void mapTrafficType_ok_c22c() {
        assertThat(mapper.map("C22C")).isEqualTo(TrafficSignType.C22C);
    }

    @Test
    void mapTrafficType_fail_invalidValue() {
        assertThatThrownBy(() -> mapper.map("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
