package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.commands.model.CmdGenerateGeoJsonType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.trafficsign.TrafficSignType;
import org.springframework.stereotype.Component;

@Component
public class CmdGenerateGeoJsonTypeTrafficSignTypeMapper {

    public TrafficSignType map(CmdGenerateGeoJsonType trafficSignType) {
        return switch (trafficSignType) {
            case CmdGenerateGeoJsonType.C6 -> TrafficSignType.C6;
            case CmdGenerateGeoJsonType.C7 -> TrafficSignType.C7;
            case CmdGenerateGeoJsonType.C7B -> TrafficSignType.C7B;
            case CmdGenerateGeoJsonType.C12 -> TrafficSignType.C12;
            case CmdGenerateGeoJsonType.C22C -> TrafficSignType.C22C;
        };
    }
}
