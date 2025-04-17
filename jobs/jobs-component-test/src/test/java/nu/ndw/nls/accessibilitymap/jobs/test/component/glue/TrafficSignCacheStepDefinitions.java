package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.jsonunit.core.Option;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.trafficsign.TrafficSignCacheDriverConfiguration;
import nu.ndw.nls.accessibilitymap.jobs.test.component.glue.data.dto.TrafficSign;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignCacheStepDefinitions {

    private final TrafficSignCacheDriverConfiguration cacheDriverConfiguration;

    private final ObjectMapper objectMapper;

    private final TestDataProvider testDataProvider;

    @Then("validate trafficSignCache")
    public void trafficSigns(List<TrafficSign> trafficSigns) throws IOException {
        TrafficSigns cachedTrafficSigns = objectMapper.readValue(cacheDriverConfiguration.getActiveVersion(), TrafficSigns.class);

        assertThat(trafficSigns.stream()
                .allMatch(trafficSign -> cachedTrafficSigns.stream()
                        .anyMatch(cachedTrafficSign ->
                                cachedTrafficSign.externalId().equals(trafficSign.id())
                                        && verifyEmissionRestrictions(cachedTrafficSign, trafficSign)
                        )
                ))
                .withFailMessage("""
                        Traffic signs given do not match traffic signs in cache.
                        Traffic signs given: %s.
                        Traffic signs in cache: %s.
                        """.formatted(trafficSigns, cachedTrafficSigns))
                .isTrue();
    }

    private boolean verifyEmissionRestrictions(
            nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign cachedTrafficSign,
            TrafficSign trafficSignExpected) {

        Restrictions actualRestrictions = cachedTrafficSign.restrictions();
        if (Objects.nonNull(trafficSignExpected.emissionZoneId())
                && (Objects.isNull(cachedTrafficSign.emissionZoneId()) || Objects.isNull(actualRestrictions.emissionZone()))) {
            fail("Traffic sign with id '%s' has an emission zone, but the cache is missing an emission restriction."
                    .formatted(cachedTrafficSign.externalId()));
        }

        if (Objects.isNull(trafficSignExpected.emissionZoneId())) {
            if ((Objects.nonNull(cachedTrafficSign.emissionZoneId()) || Objects.nonNull(actualRestrictions.emissionZone()))) {
                fail("Traffic sign with id '%s' has no emission zone, but the cache has an unexpected emission restriction."
                        .formatted(cachedTrafficSign.externalId()));
            }
            return true;
        }

        String emissionZoneJsonExpected = switch (cachedTrafficSign.emissionZoneId()) {
            case "zone-low" -> testDataProvider.readFromFile("trafficSignCache", "expected-emissionZoneLow.json");
            case "zone-zero" -> testDataProvider.readFromFile("trafficSignCache", "expected-emissionZoneZero.json");
            default -> fail("Unexpected emission zone id '%s' for traffic sign id '%s'".formatted(cachedTrafficSign.emissionZoneId(), cachedTrafficSign.externalId()));
        };

        try {
            assertThatJson(objectMapper.writeValueAsString(actualRestrictions.emissionZone()))
                    .withOptions(Option.IGNORING_ARRAY_ORDER)
                    .isEqualTo(emissionZoneJsonExpected);
        } catch (JsonProcessingException exception) {
            fail(exception.getMessage(), exception);
        }

        return true;
    }
}
