package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.graphhopper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GraphHopperDriver {

//    private final GraphHopperNetworkService graphHopperNetworkService;

//    private NetworkGraphHopper createNetwork(List<AccessibilityLink>... links) {
//
//        RoutingNetworkSettings<AccessibilityLink> routingNetworkSettings = RoutingNetworkSettings.builder(
//                        AccessibilityLink.class)
//                .indexed(true)
//                .linkSupplier(() -> join(List.of(links)).iterator())
//                .dataDate(OffsetDateTime.now().toInstant())
//                .build();
//
//        graphHopperNetworkService.storeOnDisk(routingNetworkSettings);
//
//        try {
//           return graphHopperNetworkService.loadFromDisk(routingNetworkSettings);
//        } catch (GraphHopperNotImportedException exception) {
//            fail(exception);
//        }
//        fail("No Graph Hopper network could be created.");
//        return null;
//    }

}
