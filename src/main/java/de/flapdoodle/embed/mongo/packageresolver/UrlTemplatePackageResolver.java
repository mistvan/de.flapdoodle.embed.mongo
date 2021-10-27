package de.flapdoodle.embed.mongo.packageresolver;

import de.flapdoodle.embed.process.config.store.DistributionPackage;
import de.flapdoodle.embed.process.config.store.FileSet;
import de.flapdoodle.embed.process.config.store.PackageResolver;
import de.flapdoodle.embed.process.distribution.ArchiveType;
import de.flapdoodle.embed.process.distribution.Distribution;
import org.immutables.value.Value;

import java.util.function.Function;

@Value.Immutable
abstract class UrlTemplatePackageResolver implements PackageResolver {

  protected abstract ArchiveType archiveType();
  protected abstract FileSet fileSet();
  protected abstract String urlTemplate();

  @Override
  public DistributionPackage packageFor(Distribution distribution) {
    String path=render(urlTemplate(), distribution);
    return DistributionPackage.of(archiveType(), fileSet(), path);
  }

  private static String render(String urlTemplate, Distribution distribution) {
    String version=distribution.version().asInDownloadPath();
    return urlTemplate.replace("{version}",version);
  }

  public static ImmutableUrlTemplatePackageResolver.Builder builder() {
    return ImmutableUrlTemplatePackageResolver.builder();
  }
}
