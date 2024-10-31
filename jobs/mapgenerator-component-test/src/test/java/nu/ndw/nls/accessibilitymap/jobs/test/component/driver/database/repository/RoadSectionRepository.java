package nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.repository;

import nu.ndw.nls.accessibilitymap.jobs.test.component.driver.database.entity.RoadSection;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.repository.CrudRepository;

@Order(Ordered.HIGHEST_PRECEDENCE)
public interface RoadSectionRepository extends CrudRepository<RoadSection, Integer> {

}
