package nu.ndw.nls.accessibilitymap.backend.yaml.exception;

import java.util.List;

public class InvalidDataException extends RuntimeException {

	public InvalidDataException(final List<String> errors) {

		super(String.join(". ", errors));
	}
}
