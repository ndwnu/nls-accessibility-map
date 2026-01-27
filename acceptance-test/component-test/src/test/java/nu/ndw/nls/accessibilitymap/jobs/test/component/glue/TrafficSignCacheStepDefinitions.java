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
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TransportRestrictions;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.dto.TrafficSigns;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.TrafficSignDriverConfiguration;
import nu.ndw.nls.accessibilitymap.test.acceptance.driver.trafficsign.dto.TrafficSign;
import nu.ndw.nls.springboot.test.component.util.data.TestDataProvider;

@Slf4j
@RequiredArgsConstructor
public class TrafficSignCacheStepDefinitions {

    private final TrafficSignDriverConfiguration cacheDriverConfiguration;

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

    @SuppressWarnings("java:S3658")
    private boolean verifyEmissionRestrictions(
            nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction.trafficsign.TrafficSign cachedTrafficSign,
            TrafficSign trafficSignExpected) {

        TransportRestrictions actualTransportRestrictions = cachedTrafficSign.transportRestrictions();
        if (Objects.nonNull(trafficSignExpected.regulationOrderId())
                && (Objects.isNull(cachedTrafficSign.trafficRegulationOrderId()) || Objects.isNull(actualTransportRestrictions.emissionZone()))) {
            fail("Traffic sign with id '%s' has an emission zone, but the cache is missing an emission restriction."
                    .formatted(cachedTrafficSign.externalId()));
            return false;
        }

        if (Objects.isNull(trafficSignExpected.regulationOrderId())) {
            if ((Objects.nonNull(cachedTrafficSign.trafficRegulationOrderId()) || Objects.nonNull(actualTransportRestrictions.emissionZone()))) {
                fail("Traffic sign with id '%s' has no emission zone, but the cache has an unexpected emission restriction."
                        .formatted(cachedTrafficSign.externalId()));
                return false;
            }
            return true;
        }

        String emissionZoneJsonExpected = switch (cachedTrafficSign.trafficRegulationOrderId()) {
            case "zone-low" -> testDataProvider.readFromFile("trafficSignCache", "expected-emissionZoneLow.json");
            case "zone-zero" -> testDataProvider.readFromFile("trafficSignCache", "expected-emissionZoneZero.json");
            default -> fail("Unexpected emission zone id '%s' for traffic sign id '%s'".formatted(cachedTrafficSign.trafficRegulationOrderId(), cachedTrafficSign.externalId()));
        };

        try {
            assertThatJson(objectMapper.writeValueAsString(actualTransportRestrictions.emissionZone()))
                    .withOptions(Option.IGNORING_ARRAY_ORDER)
                    .isEqualTo(emissionZoneJsonExpected);
        } catch (JsonProcessingException exception) {
            fail(exception.getMessage(), exception);
            return false;
        }

        return true;
    }
}
