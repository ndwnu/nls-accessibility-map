package nu.ndw.nls.accessibilitymap.backend.municipality.controllers.mappers;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.municipality.MunicipalityProperty;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MunicipalityCoordinateMapper {

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    public Point map(MunicipalityProperty municipalityProperty) {
        return geometryFactoryWgs84.createPoint(new Coordinate(municipalityProperty.startCoordinateLongitude(),
                municipalityProperty.startCoordinateLatitude()));
    }

}
