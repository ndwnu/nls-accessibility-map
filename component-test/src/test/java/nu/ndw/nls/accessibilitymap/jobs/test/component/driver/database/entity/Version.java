package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "nwb")
public class Version {

    @Id
    private int versionId;

    private OffsetDateTime imported;

    private OffsetDateTime referenceDate;

    private String status;

    private OffsetDateTime revision;
}
