package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtil {

    public static <T> Collection<List<T>> batch(Collection<T> collection, int batchSize) {

        AtomicInteger counter = new AtomicInteger();
        return collection.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / batchSize)).values();
    }
}
