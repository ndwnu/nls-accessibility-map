package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import static org.assertj.core.api.Fail.fail;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSign;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TextSignType;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignGeoJsonDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignPropertiesDto;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import org.geojson.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignTestDataService {

    private final GraphHopperDriver graphHopperDriver;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    @SuppressWarnings("java:S109")
    public TrafficSignGeoJsonDto createTrafficSignGeoJsonDto(TrafficSign trafficSign) {

        var graph = graphHopperDriver.getLastBuiltGraph();
        var edges = graph.findEdgesBetweenNodes(trafficSign.startNodeId(), trafficSign.endNodeId());

        if(edges.size() != 1) {
            fail("There should be exactly one link between the start and end node. But there was %s"
                    .formatted(edges.size()));
        }
        var edge = edges.getFirst();

        LineString fractionLineString = fractionAndDistanceCalculator.getSubLineString(
                edge.getWgs84LineString(),
                trafficSign.fraction());

        if (fractionLineString.getCoordinates().length != 2) {
            fail("There should a start and end coordinate. But there was only %s"
                    .formatted(fractionLineString.getCoordinates().length));
        }
        Coordinate endCoordinate = fractionLineString.getCoordinates()[1];

        return TrafficSignGeoJsonDto.builder()
                .id(UUID.fromString(trafficSign.id()))
                .geometry(new Point(endCoordinate.getX(), endCoordinate.getY()))
                .properties(TrafficSignPropertiesDto.builder()
                        .fraction(trafficSign.fraction())
                        .blackCode(trafficSign.blackCode())
                        .rvvCode(trafficSign.rvvCode())
                        .drivingDirection(trafficSign.directionType())
                        .roadSectionId(edge.getId())
                        .trafficOrderUrl(trafficSign.regulationOrderId())
                        .textSigns(List.of(
                                TextSign.builder()
                                        .type(TextSignType.TIME_PERIOD)
                                        .text(trafficSign.windowTime())
                                        .build()))
                        .build())
                .build();
    }

}
