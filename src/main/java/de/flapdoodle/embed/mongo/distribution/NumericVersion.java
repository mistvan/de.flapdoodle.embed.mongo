package de.flapdoodle.embed.mongo.distribution;

import org.immutables.value.Value;

import static java.lang.Math.abs;

@Value.Immutable
public interface NumericVersion {
  @Value.Parameter
  int major();
  @Value.Parameter
  int minor();
  @Value.Parameter
  int patch();

  static NumericVersion of(int major, int minor, int patch) {
    return ImmutableNumericVersion.of(major,minor,patch);
  }

  static NumericVersion of(String versionString) {
    int major;
    int minor;
    int patch;
    
    if ("latest".equals(versionString)) {
      major = Integer.MAX_VALUE;
      minor = Integer.MAX_VALUE;
      patch = Integer.MAX_VALUE;
    } else {
      final String[] semverParts = versionString.split("\\.", 3);
      major = Integer.parseInt(semverParts[0], 10);
      minor = Integer.parseInt(semverParts[1], 10);
      String semverPart3 = semverParts[2];
      final int idxOfDash = semverPart3.indexOf('-');
      // cut any -RC/-M
      if (idxOfDash > 0) {
        semverPart3 = semverPart3.substring(0, idxOfDash);
      }
      patch = Integer.parseInt(semverPart3, 10);
    }

    return of(major,minor,patch);
  }

  default boolean isNewerOrEqual(int major, int minor, int patch) {
    return isNewerOrEqual(NumericVersion.of(major,minor,patch));
  }

  default boolean isNewerOrEqual(NumericVersion other) {
    if (major()<other.major()) return false;
    if (minor()<other.minor()) return false;
    if (patch()<other.patch()) return false;
    return true;
  }

  default boolean isOlderOrEqual(int major, int minor, int patch) {
    return isOlderOrEqual(NumericVersion.of(major,minor,patch));
  }

  default boolean isOlderOrEqual(NumericVersion other) {
    if (major()>other.major()) return false;
    if (minor()>other.minor()) return false;
    return patch() <= other.patch();
  }

  default boolean isNextOrPrevPatch(NumericVersion other) {
    if (major()!=other.major()) return false;
    if (minor()!=other.minor()) return false;
    return abs(patch() - other.patch()) == 1;
  }
}
