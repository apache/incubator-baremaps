/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.baremaps.workflow.tasks;

import org.apache.baremaps.workflow.Task;
import org.apache.baremaps.workflow.WorkflowContext;
import org.wikidata.wdtk.datamodel.interfaces.*;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;

public record ImportWikidata(Object database) implements Task {

  @Override
  public void execute(WorkflowContext context) throws Exception {
    DumpProcessingController controller = new DumpProcessingController("wikidatawiki");
    controller.setOfflineMode(false);
    controller.registerEntityDocumentProcessor(new CustomProcessor(), null, true);
    controller.processMostRecentJsonDump();
  }

  public class CustomProcessor implements EntityDocumentDumpProcessor {

    @Override
    public void processItemDocument(ItemDocument itemDocument) {}

    @Override
    public void processPropertyDocument(PropertyDocument propertyDocument) {}

    @Override
    public void processLexemeDocument(LexemeDocument lexemeDocument) {}

    @Override
    public void processMediaInfoDocument(MediaInfoDocument mediaInfoDocument) {}

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }
  }
}
