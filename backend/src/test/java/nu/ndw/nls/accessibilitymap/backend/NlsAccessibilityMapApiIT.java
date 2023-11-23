package nu.ndw.nls.accessibilitymap.backend;

import com.intuit.karate.junit5.Karate;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.test.karate.KarateConfiguration;
import nu.ndw.nls.springboot.test.main.MainTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest(classes = {MainTestConfiguration.class, KarateConfiguration.class})
@ActiveProfiles("test")
public class NlsAccessibilityMapApiIT {

    @Karate.Test
    Karate health() {
        return Karate.run("classpath:feature/actuator.feature");
    }

    @Karate.Test
    Karate api() {
        return Karate.run("classpath:feature/api.feature");
    }

    @Karate.Test
    Karate openApi() {
        return Karate.run("classpath:feature/openApi.feature");
    }
}
