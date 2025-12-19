package nu.ndw.nls.accessibilitymap.test.performance;

import nu.ndw.nls.springboot.gatling.test.AbstractGatlingPerformanceTestIT;
import nu.ndw.nls.springboot.test.component.driver.job.EnableJobDriver;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {GatlingTestApplication.class})
@EnableJobDriver
public class GatlingPerformanceTestIT extends AbstractGatlingPerformanceTestIT {

}
