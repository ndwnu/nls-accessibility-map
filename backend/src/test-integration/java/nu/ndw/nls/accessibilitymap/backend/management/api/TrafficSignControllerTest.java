package nu.ndw.nls.accessibilitymap.backend.management.api;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.services.TrafficSignCacheUpdater;
import nu.ndw.nls.accessibilitymap.backend.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = TrafficSignController.class)
@Import({SecurityConfig.class})
@TestPropertySource(properties = {
        "nls.keycloak.url=http://localhost",
})
@AutoConfigureMockMvc
@ActiveProfiles("component-test")
@ExtendWith(MockitoExtension.class)
class TrafficSignControllerTest {

    private static final String CLIENT_ID = "clientId";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrafficSignCacheUpdater trafficSignCacheUpdater;

    private Jwt jwt;

    @BeforeEach
    void setUp() {

        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("azp", CLIENT_ID)
                .build();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            admin, 200
            invalid_authority, 403
            null, 401
            """)
    void reload(String authority, int expectedHttpStatusCode) throws Exception {
        HttpStatus expectedHttpStatus = HttpStatus.valueOf(expectedHttpStatusCode);

        ResultActions mockMvcBuilder = mockMvc
                .perform(MockMvcRequestBuilders.put("/management/traffic-sign/reload")
                        .with((expectedHttpStatus == HttpStatus.UNAUTHORIZED)
                                ? SecurityMockMvcRequestPostProcessors.anonymous()
                                : SecurityMockMvcRequestPostProcessors.jwt()
                                        .jwt(jwt)
                                        .authorities(AuthorityUtils.createAuthorityList(authority))));

        if (expectedHttpStatus.is2xxSuccessful()) {
            mockMvcBuilder.andExpect(status().is(expectedHttpStatus.value()));

            verify(trafficSignCacheUpdater).updateCache();
        } else {
            mockMvcBuilder.andExpect(status().is(expectedHttpStatus.value()));
            verify(trafficSignCacheUpdater, never()).updateCache();
        }
    }
}
