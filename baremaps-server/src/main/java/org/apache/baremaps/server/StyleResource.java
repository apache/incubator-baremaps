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

package org.apache.baremaps.server;

import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.ProducesJson;
import java.util.function.Supplier;
import org.apache.baremaps.vectortile.style.Style;

/**
 * A resource that provides access to the style.
 */
public class StyleResource {

  private final Supplier<Style> styleSupplier;

  public StyleResource(Supplier<Style> styleSupplier) {
    this.styleSupplier = styleSupplier;
  }

  @Get("/style.json")
  @ProducesJson
  public Style getStyle() {
    return styleSupplier.get();
  }
}
