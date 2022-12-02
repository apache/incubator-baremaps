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

package org.apache.baremaps.openstreetmap.utils;



import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * A utility class for peeking progress when processing a {@code Stream}.
 *
 * @param <T>
 */
public class StreamProgress<T> implements Consumer<T> {

  private final AtomicLong position = new AtomicLong(0);

  private final Consumer<Long> listener;

  /**
   * Constructs a {@code StreamProgress} that periodically logs progress.
   *
   * @param size the size of the stream
   * @param tick the tick in milliseconds at with progress is logged
   */
  public StreamProgress(Long size, Integer tick) {
    this(new ProgressLogger(size, tick));
  }

  /**
   * Constructs a {@code StreamProgress}.
   *
   * @param listener the progress listener
   */
  public StreamProgress(Consumer<Long> listener) {
    this.listener = listener;
  }

  /**
   * Accepts stream element and increments progress.
   *
   * @param e the element
   */
  @Override
  public void accept(T e) {
    listener.accept(position.incrementAndGet());
  }
}
