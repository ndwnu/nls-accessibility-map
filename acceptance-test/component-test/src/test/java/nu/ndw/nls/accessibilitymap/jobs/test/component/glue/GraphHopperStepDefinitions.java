package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.NwbRoadSection;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.accessibilitymap.AccessibilityMapServicesClient;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity.NwbRoadSectionPrimaryKey;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.entity.RoadSection;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.database.repository.RoadSectionRepository;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperDriver;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.GraphHopperTestDataService;
import nu.ndw.nls.geometry.crs.CrsTransformer;
import nu.ndw.nls.geometry.factories.GeometryFactoryRijksdriehoek;
import nu.ndw.nls.geometry.factories.GeometryFactoryWgs84;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

@Slf4j
@RequiredArgsConstructor
public class GraphHopperStepDefinitions {

    private final GeometryFactoryRijksdriehoek geometryFactoryRijksdriehoek;

    private final CrsTransformer crsTransformer;

    private final RoadSectionRepository roadSectionRepository;

    private final GraphHopperTestDataService graphHopperTestDataService;

    private final GeometryFactoryWgs84 geometryFactoryWgs84 = new GeometryFactoryWgs84();

    private final AccessibilityMapServicesClient accessibilityMapServicesClient;

    @Given("a simple network")
    public void graphHopperNetwork() {

        graphHopperTestDataService.buildSimpleNetwork()
                .insertNwbData()
                .rebuildCache();
        accessibilityMapServicesClient.reloadCaches();
    }

    @Given("a simple network with unroutable road sections")
    public void graphHopperNetworkWithUnroutableRoadSections(List<NwbRoadSection> nwbRoadSections) {

        GraphHopperDriver graphHopperDriver = graphHopperTestDataService.buildSimpleNetwork()
                .insertNwbData();

        nwbRoadSections.forEach(nwbRoadSection -> {
            roadSectionRepository.save(RoadSection.builder()
                    .primaryKey(new NwbRoadSectionPrimaryKey(1, nwbRoadSection.id()))
                    .junctionIdFrom(nwbRoadSection.junctionIdFrom())
                    .junctionIdTo(nwbRoadSection.junctionIdTo())
                    .roadOperatorType("Municipality")
                    .geometry(createRijksdriehoekLineString(geometryFactoryWgs84.createLineString(
                            new Coordinate[]{
                                    new Coordinate(50, 51),
                                    new Coordinate(51, 51)
                            }
                    )))
                    .build());
        });

        graphHopperDriver.rebuildCache();
        accessibilityMapServicesClient.reloadCaches();
    }

    public LineString createRijksdriehoekLineString(LineString latLongLineString) {

        return geometryFactoryRijksdriehoek.createLineString(
                new Coordinate[]{
                        crsTransformer.transformFromWgs84ToRdNew(latLongLineString).getCoordinates()[0],
                        crsTransformer.transformFromWgs84ToRdNew(latLongLineString).getCoordinates()[1]
                }
        );
    }
}
