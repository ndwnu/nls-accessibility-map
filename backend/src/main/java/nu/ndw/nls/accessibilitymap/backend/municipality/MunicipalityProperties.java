package nu.ndw.nls.accessibilitymap.backend.municipality;

import com.esotericsoftware.kryo.kryo5.serializers.FieldSerializer.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.core.yaml.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("ConfigurationProperties")
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:municipality-v6-config.yml", factory = YamlPropertySourceFactory.class)
@Getter
@Validated
@RequiredArgsConstructor
public class MunicipalityProperties {

    @NotNull
    private final Map<String, MunicipalityProperty> municipalities;
}
