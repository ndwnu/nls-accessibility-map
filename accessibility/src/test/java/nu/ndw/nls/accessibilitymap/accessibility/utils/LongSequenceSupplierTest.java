package nu.ndw.nls.accessibilitymap.accessibility.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LongSequenceSupplierTest {

    @Test
    void next_ok() {
        LongSequenceSupplier longSequenceSupplier = new LongSequenceSupplier();

        assertThat(longSequenceSupplier.next()).isOne();
        assertThat(longSequenceSupplier.next()).isEqualTo(2);
        assertThat(longSequenceSupplier.next()).isEqualTo(3);
    }
}
