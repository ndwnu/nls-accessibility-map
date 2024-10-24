package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign.TrafficSignDriver;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.DirectionType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.geojson.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignStepDefinitions {

    private final GraphHopperDriver graphHopperDriver;

    private final TrafficSignDriver trafficSignDriver;

    private final GeometryFactoryWgs84 geometryFactoryWgs84;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final CrsTransformer crsTransformer;


    @Given("with traffic signs")
    public void trafficSigns() {

        trafficSignDriver.stubTrafficSignRequest(
                Set.of("C12"),
                List.of(
                        createTrafficSigns(5, 11, 0.5, "C12"),
                        createTrafficSigns(2, 8, 0.5, "C12")
                )
        );
    }

    private TrafficSignGeoJsonDto createTrafficSigns(long nodeId1, long nodeId2, double fraction, String rvvCode) {

        AccessibilityLink link = graphHopperDriver.getNetworkData().findLinkBetweenNodes(nodeId1, nodeId2);
        Geometry wsg84Geometry = crsTransformer.transformFromRdNewToWgs84(link.getGeometry());

        LineString fractionLineString = fractionAndDistanceCalculator.getSubLineString((LineString) wsg84Geometry, fraction);
        Coordinate endCoordinate = fractionLineString.getCoordinates()[wsg84Geometry.getCoordinates().length-1];

        return TrafficSignGeoJsonDto.builder()
                .id(UUID.randomUUID())
                .geometry(new Point(endCoordinate.getX(), endCoordinate.getY()))
                .properties(TrafficSignPropertiesDto.builder()
                        .fraction(fraction)
                        .rvvCode(rvvCode)
                        .drivingDirection(DirectionType.FORTH)
                        .roadSectionId(link.getId())
                        .textSigns(List.of(TextSign.builder()
                                        .type(TextSignType.TIME_PERIOD)
                                        .text("window")
                                .build()))
                        .build())
                .build();
    }
}
