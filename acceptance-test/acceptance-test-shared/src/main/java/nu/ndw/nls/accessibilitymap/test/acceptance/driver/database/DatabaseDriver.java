package nu.ndw.nls.accessibilitymap.test.acceptance.driver.database;

import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.springboot.test.component.state.StateManagement;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseDriver implements StateManagement {

    private final DataSource dataSource;

    private final List<CrudRepository<?, ?>> repositories;

    @Override
    public void clearState() {

        repositories.forEach(CrudRepository::deleteAll);
    }
}
