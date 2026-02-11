package nu.ndw.nls.accessibilitymap.accessibility.actuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignDataService;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
        classes = {
                CacheActuator.class,
        },
        properties = {
                "management.endpoints.web.exposure.include=accessibility-map-cache-reload"
        })
@AutoConfigureMockMvc
@EnableAutoConfiguration
class CacheActuatorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NetworkDataService networkDataService;

    @MockitoBean
    private TrafficSignDataService trafficSignDataService;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Test
    void reloadCache() throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/actuator/accessibility-map-cache-reload")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                ).andReturn();

        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

        verify(networkDataService).read();
        verify(trafficSignDataService).read();

        loggerExtension.containsLog(Level.INFO, "Cache reloaded");
    }
}
