package nu.ndw.nls.accessibilitymap.backend.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.backend.yaml.exception.InvalidDataClassException;
import nu.ndw.nls.accessibilitymap.backend.yaml.exception.InvalidDataException;
import org.springframework.core.env.Environment;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

@Slf4j
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

        var objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();

        log.debug("{} started Loading data.", this.getClass().getSimpleName());
        var yamlFileContent = getYamlFile(yamlFile, environment);
        if (StringUtils.hasText(yamlFileContent)) {
            data = objectMapper.readValue(
                    yamlFileContent,
                    dataClass);

            preValidateCallback.accept(data);
            validateData();
        } else {
            log.debug("No data available.");
            try {
                data = dataClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.error("Couldn't create instance of class {}", dataClass.getSimpleName(), e);
                throw new InvalidDataClassException(String.format("Could not create instance of class %s.", dataClass.getSimpleName()));
            }
        }
        log.debug("{} data loaded successfully with {} items.", this.getClass().getSimpleName(), data.size());
    }

    private void validateData() {

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();

            var errors = IntStream.range(0, data.size())
                    .mapToObj(index -> getErrorMessage(validator, data, index))
                    .filter(Objects::nonNull)
                    .toList();

            if (!errors.isEmpty()) {
                throw new InvalidDataException(errors);
            } else {
                log.debug("{} data validated.", this.getClass().getSimpleName());
            }
        }
    }

    private String getErrorMessage(Validator validator, T object, int index) {

        var validationErrors = validator.validate(object.get(index));
        if (!validationErrors.isEmpty()) {
            return String.format("%s data record nr %s is invalid because: %s",
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

        var activeProfiles = Arrays.asList(environment.getActiveProfiles());
        Collections.reverse(activeProfiles);

        for (String activeProfile : activeProfiles) {
            var file = readFile(String.format("%s-%s", fileName, activeProfile));
            if (file.isPresent()) {
                return file.get();
            }
        }

        var file = readFile(fileName);
        if (file.isPresent()) {
            return file.get();
        }

        throw new FileNotFoundException(String.format("Could not load data file for %s", this.getClass().getSimpleName()));
    }

    private Optional<String> readFile(String fileName) {

        try (var inputStream = ResourceUtils.getURL(String.format("classpath:data/%s.yml", fileName)).openStream()) {
            var yaml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.debug("Successfully loaded data from file: '{}'.", fileName);
            return Optional.of(yaml);
        } catch (IOException e) {
            log.debug("Failed to load data from file: '{}'.", fileName, e);
            return Optional.empty();
        }
    }
}
