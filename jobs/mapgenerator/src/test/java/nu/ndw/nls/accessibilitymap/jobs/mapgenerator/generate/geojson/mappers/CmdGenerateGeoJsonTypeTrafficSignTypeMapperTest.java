package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;


import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.TrafficSignType;
import org.junit.jupiter.api.Test;

class CmdGenerateGeoJsonTypeTrafficSignTypeMapperTest {

    private final CmdGenerateGeoJsonTypeTrafficSignTypeMapper mapper =
            new CmdGenerateGeoJsonTypeTrafficSignTypeMapper();

    @Test
    void map_ok_C6() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C6)).isEqualTo(TrafficSignType.C6);
    }

    @Test
    void map_ok_C7() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C7)).isEqualTo(TrafficSignType.C7);
    }

    @Test
    void map_ok_C7B() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C7B)).isEqualTo(TrafficSignType.C7B);
    }

    @Test
    void map_ok_C12() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C12)).isEqualTo(TrafficSignType.C12);
    }

    @Test
    void map_ok_C22C() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C22C)).isEqualTo(TrafficSignType.C22C);
    }

    @Test
    void map_fail_unknownRvvCodeMapping() {
        assertThat(CmdGenerateGeoJsonType.values().length).isEqualTo(5);
    }

}
