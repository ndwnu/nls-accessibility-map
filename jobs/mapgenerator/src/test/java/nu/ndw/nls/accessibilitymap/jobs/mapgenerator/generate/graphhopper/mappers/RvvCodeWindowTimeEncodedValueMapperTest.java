package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.graphhopper.mappers;


import static org.assertj.core.api.Assertions.assertThat;

import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.mappers.RvvCodeWindowTimeEncodedValueMapper;
import org.junit.jupiter.api.Test;

class RvvCodeWindowTimeEncodedValueMapperTest {

    private final RvvCodeWindowTimeEncodedValueMapper mapper = new RvvCodeWindowTimeEncodedValueMapper();

    @Test
    void map_ok_c6() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C6)).isEqualTo(WindowTimeEncodedValue.C6);
    }

    @Test
    void map_ok_c7() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C7)).isEqualTo(WindowTimeEncodedValue.C7);
    }

    @Test
    void map_ok_c7b() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C7B)).isEqualTo(WindowTimeEncodedValue.C7B);
    }

    @Test
    void map_ok_c12() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C12)).isEqualTo(WindowTimeEncodedValue.C12);
    }

    @Test
    void map_ok_c22c() {
        assertThat(mapper.map(CmdGenerateGeoJsonType.C22C)).isEqualTo(WindowTimeEncodedValue.C22C);
    }

    @Test
    void map_ok_equalValuesCount() {
        assertThat(CmdGenerateGeoJsonType.values().length).isEqualTo(WindowTimeEncodedValue.values().length);
    }



}