package nu.ndw.nls.accessibilitymap.jobs.trafficsign.emission.client.configuration;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Bean;

@AllArgsConstructor
@Slf4j
public class EmissionZoneFeignClientConfiguration {

    @Bean(name = "emissionZoneFeignFormatterRegistrar")
    public FeignFormatterRegistrar feignFormatterRegistrar() {

        return formatterRegistry -> formatterRegistry.addFormatterForFieldType(
                OffsetDateTime.class,
                (object, locale) -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((TemporalAccessor) object),
                (object, locale) -> DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(object));
    }
}
