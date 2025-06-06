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

package org.apache.baremaps.postgres.openstreetmap;

/** Signals that an exception occurred in a {@code Repository}. */
public class RepositoryException extends Exception {

  /** Constructs a {@code RepositoryException} with {@code null} as its error detail message. */
  public RepositoryException() {}

  /**
   * Constructs an {@code RepositoryException} with the specified detail message.
   *
   * @param message the message
   */
  public RepositoryException(String message) {
    super(message);
  }

  /**
   * Constructs a {@code RepositoryException} with the specified cause.
   *
   * @param cause the cause
   */
  public RepositoryException(Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a {@code RepositoryException} with the specified detail message and cause.
   *
   * @param message the message
   * @param cause the cause
   */
  public RepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
