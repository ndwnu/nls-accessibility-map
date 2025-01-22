package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "nwb")
public class RoadSection {

    @EmbeddedId
    private nwbRoadSectionPrimaryKey primaryKey;

    private long junctionIdFrom;

	private long junctionIdTo;

    private String roadOperatorType;

	private LineString geometry;
}
