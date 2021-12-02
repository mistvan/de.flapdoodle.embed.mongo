package de.flapdoodle.embed.mongo.distribution;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumericVersionTest {

    private static final NumericVersion V3_2_6 = NumericVersion.of(3, 2, 26);
    private static final NumericVersion V_5_0_0 = NumericVersion.of(5, 0, 0);

    @Test
    public void older_or_equal_comparison_should_work() {
        assertTrue(V3_2_6.isOlderOrEqual(V_5_0_0));
    }

    @Test
    public void newer_or_equal_comparison_should_work() {
        assertTrue(V_5_0_0.isNewerOrEqual(V3_2_6));
    }
}