package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.mongo.distribution.NumericVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ExplainRules {
	private ExplainRules() {
		// no instance
	}

	public static void explain(PlatformMatchRules rules) {
		explain(0, rules);
	}

	static void explain(int level, PlatformMatchRules rules) {
		String indent=indent(level);
		rules.rules().forEach(rule -> {
			String explained = explainMatch(rule.match());
			System.out.print(indent);
			System.out.println("matching "+explained);
			explainFinder(level, rule.finder());
		});
	}

	static String explainMatch(DistributionMatch match) {
		return forType(DistributionMatch.class)
			.mapIfInstance(PlatformMatch.class, ExplainRules::explainPlatformMatch)
			.orMapIfInstance(DistributionMatch.AndThen.class, andThen -> "" + explainMatch(andThen.first()) + " and " + explainMatch(andThen.second()))
			.orMapIfInstance(DistributionMatch.Any.class, any -> any.matcher().stream().map(ExplainRules::explainMatch).collect(Collectors.joining(" or ")))
			.orMapIfInstance(VersionRange.class, ExplainRules::explainVersionRange)
			.orMapIfInstance(DistributionMatch.class, it -> it.getClass().getSimpleName())
			.apply(match)
			.get();
	}

	private static String explainPlatformMatch(PlatformMatch match) {
		List<String> parts=new ArrayList<>();
		match.os().ifPresent(os -> parts.add("os="+os));
		match.bitSize().ifPresent(bitSize -> parts.add("bitSize="+bitSize));

		if (!match.version().isEmpty()) {
			parts.add(match.version().stream().map(version -> ""+version)
				.collect(Collectors.joining(",", "(version is any of", ")")));
		}

		return !parts.isEmpty()
			? parts.stream().collect(Collectors.joining(" and ", "(", ")"))
			: "(any)";
	}

	private static String explainVersionRange(VersionRange versionRange) {
		return asHumanReadable(versionRange.min())+"-"+asHumanReadable(versionRange.max());
	}

	private static String asHumanReadable(NumericVersion version) {
		return version.major()+"."+version.minor()+"."+version.patch();
	}

	private static void explainFinder(int level, PackageFinder finder) {

		String finderExplained = forType(PackageFinder.class)
			.mapIfInstance(UrlTemplatePackageResolver.class, it -> "-> "+it.urlTemplate())
			.orMapIfInstance(PackageFinder.class, it -> it.getClass().getSimpleName())
			.apply(finder)
			.get();

		String indent=indent(level);
		System.out.print(indent);
		System.out.println(finderExplained);

		if (finder instanceof HasPlatformMatchRules) {
			explain(level+1, ((HasPlatformMatchRules) finder).rules());
		} else {
		}
	}

	private static String indent(int level) {
		return repeat(' ', level * 2);
	}

	private static String repeat(char c, int level) {
		char[] s = new char[level];
		for (int i = 0; i < level; i++) {
			s[i] = c;
		}
		return String.valueOf(s);
	}

	public static <S> HasOptionalBuilder<S> forType(Class<S> sourceType) {
		return new HasOptionalBuilder<>();
	}

	static class HasOptionalBuilder<S> {
		<T extends S,V> HasOptionalResult<S, V> mapIfInstance(Class<T> type, Function<T, V> mapIfTypeMatches) {
			return ExplainRules.mapIfInstance(type, mapIfTypeMatches);
		}
	}

	interface HasOptionalResult<S, V> extends Function<S, Optional<V>> {

		default HasOptionalResult<S, V> or(HasOptionalResult<S, V> other) {
			HasOptionalResult<S, V> that = this;
			return s -> {
				Optional<V> first = that.apply(s);
				return first.isPresent() ? first : other.apply(s);
			};
		}

		default <T extends S> HasOptionalResult<S, V> orMapIfInstance(Class<T> type, Function<T, V> mapIfTypeMatches) {
			return or(ExplainRules.mapIfInstance(type, mapIfTypeMatches));
		}
	}

	static <S,T extends S,V> HasOptionalResult<S, V> mapIfInstance(Class<T> type, Function<T, V> mapIfTypeMatches) {
		return s -> type.isInstance(s)
			? Optional.of(mapIfTypeMatches.apply(type.cast(s)))
			: Optional.empty();
	}
}
