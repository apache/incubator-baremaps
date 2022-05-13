/*
 * Copyright (C) 2020 The Baremaps Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.baremaps.workflow;

import com.baremaps.workflow.tasks.DownloadUrl;
import com.baremaps.workflow.tasks.UnzipFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class ObjectMapperTest {

  @Test
  public void test() throws IOException {
    var mapper = new ObjectMapper();

    var workflow1 =
        new Workflow(
            new DownloadUrl(
                "download", List.of(), "http://www.baremaps.com/download.zip", "download.zip"),
            new UnzipFile("unzip", List.of("download"), "download.zip", "download"));

    var json = mapper.writeValueAsString(workflow1);
    System.out.println(json);

    var workflow2 = mapper.readValue(json, Workflow.class);
    System.out.println(workflow2.tasks()[0].getClass().getSimpleName());
    System.out.println(workflow2.tasks()[1].getClass().getSimpleName());
  }
}