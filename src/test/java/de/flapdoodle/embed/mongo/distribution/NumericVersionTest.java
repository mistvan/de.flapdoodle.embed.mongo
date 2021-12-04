package de.flapdoodle.embed.mongo.distribution;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NumericVersionTest {

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3|1.2.3",
		"1.1.3|1.1.3",
		"2.0.3|2.0.3",
	})
	public void isEqual(String sample) {
		String[] parts = sample.split("\\|");
		NumericVersion left = NumericVersion.of(parts[0]);
		NumericVersion right = NumericVersion.of(parts[1]);
		assertThat(left.isEqual(right))
			.describedAs("%s == %s", left, right)
			.isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3|1.2.4",
		"1.0.3|1.1.0",
		"1.1.1|2.1.1",
	})
	public void isOlder(String sample) {
		String[] parts = sample.split("\\|");
		NumericVersion left = NumericVersion.of(parts[0]);
		NumericVersion right = NumericVersion.of(parts[1]);
		assertThat(left.isOlder(right))
			.describedAs("%s <= %s", left, right)
			.isTrue();
		assertThat(left.isEqual(right))
			.describedAs("%s != %s", left, right)
			.isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3|1.2.3",
		"1.2.4|1.2.3",
		"1.1.0|1.0.3",
		"2.1.1|1.1.1",
	})
	public void isNotOlder(String sample) {
		String[] parts = sample.split("\\|");
		NumericVersion left = NumericVersion.of(parts[0]);
		NumericVersion right = NumericVersion.of(parts[1]);
		assertThat(left.isOlder(right))
			.describedAs("!%s <= %s", left, right)
			.isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3|1.2.3",
		"1.2.3|1.2.4",
		"1.0.3|1.1.0",
		"1.1.1|2.1.1",
	})
	public void isOlderOrEqual(String sample) {
		String[] parts = sample.split("\\|");
		NumericVersion left = NumericVersion.of(parts[0]);
		NumericVersion right = NumericVersion.of(parts[1]);
		assertThat(left.isOlderOrEqual(right))
			.describedAs("%s <= %s", left, right)
			.isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.4|1.2.3",
		"1.1.0|1.0.3",
		"2.1.1|1.1.1",
	})
	public void isNewer(String sample) {
		String[] parts = sample.split("\\|");
		NumericVersion left = NumericVersion.of(parts[0]);
		NumericVersion right = NumericVersion.of(parts[1]);
		assertThat(left.isNewer(right))
			.describedAs("%s >= %s", left, right)
			.isTrue();
		assertThat(left.isEqual(right))
			.describedAs("%s != %s", left, right)
			.isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3|1.2.3",
		"1.2.3|1.2.4",
		"1.0.3|1.1.0",
		"1.1.1|2.1.1",
	})
	public void isNotNewer(String sample) {
		String[] parts = sample.split("\\|");
		NumericVersion left = NumericVersion.of(parts[0]);
		NumericVersion right = NumericVersion.of(parts[1]);
		assertThat(left.isNewer(right))
			.describedAs("!%s >= %s", left, right)
			.isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"1.2.3|1.2.3",
		"1.2.4|1.2.3",
		"1.1.0|1.0.3",
		"2.1.1|1.1.1",
	})
	public void isNewerOrEqual(String sample) {
		String[] parts = sample.split("\\|");
		NumericVersion left = NumericVersion.of(parts[0]);
		NumericVersion right = NumericVersion.of(parts[1]);
		assertThat(left.isNewerOrEqual(right))
			.describedAs("%s >= %s", left, right)
			.isTrue();
	}

}