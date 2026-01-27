package nu.ndw.nls.accessibilitymap.accessibility.core.dto.restriction;

import java.io.Serial;
import java.util.Collection;
import java.util.HashSet;
import org.jspecify.annotations.NonNull;

public class Restrictions extends HashSet<Restriction> {

    @Serial
    private static final long serialVersionUID = 1;

    public Restrictions() {
        super();
    }

    public Restrictions(@NonNull Collection<? extends Restriction> c) {
        super(c);
    }
}
