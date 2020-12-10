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
package de.flapdoodle.embed.mongo.runtime;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.flapdoodle.embed.mongo.config.MongoDumpConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;

public class MongoDump extends AbstractMongo {

   public static List<String> getCommandLine(MongoDumpConfig config, ExtractedFileSet files)
      throws UnknownHostException {
      List<String> ret = new ArrayList<>();
      ret.addAll(Arrays.asList(files.executable().getAbsolutePath()));
      if (config.isVerbose()) {
         ret.add("-v");
      }
      applyNet(config.net(), ret);

      if (config.getDatabaseName().isPresent()) {
         ret.add("--db");
         ret.add(config.getDatabaseName().get());
      }
      if (config.getCollectionName().isPresent()) {
         ret.add("--collection");
         ret.add(config.getCollectionName().get());
      }

      if (config.getQuery().isPresent()) {
         ret.add("--query");
         ret.add(config.getQuery().get());
      }
      if (config.getQueryFile().isPresent()) {
         ret.add("--queryFile");
         ret.add(config.getQueryFile().get());
      }
      if (config.getReadPreference().isPresent()) {
         ret.add("--readPreference");
         ret.add(config.getReadPreference().get());
      }
      if (config.isForceTableScan()) {
         ret.add("--forceTableScan");
      }
      if (config.getArchive().isPresent()) {
         ret.add("--archive=" + config.getArchive().get());
      }
      if (config.isDumpDbUsersAndRoles()) {
         ret.add("--dumpDbUsersAndRoles");
      }
      if (config.getOut().isPresent()) {
         ret.add("--out");
         ret.add(config.getOut().get());
      }
      if (config.isGzip()) {
         ret.add("--gzip");
      }
      if (config.isRepair()) {
         ret.add("--repair");
      }
      if (config.isOplog()) {
         ret.add("--oplog");
      }
      if (config.getExcludeCollection().isPresent()) {
         ret.add("--excludeCollection");
         ret.add(config.getExcludeCollection().get());
      }
      if (config.getExcludeCollectionWithPrefix().isPresent()) {
         ret.add("--excludeCollectionWithPrefix");
         ret.add(config.getExcludeCollectionWithPrefix().get());
      }
      if (config.getNumberOfParallelCollections().isPresent()) {
         ret.add("--numParallelCollections");
         ret.add(""+config.getNumberOfParallelCollections().getAsInt());
      }

      return ret;
   }

   protected static void applyNet(Net net, List<String> ret) {
      ret.add("--port");
      ret.add("" + net.getPort());
      if (net.getBindIp()!=null) {
         ret.add("--host");
         ret.add(net.getBindIp());
      }
   }

}
