/**
 * Copyright (C) 2011
 *   Michael Mosmann <michael@mosmann.de>
 *   Martin Jöhren <m.joehren@googlemail.com>
 *
 * with contributions from
 * 	konstantin-ba@github,Archimedes Trajano	(trajano@github)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
