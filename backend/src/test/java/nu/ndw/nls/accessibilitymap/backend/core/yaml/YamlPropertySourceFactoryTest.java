package nu.ndw.nls.accessibilitymap.backend.core.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.support.EncodedResource;

class YamlPropertySourceFactoryTest {

    @Test
    void createPropertySourceWithValidYaml() {
        EncodedResource encodedResource = new EncodedResource(new FileUrlResource(
                Objects.requireNonNull(getClass().getResource("/property-sources-test.yml"))));
        YamlPropertySourceFactory yamlPropertySourceFactory = new YamlPropertySourceFactory();

        PropertySource<?> propertySource = yamlPropertySourceFactory.createPropertySource("testSource", encodedResource);

        assertNotNull(propertySource);
        assertEquals("property-sources-test.yml", propertySource.getName());
        assertEquals("value1", propertySource.getProperty("test.key1"));
        assertEquals("value2", propertySource.getProperty("test.key2"));
    }

}
