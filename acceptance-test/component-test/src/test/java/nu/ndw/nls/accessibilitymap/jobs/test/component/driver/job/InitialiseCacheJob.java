package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.test.component.driver.job.JobDriver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialiseCacheJob {

    private final JobDriver jobDriver;

    @PostConstruct
    public void initialiseCache() {
        log.info("Initialising caches");
        // run once to initialise the caches
        jobDriver.run("job", "initializeCache");
    }
}
