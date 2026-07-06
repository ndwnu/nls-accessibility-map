package nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign;

import static org.assertj.core.api.Fail.fail;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.mappers.ConditionPropertiesDtoV5JsonMapper;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.ConditionsDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.PointJson;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignGeoJsonDtoV5Json;
import nu.ndw.nls.accessibilitymap.trafficsignclient.feign.generated.model.v1.TrafficSignPropertiesDtoV5Json;
import nu.ndw.nls.geometry.distance.FractionAndDistanceCalculator;
import nu.ndw.nls.springboot.test.graph.dto.Edge;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrafficSignTestDataService {

    private final GraphHopperDriver graphHopperDriver;

    private final FractionAndDistanceCalculator fractionAndDistanceCalculator;

    private final ConditionPropertiesDtoV5JsonMapper conditionPropertiesDtoV5JsonMapper;

    @SuppressWarnings("java:S109")
    public TrafficSignGeoJsonDtoV5Json createTrafficSignGeoJsonDto(TrafficSign trafficSign) {

        Graph graph = graphHopperDriver.getLastBuiltGraph();
        List<Edge> edges = graph.findEdgesBetweenNodes(trafficSign.startNodeId(), trafficSign.endNodeId());

        if (edges.size() != 1) {
            fail("There should be exactly one link between the start and end node. But there was %s"
                    .formatted(edges.size()));
        }
        Edge edge = edges.getFirst();

        LineString fractionLineString = fractionAndDistanceCalculator.getSubLineString(
                edge.getWgs84LineString(),
                trafficSign.fraction());

        if (fractionLineString.getCoordinates().length != 2) {
            fail("There should a start and end coordinate. But there was only %s"
                    .formatted(fractionLineString.getCoordinates().length));
        }
        Coordinate endCoordinate = fractionLineString.getCoordinates()[1];

        return TrafficSignGeoJsonDtoV5Json.builder()
                .id(UUID.fromString(trafficSign.id()))
                .geometry(new PointJson().type("point").coordinates(List.of(endCoordinate.x, endCoordinate.y)))
                .properties(TrafficSignPropertiesDtoV5Json.builder()
                        .fraction(trafficSign.fraction())
                        .blackCode(trafficSign.blackCode())
                        .rvvCode(trafficSign.rvvCode())
                        .drivingDirection(trafficSign.directionType())
                        .roadSectionId(Math.toIntExact(edge.getId()))
                        .trafficOrderId(trafficSign.regulationOrderId())
                        .conditions(ConditionsDtoV5Json.builder()
                                .restrictions(conditionPropertiesDtoV5JsonMapper.map(trafficSign.restrictions()))
                                .exemptions(trafficSign.exemptions()
                                        .stream()
                                        .map(conditionPropertiesDtoV5JsonMapper::map)
                                        .toList())
                                .build())
//                        .textSigns(List.of(
//                                TextSign.builder()
//                                        .type(TextSignType.TIME_PERIOD)
//                                        .text(trafficSign.windowTime())
//                                        .build()))
                        .build())
                .build();
    }


}
