package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.job.configuration;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "nu.ndw.nls.springboot.test.component.driver.job")
@Validated
public class JobDriverConfiguration {

    @Valid
    private Map<String, JobConfiguration> jobConfigurations;

    public JobConfiguration getJobConfiguration(String jobName) {
        if (!jobConfigurations.containsKey(jobName)) {
            throw new IllegalArgumentException("Job configuration for job '" + jobName + "' does not exist. Please configure your job in nu.ndw.nls.springboot.test.component.driver.job.job-configurations");
        }

        return jobConfigurations.get(jobName);
    }
}
