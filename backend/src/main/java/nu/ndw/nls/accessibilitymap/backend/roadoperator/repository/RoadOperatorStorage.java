package nu.ndw.nls.accessibilitymap.backend.roadoperator.repository;

import com.esotericsoftware.kryo.kryo5.serializers.FieldSerializer.NotNull;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.backend.roadoperator.repository.dto.RoadOperator;
import nu.ndw.nls.accessibilitymap.backend.yaml.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("ConfigurationProperties")
@Configuration
@ConfigurationProperties
@PropertySource(value = "classpath:data/road-operators.yml", factory = YamlPropertySourceFactory.class)
@Validated
@RequiredArgsConstructor
@Getter
public class RoadOperatorStorage {

    @NotNull
    @Valid
    private final List<RoadOperator> roadOperators;
}
