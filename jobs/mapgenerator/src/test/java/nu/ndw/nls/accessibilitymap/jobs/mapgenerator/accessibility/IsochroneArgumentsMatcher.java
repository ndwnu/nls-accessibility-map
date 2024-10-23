package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.model.IsochroneArguments;
import org.mockito.ArgumentMatcher;

@RequiredArgsConstructor
public class IsochroneArgumentsMatcher implements ArgumentMatcher<IsochroneArguments> {

	private final IsochroneArguments left;

	@Override
	public boolean matches(final IsochroneArguments right) {

		if(right == null) {
			return false;
		}

		return left.weighting().equals(right.weighting())
				&& left.startPoint().equals(right.startPoint());
	}
}
