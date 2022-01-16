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
		HasOptionalResult<DistributionMatch, String> explainRules = ExplainRules.<DistributionMatch, PlatformMatch, String>mapIfInstance(PlatformMatch.class,
				platformMatch -> explainPlatformMatch(platformMatch))
			.or(mapIfInstance(DistributionMatch.AndThen.class, andThen -> ""+explainMatch(andThen.first())+" and "+explainMatch(andThen.second())))
			.or(mapIfInstance(DistributionMatch.Any.class, any -> any.matcher().stream().map(ExplainRules::explainMatch).collect(Collectors.joining(" or "))))
			.or(mapIfInstance(VersionRange.class, versionRange -> explainVersionRange(versionRange)));

		return explainRules.apply(match)
			.orElseGet(() -> match.getClass().getSimpleName());
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
		String indent=indent(level);
		System.out.print(indent);
		if (finder instanceof UrlTemplatePackageResolver) {
			System.out.println(" -> "+((UrlTemplatePackageResolver) finder).urlTemplate());
		} else {
			System.out.println(finder.getClass().getSimpleName());
		}

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

	interface HasOptionalResult<S, T> extends Function<S, Optional<T>> {

		default HasOptionalResult<S, T> or(HasOptionalResult<S, T> other) {
			HasOptionalResult<S, T> that = this;
			return s -> {
				Optional<T> first = that.apply(s);
				return first.isPresent() ? first : other.apply(s);
			};
		}
	}

	static <S,T extends S,V> HasOptionalResult<S, V> mapIfInstance(Class<T> type, Function<T, V> mapIfTypeMatches) {
		return s -> type.isInstance(s)
			? Optional.of(mapIfTypeMatches.apply(type.cast(s)))
			: Optional.empty();
	}
}
