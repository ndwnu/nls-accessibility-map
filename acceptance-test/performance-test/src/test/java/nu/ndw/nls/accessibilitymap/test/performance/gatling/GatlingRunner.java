package nu.ndw.nls.accessibilitymap.test.performance.gatling;

import io.gatling.app.Gatling;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GatlingRunner {

	public static int runSimulation(String simulationClass) {

		try {
			Gatling.main(new String[]{
					"--simulation", simulationClass,
					"--results-folder", "target/gatling/results"
			});
			return 0;
		} catch (Exception exception) {
			log.error("Failed to run simulation {}", simulationClass, exception);
			return -1;
		}
	}
}
