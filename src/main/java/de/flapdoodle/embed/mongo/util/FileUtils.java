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
package de.flapdoodle.embed.mongo.util;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Some common functions for working with java.nio.file.*.
 */
public class FileUtils {

    /**
     * Copies a directory recursively.
     *
     * @param source The source directory.
     * @param target The target directory.
     */
    public static void copyDirectory(final Path source, final Path target) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        if (!Files.isDirectory(source)) {
            throw new IllegalArgumentException("Source must be a directory");
        }

        if (!Files.isDirectory(target)) {
            throw new IllegalArgumentException("Target must be a directory");
        }

        Files.walkFileTree(source, new DirectoryCopier(source, target));
    }

    private static class DirectoryCopier extends SimpleFileVisitor<Path> {
        private final Path source;
        private final Path target;

        public DirectoryCopier(final Path source, final Path target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            final Path targetFile = target.resolve(source.relativize(file));

            final Path targetDir = targetFile.getParent();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            final CopyOption copyOptions[];
            if (Files.exists(targetFile)) {
                copyOptions = new CopyOption[]{ StandardCopyOption.REPLACE_EXISTING };
            } else {
                copyOptions = new CopyOption[0];
            }

            Files.copy(file, targetFile, copyOptions);

            return CONTINUE;
        }
    }
}
