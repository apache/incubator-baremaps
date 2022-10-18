/*
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

package org.apache.baremaps.collection;



import java.io.IOException;

/**
 * A {@link Cleanable} is a data object that can be cleaned. The clean method is invoked to delete
 * resources that the object is using.
 */
public interface Cleanable {

  /** Cleans the underlying resources (files, memory, etc.) used by this object. */
  void clean() throws IOException;
}
