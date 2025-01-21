package nu.ndw.nls.accessibilitymap.accessibility.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IntegerSequenceSupplierTest {

    @Test
    void next_ok() {
        IntegerSequenceSupplier integerSequenceSupplier = new IntegerSequenceSupplier();

        assertThat(integerSequenceSupplier.next()).isOne();
        assertThat(integerSequenceSupplier.next()).isEqualTo(2);
        assertThat(integerSequenceSupplier.next()).isEqualTo(3);
    }
}
