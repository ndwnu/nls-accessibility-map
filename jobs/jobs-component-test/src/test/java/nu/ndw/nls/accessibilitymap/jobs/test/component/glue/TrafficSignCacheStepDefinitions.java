package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign.TrafficSignCacheDriverConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSign;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignCacheStepDefinitions {

    private final TrafficSignCacheDriverConfiguration cacheDriverConfiguration;

    private final ObjectMapper objectMapper;

    @Then("validate trafficSignCache")
    public void trafficSigns(List<TrafficSign> trafficSigns) throws IOException {
        TrafficSigns cachedTrafficSigns = objectMapper.readValue(cacheDriverConfiguration.getActiveVersion(), TrafficSigns.class);

        assertThat(trafficSigns.stream()
                .allMatch(trafficSign -> cachedTrafficSigns.stream()
                        .anyMatch(cachedTrafficSign ->
                                cachedTrafficSign.externalId().equals(trafficSign.id())
                        )
                ))
                .withFailMessage("""
                        Traffic signs given do not match traffic signs in cache.
                        Traffic signs given: %s.
                        Traffic signs in cache: %s.
                        """.formatted(trafficSigns, cachedTrafficSigns))
                .isTrue();

    }

}
