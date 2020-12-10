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

import de.flapdoodle.embed.mongo.config.MongoRestoreConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.process.extract.ExtractedFileSet;

public class MongoRestore extends AbstractMongo {

   public static List<String> getCommandLine(MongoRestoreConfig config, ExtractedFileSet files)
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
      if (config.isObjectCheck()) {
         ret.add("--objCheck");
      }
      if (config.isOplogReplay()) {
         ret.add("--oplogReplay");
      }
      if (config.getOplogLimit().isPresent()) {
         ret.add("--oplogLimit");
         ret.add(""+config.getOplogLimit().getAsLong());
      }
      if (config.getArchive().isPresent()) {
         ret.add(String.format("--archive=%s", config.getArchive().get()));
      }
      if (config.isRestoreDbUsersAndRoles()) {
         ret.add("--restoreDbUsersAndRoles");
      }
      if (config.getDir().isPresent()) {
         ret.add("--dir");
         ret.add(config.getDir().get());
      }
      if (config.isGzip()) {
         ret.add("--gzip");
      }
      if (config.isDropCollection()) {
         ret.add("--drop");
      }
      if (config.getWriteConcern().isPresent()) {
         ret.add("--writeConcern");
         ret.add(config.getWriteConcern().get());
      }
      if (config.isNoIndexRestore()) {
         ret.add("--noIndexRestore");
      }
      if (config.isNoOptionsRestore()) {
         ret.add("--noOptionsRestore");
      }
      if (config.isKeepIndexVersion()) {
         ret.add("--keepIndexVersion");
      }
      if (config.isMaintainInsertionOrder()) {
         ret.add("--maintainInsertionOrder");
      }
      if (config.getNumberOfParallelCollections().isPresent()) {
         ret.add("--numParallelCollections");
         ret.add(""+config.getNumberOfParallelCollections().getAsInt());
      }
      if (config.getNumberOfInsertionWorkersPerCollection().isPresent()) {
         ret.add("--numInsertionWorkersPerCollection");
         ret.add(""+config.getNumberOfInsertionWorkersPerCollection().getAsInt());
      }
      if (config.isStopOnError()) {
         ret.add("--stopOnError");
      }
      if (config.isBypassDocumentValidation()) {
         ret.add("--bypassDocumentValidation");
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
