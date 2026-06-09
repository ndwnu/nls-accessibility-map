package nu.ndw.nls.accessibilitymap.accessibility.cache.active;

import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ActiveVersionRepository {

    private static final String NAME_COLUMN_NAME = "name";

    private static final String VERSION_COLUMN_NAME = "version";

    private static final String ACTIVE_VERSION_TABLE_NAME = "active_version";

    private static final String ACCESSIBILITY_MAP_SCHEMA = "accessibility_map";

    private final DSLContext dsl;

    private final Table<?> activeVersionTable;

    private final Field<String> nameField;

    private final Field<String> versionField;

    public ActiveVersionRepository(DSLContext dsl) {
        this.dsl = dsl;
        var schema = DSL.schema(ACCESSIBILITY_MAP_SCHEMA);
        this.activeVersionTable = DSL.table(DSL.name(schema.getName(), ACTIVE_VERSION_TABLE_NAME));
        this.nameField = DSL.field(DSL.name(NAME_COLUMN_NAME), String.class);
        this.versionField = DSL.field(DSL.name(VERSION_COLUMN_NAME), String.class);
    }

    @Transactional
    public Optional<String> findActiveVersion(String cacheName) {

        return Optional.ofNullable(dsl.select(versionField).from(activeVersionTable)
                .where(nameField.eq(cacheName))
                .fetchOne(versionField));
    }

    @Transactional
    public void switchActiveVersion(String cacheName, String activeVersion) {
        dsl.insertInto(activeVersionTable)
                .columns(nameField, versionField)
                .values(cacheName, activeVersion)
                .onConflict(nameField)
                .doUpdate()
                .set(versionField, activeVersion)
                .execute();
    }
}
