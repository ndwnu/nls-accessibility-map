package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.utils;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class SynchronizedListAppender<E> extends ch.qos.logback.core.read.ListAppender<E> {

	public SynchronizedListAppender() {

		list = new CopyOnWriteArrayList<>();
	}

	public Stream<E> stream() {

		return list.stream();
	}

	public void clear() {

		list.clear();
	}
}

