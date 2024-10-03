package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.trafficsignapi.mappers;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.springframework.stereotype.Component;

@Component
public class TrafficSignApiRvvCodeMapper {

    public Set<String> mapRvvCode(CmdGenerateGeoJsonType type) {
        return Set.of(type.name());
    }

    public TrafficSignType map(String rvvCode) {
        return TrafficSignType.valueOf(rvvCode);
    }
}
