package nu.ndw.nls.accessibilitymap.accessibility.nwb.repository;

import static org.jooq.impl.DSL.function;
import static org.jooq.impl.DSL.val;

import com.graphhopper.util.shapes.BBox;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.data.api.nwb.helpers.types.CarriagewayTypeCode;
import nu.ndw.nls.db.nwb.jooq.generated.tables.NwbRoadSections;
import nu.ndw.nls.db.nwb.jooq.generated.tables.daos.NwbRoadSectionDao;
import nu.ndw.nls.db.nwb.jooq.generated.tables.pojos.NwbRoadSection;
import nu.ndw.nls.db.nwb.jooq.mappers.NwbRoadOperatorMapper;
import nu.ndw.nls.db.nwb.jooq.repositories.JooqNwbRoadSectionCrudRepository;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import nu.ndw.nls.jooq.mappers.ApiDtoJooqDtoMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.impl.DSL;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class NwbRoadSectionGeometryRepository extends JooqNwbRoadSectionCrudRepository {

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final WktMapper wktMapper;

    private static final Field<Geometry> GEOMETRY_IN_WGS84 =
            DSL.field(
                    "ST_Transform({0}, 4326)",
                    NwbRoadSections.ROAD_SECTION.GEOMETRY.getDataType(),
                    NwbRoadSections.ROAD_SECTION.GEOMETRY
            );

    public NwbRoadSectionGeometryRepository(ApiDtoJooqDtoMapper<NwbRoadSectionDto, NwbRoadSection> mapper,
            NwbRoadSectionDao nwbRoadSectionDao,
            NwbRoadOperatorMapper nwbRoadOperatorMapper, GeometryFactoryWgs84 geometryFactoryWgs84, WktMapper wktMapper
    ) {
        super(mapper, nwbRoadSectionDao, nwbRoadOperatorMapper);
        this.geometryFactoryWgs84 = geometryFactoryWgs84;
        this.wktMapper = wktMapper;
    }

    public LineString findGeometryById(int versionId, long roadSectionId) {
        return this.getDao().ctx()
                .select(GEOMETRY_IN_WGS84.as("geometry"))
                .from(getTable())
                .where(NwbRoadSections.ROAD_SECTION.VERSION_ID.eq(versionId))
                .and(NwbRoadSections.ROAD_SECTION.ROAD_SECTION_ID.eq(roadSectionId))
                .fetch()
                .stream()
                .filter(r -> r.get("geometry", Geometry.class) instanceof LineString)
                .map(r -> r.get("geometry", Geometry.class))
                .map(LineString.class::cast)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No geometry found for road section %s".formatted(roadSectionId)));
    }

    private Collector<ImmutablePair<Long, LineString>, ?, Map<Long, LineString>> buildRoadGeometryMap() {
        return Collectors.toMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight
        );
    }

    private Function<Record2<Long, Geometry>, ImmutablePair<Long, LineString>> mapToRoadSectionGeometry() {
        return r -> new ImmutablePair<>(r.get(NwbRoadSections.ROAD_SECTION.ROAD_SECTION_ID),
                (LineString) r.get("geometry", Geometry.class));
    }

    private Predicate<Record2<Long, Geometry>> isGeometryOfTypeLineString() {
        return r -> r.get("geometry", Geometry.class) instanceof LineString;
    }

    public Map<Long, LineString> findGeometriesByArea(int versionId, BBox area, Set<CarriagewayTypeCode> carriagewayTypes) {
        String wktText = wktMapper.toWkt(toPolygon(area));
        int wgs84Srid = 4326;
        Field<Geometry> polygonFromText = function("ST_PolygonFromText", Geometry.class, val(wktText), val(wgs84Srid));
        Set<String> carriagewayTypeCodes = carriagewayTypes.stream().map(CarriagewayTypeCode::getCode).collect(Collectors.toSet());
        return this.getDao().ctx()
                .select(NwbRoadSections.ROAD_SECTION.ROAD_SECTION_ID, GEOMETRY_IN_WGS84.as("geometry"))
                .from(getTable())
                .where(NwbRoadSections.ROAD_SECTION.VERSION_ID.eq(versionId))
                .and(NwbRoadSections.ROAD_SECTION.CARRIAGEWAY_TYPE_CODE.in(carriagewayTypeCodes))
                .and(DSL.condition(
                        "ST_Intersects({0}, {1})",
                        polygonFromText,
                        GEOMETRY_IN_WGS84
                )).fetch()
                .stream()
                .filter(isGeometryOfTypeLineString())
                .map(mapToRoadSectionGeometry())
                .collect(buildRoadGeometryMap());
    }

    public Polygon toPolygon(BBox bbox) {

        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(bbox.minLon, bbox.minLat),
                new Coordinate(bbox.maxLon, bbox.minLat),
                new Coordinate(bbox.maxLon, bbox.maxLat),
                new Coordinate(bbox.minLon, bbox.maxLat),
                new Coordinate(bbox.minLon, bbox.minLat)
        };

        LinearRing shell = geometryFactoryWgs84.createLinearRing(coordinates);
        return geometryFactoryWgs84.createPolygon(shell);
    }
}

