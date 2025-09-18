package nu.ndw.nls.accessibilitymap.backend.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.yaml.exception.InvalidDataException;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

@Slf4j
@SuppressWarnings("java:S1694")
public abstract class AbstractYamlRepository<T extends List<?>> {

    @Getter
    private final T data;

    protected AbstractYamlRepository(Environment environment, Class<? extends T> dataClass, String yamlFile) throws IOException {

        this(environment, dataClass, yamlFile, preValidateCallback -> {
        });
    }

    protected AbstractYamlRepository(
            Environment environment,
            Class<? extends T> dataClass,
            String yamlFile,
            Consumer<T> preValidateCallback) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();

        log.debug("{} started Loading data from.", this.getClass().getSimpleName());
        String yamlFileContent = getYamlFile(yamlFile, environment);
        if (StringUtils.hasText(yamlFileContent)) {
            data = objectMapper.readValue(
                    yamlFileContent,
                    dataClass);

            preValidateCallback.accept(data);
            validateData();
            log.debug("{} data loaded successfully with {} items.", this.getClass().getSimpleName(), data.size());
        } else {
            throw new InvalidDataException(List.of("No data is available for `%s` because it is empty".formatted(yamlFile)));
        }
    }

    private void validateData() {

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            List<String> errors = IntStream.range(0, data.size())
                    .mapToObj(index -> getErrorMessage(validator, data, index))
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .toList();

            if (!errors.isEmpty()) {
                throw new InvalidDataException(errors);
            } else {
                log.debug("{} data validated.", this.getClass().getSimpleName());
            }
        }
    }

    private String getErrorMessage(Validator validator, T object, int index) {

        Set<? extends ConstraintViolation<?>> validationErrors = validator.validate(object.get(index));
        if (!validationErrors.isEmpty()) {
            return "%s data record nr %s is invalid because: %s".formatted(
                    this.getClass().getSimpleName(),
                    index,
                    validationErrors.stream()
                            .map(constraintViolation -> String.format("%s: %s", constraintViolation.getPropertyPath(),
                                    constraintViolation.getMessage()))
                            .toList());
        }
        return null;
    }

    private String getYamlFile(String fileName, Environment environment) throws FileNotFoundException {

        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        Collections.reverse(activeProfiles);

        Optional<String> fileContent;
        for (String activeProfile : activeProfiles) {
            fileContent = readFile("%s-%s".formatted(fileName, activeProfile));
            if (fileContent.isPresent()) {
                return fileContent.get();
            }
        }

        fileContent = readFile(fileName);
        if (fileContent.isPresent()) {
            return fileContent.get();
        }

        throw new FileNotFoundException("Could not load data file for %s".formatted(this.getClass().getSimpleName()));
    }

    private Optional<String> readFile(String fileName) {

        try (InputStream inputStream = ResourceUtils.getURL("classpath:data/%s.yml".formatted(fileName)).openStream()) {
            String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Successfully loaded data from file: '{}'.", fileName);

            return Optional.of(fileContent);
        } catch (IOException exception) {
            log.debug("Failed to load data from file: '{}'.", fileName, exception);
            return Optional.empty();
        }
    }
}
