package nu.ndw.nls.accessibilitymap.jobs.test.component.glue;

import io.cucumber.java.en.Given;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.dto.VersionStatus;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.db.nwb.jooq.repositories.JooqNwbVersionCrudRepository;

@RequiredArgsConstructor
public class NwbVersionStepDefinitions {

    private final JooqNwbVersionCrudRepository jooqNwbVersionCrudRepository;

    @Given("an nwb version {word}")
    public void anNwbVersion(String version) {
        int versionId = Integer.parseInt(version.replace("-", ""));

        jooqNwbVersionCrudRepository.insert(NwbVersionDto.builder()
                .revision(Instant.now())
                .versionId(versionId)
                .status(VersionStatus.OK)
                .referenceDate(LocalDate.parse(version))
                .imported(Instant.now())
                .build());
    }
}
