package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.mappers;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeature;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.AccessibilityGeoJsonFeatureCollection;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.model.DirectionalRoadSectionAndTrafficSign;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers.GeoJsonIdSequenceSupplier;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EffectivelyAccessibleGeoJsonMapper {

    private final AccessibilityGeoJsonFeatureCollectionMapper accessibilityGeoJsonFeatureCollectionMapper;

    private final RoadSectionAccessibilityGeoJsonFeatureMapper roadSectionAccessibilityGeoJsonFeatureMapper;


    public AccessibilityGeoJsonFeatureCollection map(GeoJsonIdSequenceSupplier geoJsonIdSequenceSupplier,
            List<DirectionalRoadSectionAndTrafficSign> directionalRoadSectionAndTrafficSigns, int nwbVersion) {

        List<AccessibilityGeoJsonFeature> features = directionalRoadSectionAndTrafficSigns.stream()
                .filter(Objects::nonNull)
                .map(directionalRoadSectionAndTrafficSign ->
                        roadSectionAccessibilityGeoJsonFeatureMapper.map(directionalRoadSectionAndTrafficSign,
                                nwbVersion, geoJsonIdSequenceSupplier.next()))
                .toList();

        return accessibilityGeoJsonFeatureCollectionMapper.map(features);
    }
}
