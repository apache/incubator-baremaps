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

package org.apache.baremaps.data.schema;

import java.util.List;

/**
 * A {@link DataSchema} is a description of the structure of a row in a {@link DataFrame}.
 */
public interface DataSchema {

  /**
   * Returns the name of the schema.
   * 
   * @return the name of the schema
   */
  String name();

  /**
   * Returns the columns of the schema.
   * 
   * @return the columns of the schema
   */
  List<DataColumn> columns();

  /**
   * Creates a new row of the schema.
   * 
   * @return a new row of the schema
   */
  DataRow createRow();

}
