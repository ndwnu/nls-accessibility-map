package nu.ndw.nls.accessibilitymap.job.trafficsign.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.Direction;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.SupplementaryTrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.ZoneCodeType;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.BlackCodeMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.DirectionMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.SupplementaryTrafficSignMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.TransportRestrictionMapper;
import nu.ndw.nls.accessibilitymap.job.trafficsign.cache.mapper.ZoneCodeTypeMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TextSignDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.DrivingDirectionEnum;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json.ZoneCodeEnum;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.distance.model.CoordinateAndBearing;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrafficSignBuilderTest {

    private static final UUID ID = UUID.randomUUID();

    private static final double FRACTION = 0.5d;

    private static final int ROAD_SECTION_ID = 1;

    private static final String TRAFFIC_REGULATION_ORDER_ID = "trafficRegulationOrderId";

    private static final int ID_SEQUENCE_SUPPLIED_ID = 2;

    private static final String RVV_CODE_C1_STRING = "C1";

    private static final TrafficSignType RVV_CODE_C1_TRAFFIC_SIGN_TYPE = TrafficSignType.C1;

    private static final DrivingDirectionEnum DRIVING_DIRECTION_ENUM = DrivingDirectionEnum.FORTH;

    private static final Direction DIRECTION = Direction.FORWARD;

    private static final ZoneCodeEnum ZONE_CODE_ENUM = ZoneCodeEnum.BEGIN;

    private static final ZoneCodeType ZONE_CODE_TYPE = ZoneCodeType.START;

    private static final double POINT_COORDINATE_X = 3.0;

    private static final double POINT_COORDINATE_Y = 4.0;

    private static final double BLACK_CODE = 5d;

    private static final double COORDINATE_AND_BEARING_X = 6d;

    private static final double COORDINATE_AND_BEARING_Y = 7d;

    @Mock
    private FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @Mock
    private BlackCodeMapper blackCodeMapper;

    @Mock
    private ZoneCodeTypeMapper zoneCodeTypeMapper;

    @Mock
    private DirectionMapper directionMapper;

    @Mock
    private TransportRestrictionMapper transportRestrictionMapper;

    @Mock
    private SupplementaryTrafficSignMapper supplementaryTrafficSignMapper;

    @InjectMocks
    private TrafficSignBuilder trafficSignBuilder;

    @Mock
    private LineString nwbRoadSectionGeometry;

    @Mock
    private TrafficSignGeoJsonDtoV5Json trafficSignGeoJsonDtoV5Json;

    @Mock
    private TrafficSignPropertiesDtoV5Json trafficSignPropertiesDtoV5Json;

    @Mock
    private CoordinateAndBearing coordinateAndBearing;

    @Mock
    private TextSignDtoV5Json textSignDtoV5JsonA;

    @Mock
    private TextSignDtoV5Json textSignDtoV5JsonB;

    @Mock
    private SupplementaryTrafficSign supplementaryTrafficSignA;

    @Mock
    private SupplementaryTrafficSign supplementaryTrafficSignB;

    @Mock
    private AtomicInteger idSequenceSupplier;

    @Mock
    private PointJson pointJson;

    @Mock
    private ConditionsDtoV5Json conditionsDtoV5Json;

    @Mock
    private TransportRestrictions transportRestrictions;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Test
    void mapFromTrafficSignGeoJsonDto_ok() {

        when(trafficSignGeoJsonDtoV5Json.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json);
        when(trafficSignPropertiesDtoV5Json.getFraction()).thenReturn(FRACTION);
        when(trafficSignPropertiesDtoV5Json.getRoadSectionId()).thenReturn(ROAD_SECTION_ID);

        when(fractionAndDistanceCalculator.getCoordinateAndBearing(nwbRoadSectionGeometry, FRACTION))
                .thenReturn(coordinateAndBearing);

        when(trafficSignGeoJsonDtoV5Json.getProperties().getRvvCode()).thenReturn(RVV_CODE_C1_STRING);
        when(idSequenceSupplier.getAndIncrement()).thenReturn(ID_SEQUENCE_SUPPLIED_ID);
        when(trafficSignGeoJsonDtoV5Json.getId()).thenReturn(ID);
        when(trafficSignPropertiesDtoV5Json.getDrivingDirection()).thenReturn(DRIVING_DIRECTION_ENUM);
        when(directionMapper.map(DRIVING_DIRECTION_ENUM)).thenReturn(DIRECTION);
        when(trafficSignGeoJsonDtoV5Json.getGeometry()).thenReturn(pointJson);
        when(pointJson.getCoordinates()).thenReturn(List.of(POINT_COORDINATE_X, POINT_COORDINATE_Y));
        when(trafficSignPropertiesDtoV5Json.getZoneCode()).thenReturn(ZONE_CODE_ENUM);
        when(zoneCodeTypeMapper.map(ZONE_CODE_ENUM)).thenReturn(ZONE_CODE_TYPE);
        when(trafficSignPropertiesDtoV5Json.getTrafficOrderId()).thenReturn(TRAFFIC_REGULATION_ORDER_ID);
        when(blackCodeMapper.map(trafficSignGeoJsonDtoV5Json, RVV_CODE_C1_TRAFFIC_SIGN_TYPE)).thenReturn(BLACK_CODE);
        when(coordinateAndBearing.coordinate()).thenReturn(new Coordinate(COORDINATE_AND_BEARING_X, COORDINATE_AND_BEARING_Y));

        when(trafficSignPropertiesDtoV5Json.getSupplementarySigns()).thenReturn(List.of(textSignDtoV5JsonA, textSignDtoV5JsonB));
        when(supplementaryTrafficSignMapper.map(textSignDtoV5JsonA)).thenReturn(supplementaryTrafficSignA);
        when(supplementaryTrafficSignMapper.map(textSignDtoV5JsonB)).thenReturn(supplementaryTrafficSignB);

        when(trafficSignPropertiesDtoV5Json.getConditions()).thenReturn(conditionsDtoV5Json);
        when(transportRestrictionMapper.map(conditionsDtoV5Json, TRAFFIC_REGULATION_ORDER_ID)).thenReturn(transportRestrictions);

        Optional<TrafficSign> result = trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDtoV5Json,
                idSequenceSupplier);

        assertThat(result).isPresent();
        TrafficSign trafficSign = result.get();
        assertThat(trafficSign.id()).isEqualTo(ID_SEQUENCE_SUPPLIED_ID);
        assertThat(trafficSign.externalId()).isEqualTo(ID.toString());
        assertThat(trafficSign.roadSectionId()).isEqualTo(ROAD_SECTION_ID);
        assertThat(trafficSign.trafficSignType()).isEqualTo(RVV_CODE_C1_TRAFFIC_SIGN_TYPE);
        assertThat(trafficSign.direction()).isEqualTo(DIRECTION);
        assertThat(trafficSign.fraction()).isEqualTo(FRACTION);
        assertThat(trafficSign.latitude()).isEqualTo(POINT_COORDINATE_Y);
        assertThat(trafficSign.longitude()).isEqualTo(POINT_COORDINATE_X);
        assertThat(trafficSign.zoneCodeType()).isEqualTo(ZONE_CODE_TYPE);
        assertThat(trafficSign.trafficRegulationOrderId()).isEqualTo(TRAFFIC_REGULATION_ORDER_ID);
        assertThat(trafficSign.blackCode()).isEqualTo(BLACK_CODE);
        assertThat(trafficSign.networkSnappedLatitude()).isEqualTo(COORDINATE_AND_BEARING_Y);
        assertThat(trafficSign.networkSnappedLongitude()).isEqualTo(COORDINATE_AND_BEARING_X);
        assertThat(trafficSign.supplementaryTrafficSigns()).containsExactly(supplementaryTrafficSignA, supplementaryTrafficSignB);
        assertThat(trafficSign.transportRestrictions()).isEqualTo(transportRestrictions);
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_illegalState_geometryIsNull() {
        when(trafficSignGeoJsonDtoV5Json.toString()).thenReturn("TrafficSignGeoJsonDtoV5Json.toString()");
        when(trafficSignGeoJsonDtoV5Json.getId()).thenReturn(ID);

        assertThat(trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                null,
                trafficSignGeoJsonDtoV5Json,
                idSequenceSupplier)).isEmpty();

        loggerExtension.containsLog(
                Level.DEBUG,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: TrafficSignGeoJsonDtoV5Json.toString()".formatted(ID),
                "Traffic sign with id '%s' is missing a road section.".formatted(ID));
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_illegalState_fractionIsNull() {
        when(trafficSignGeoJsonDtoV5Json.toString()).thenReturn("TrafficSignGeoJsonDtoV5Json.toString()");
        when(trafficSignGeoJsonDtoV5Json.getId()).thenReturn(ID);
        when(trafficSignGeoJsonDtoV5Json.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json);
        when(trafficSignPropertiesDtoV5Json.getFraction()).thenReturn(null);

        assertThat(trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDtoV5Json,
                idSequenceSupplier)).isEmpty();

        loggerExtension.containsLog(
                Level.DEBUG,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: TrafficSignGeoJsonDtoV5Json.toString()".formatted(ID),
                "Traffic sign with id '%s' is missing a fraction.".formatted(ID));
    }

    @Test
    void mapFromTrafficSignGeoJsonDto_illegalState_roadSectionIdIsNull() {
        when(trafficSignGeoJsonDtoV5Json.toString()).thenReturn("TrafficSignGeoJsonDtoV5Json.toString()");
        when(trafficSignGeoJsonDtoV5Json.getId()).thenReturn(ID);
        when(trafficSignGeoJsonDtoV5Json.getProperties()).thenReturn(trafficSignPropertiesDtoV5Json);
        when(trafficSignPropertiesDtoV5Json.getFraction()).thenReturn(1.0);
        when(trafficSignPropertiesDtoV5Json.getRoadSectionId()).thenReturn(null);

        assertThat(trafficSignBuilder.mapFromTrafficSignGeoJsonDto(
                nwbRoadSectionGeometry,
                trafficSignGeoJsonDtoV5Json,
                idSequenceSupplier)).isEmpty();

        loggerExtension.containsLog(
                Level.DEBUG,
                "Traffic sign with id '%s' is incomplete and will be skipped. Traffic sign: TrafficSignGeoJsonDtoV5Json.toString()".formatted(ID),
                "Traffic sign with id '%s' is missing a roadSectionId.".formatted(ID));
    }

}
