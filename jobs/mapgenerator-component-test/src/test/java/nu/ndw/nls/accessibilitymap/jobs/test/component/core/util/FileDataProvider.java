package nu.ndw.nls.accessibilitymap.jobs.test.component.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Slf4j
@Component
public class FileDataProvider {

    @SuppressWarnings("java:S3658")
    public void writeDataToFile(final File file, final String data) {

        try {
            FileUtils.writeStringToFile(file, data, StandardCharsets.UTF_8.toString());

        } catch (final Exception exception) {
            log.error("Failed to write file.", exception);
            fail(exception.getMessage());
        }
    }

    @SuppressWarnings("java:S3658")
    public String readDataFromFile(final String folder, final String file, final String extension) {

        try {
            return FileUtils.readFileToString(
                    new File("./%s/%s.%s".formatted(folder, file, extension)),
                    StandardCharsets.UTF_8.toString());

        } catch (final Exception exception) {
            log.error("Failed to load file.", exception);
            fail(exception.getMessage());
            return null;
        }
    }

    @SuppressWarnings("java:S3658")
    public String readTestDataFromFile(final String folder, final String file, final String extension) {

        try {
            return FileUtils.readFileToString(
                    ResourceUtils.getFile("classpath:data/%s/%s.%s".formatted(folder, file, extension)),
                    StandardCharsets.UTF_8.toString());

        } catch (final Exception e) {
            log.error("Failed to load file.", e);
            fail(e.getMessage());
            return null;
        }
    }
}
