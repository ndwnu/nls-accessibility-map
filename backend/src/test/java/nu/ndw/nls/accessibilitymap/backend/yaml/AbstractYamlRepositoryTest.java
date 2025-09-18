package nu.ndw.nls.accessibilitymap.backend.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import nu.ndw.nls.accessibilitymap.backend.yaml.exception.InvalidDataException;
import nu.ndw.nls.accessibilitymap.backend.yaml.test.Data;
import nu.ndw.nls.accessibilitymap.backend.yaml.test.DataCollection;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class AbstractYamlRepositoryTest {

    @Mock
    private Environment environment;

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @BeforeEach
    void setUpLocale() {

        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    void constructor() throws IOException {

        when(environment.getActiveProfiles()).thenReturn(new String[]{"profile3"});

        AbstractYamlRepository<DataCollection> abstractYamlRepository = new AbstractYamlRepository<>(
                environment,
                DataCollection.class,
                "AbstractYamlRepositoryTest-valid") {
        };

        assertThat(abstractYamlRepository.getData()).isEqualTo(List.of(
                Data.builder().value("value1").build(),
                Data.builder().value("value2").build()
        ));
    }

    @Test
    void constructor_invalidData() {

        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        Throwable exception = catchThrowable(
                () -> new AbstractYamlRepository<>(environment, DataCollection.class, "AbstractYamlRepositoryTest-invalid") {
                });

        assertThat(exception).isInstanceOf(InvalidDataException.class);
        assertThat(exception.getMessage()).isEqualTo(
                "data record nr 0 is invalid because: [value: length must be between 5 and 2147483647], data record nr 1 is invalid because: [value: length must be between 5 and 2147483647]");
    }

    @Test
    void constructor_emptyFile() {

        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        InvalidDataException exception = assertThrows(InvalidDataException.class,
                () -> new AbstractYamlRepository<>(
                        environment,
                        DataCollection.class,
                        "AbstractYamlRepositoryTest-empty") {
                });

        assertThat(exception.getMessage()).isEqualTo("No data is available for `AbstractYamlRepositoryTest-empty` because it is empty");
    }

    @Test
    void constructor_loadingPreferredProfile() throws IOException {

        when(environment.getActiveProfiles()).thenReturn(new String[]{"profile2", "profile1"});

        AbstractYamlRepository<DataCollection> abstractYamlRepository = new AbstractYamlRepository<>(environment, DataCollection.class,
                "AbstractYamlRepositoryTest-valid") {
        };

        assertThat(abstractYamlRepository.getData()).isEqualTo(List.of(
                Data.builder().value("profile1-value1").build(),
                Data.builder().value("profile1-value2").build()
        ));
    }

    @Test
    void constructor_noDataFile() {

        when(environment.getActiveProfiles()).thenReturn(new String[]{});

        assertThrows(FileNotFoundException.class,
                () -> new AbstractYamlRepository<>(environment, DataCollection.class, "not-existing-file") {
                });
    }
}