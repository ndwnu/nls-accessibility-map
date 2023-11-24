package nu.ndw.nls.accessibilitymap.backend.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.FeatureCollectionJson;
import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.wololo.geojson.Feature;
import org.wololo.jts2geojson.GeoJSONWriter;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public class MunicipalityMapper {


    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final GeoJSONWriter JTS_CONVERTER = new GeoJSONWriter();

    public FeatureCollectionJson mapToMunicipalitiesToGeoJSON(List<Municipality> municipalities) {
        try {
            List<Feature> features = new ArrayList<>();
            for (var municipality : municipalities) {
                Map<String, Object> properties = new HashMap<>();
                properties.put("name", municipality.getName());
                properties.put("searchDistance", municipality.getSearchDistanceInMetres());
                features.add(new Feature(municipality.getMunicipalityId(), JTS_CONVERTER.write(municipality.getStartPoint()), properties));
            }
            var geoJSON = JSON_MAPPER.writeValueAsString(JTS_CONVERTER.write(features));
            return JSON_MAPPER.readValue(geoJSON, FeatureCollectionJson.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failure while mapping geometry " + municipalities, e);
        }
    }
}
