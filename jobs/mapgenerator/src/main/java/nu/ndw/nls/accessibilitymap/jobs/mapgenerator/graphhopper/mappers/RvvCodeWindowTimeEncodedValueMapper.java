package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.mappers;

import nu.ndw.nls.accessibilitymap.accessibility.model.WindowTimeEncodedValue;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import org.springframework.stereotype.Component;

@Component
public class RvvCodeWindowTimeEncodedValueMapper {

    public WindowTimeEncodedValue map(CmdGenerateGeoJsonType type) {
        return WindowTimeEncodedValue.valueOf(type.name());
    }

}
