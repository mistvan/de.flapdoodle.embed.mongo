package de.flapdoodle.embed.mongo.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Arguments {

	public static class Builder {
		private final List<String> arguments = new ArrayList<>();

		public List<String> build() {
			return Collections.unmodifiableList(new ArrayList<>(arguments));
		}
		public Builder add(String... parts) {
			for (String part : parts) arguments.add(part);
			return this;
		}

		public Builder addIf(boolean condition, String ... parts) {
			if (condition) add(parts);
			return this;
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}
