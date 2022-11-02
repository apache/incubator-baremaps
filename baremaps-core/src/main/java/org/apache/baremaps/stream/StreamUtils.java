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

package org.apache.baremaps.stream;



import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.baremaps.stream.BufferedSpliterator.CompletionOrder;
import org.apache.baremaps.stream.BufferedSpliterator.InCompletionOrder;
import org.apache.baremaps.stream.BufferedSpliterator.InSourceOrder;

/** Utility methods for creating parallel, buffered and batched streams of unknown size. */
public class StreamUtils {

  /**
   * Create an ordered sequential stream from an iterator of unknown size.
   *
   * @param iterator
   * @param <T>
   * @return a ordered sequential stream.
   */
  public static <T> Stream<T> stream(Iterator<T> iterator) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
        false);
  }

  /**
   * Parallelize the provided stream of unknown size.
   *
   * @param stream
   * @param <T>
   * @return a parallel stream
   */
  public static <T> Stream<T> batch(Stream<T> stream) {
    return batch(stream, 1);
  }

  /**
   * Parallelize the provided stream of unknown size and split it according to the batch size.
   *
   * @param stream
   * @param batchSize
   * @param <T>
   * @return a parallel stream
   */
  public static <T> Stream<T> batch(Stream<T> stream, int batchSize) {
    return StreamSupport.stream(new BatchedSpliterator<T>(stream.spliterator(), batchSize), true);
  }

  /**
   * Buffer the completion of the provided asynchronous stream according to a completion strategy
   * and a buffer size.
   *
   * @param asyncStream
   * @param completionOrder
   * @param <T>
   * @return a buffered stream
   */
  private static <T> Stream<CompletableFuture<T>> buffer(Stream<CompletableFuture<T>> asyncStream,
      CompletionOrder completionOrder) {
    return buffer(asyncStream, completionOrder, Runtime.getRuntime().availableProcessors());
  }

  /**
   * Buffer the completion of the provided asynchronous stream according to a completion strategy
   * and a buffer size.
   *
   * @param asyncStream
   * @param completionOrder
   * @param bufferSize
   * @param <T>
   * @return a buffered stream
   */
  private static <T> Stream<CompletableFuture<T>> buffer(Stream<CompletableFuture<T>> asyncStream,
      CompletionOrder completionOrder, int bufferSize) {
    return StreamSupport.stream(
        new BufferedSpliterator<>(asyncStream.spliterator(), bufferSize, completionOrder),
        asyncStream.isParallel());
  }

  /**
   * Buffer the completion of the provided asynchronous stream according to a buffer size.
   *
   * @param asyncStream
   * @param bufferSize
   * @param <T>
   * @return a buffered stream
   */
  public static <T> Stream<CompletableFuture<T>> bufferInCompletionOrder(
      Stream<CompletableFuture<T>> asyncStream, int bufferSize) {
    return buffer(asyncStream, InCompletionOrder.INSTANCE, bufferSize);
  }

  /**
   * Buffer the completion of the provided asynchronous stream according to a buffer size.
   *
   * @param asyncStream
   * @param bufferSize
   * @param <T>
   * @return a buffered stream
   */
  public static <T> Stream<CompletableFuture<T>> bufferInSourceOrder(
      Stream<CompletableFuture<T>> asyncStream, int bufferSize) {
    return buffer(asyncStream, InSourceOrder.INSTANCE, bufferSize);
  }

  /**
   * Buffer the asynchronous mapping of the provided stream according to a buffer size.
   *
   * @param stream
   * @param asyncMapper
   * @param bufferSize
   * @param <T>
   * @return a buffered stream
   */
  private static <T, U> Stream<U> buffer(Stream<T> stream, Function<T, U> asyncMapper,
      CompletionOrder completionOrder, int bufferSize) {
    Stream<CompletableFuture<U>> asyncStream =
        stream.map(t -> CompletableFuture.supplyAsync(() -> asyncMapper.apply(t)));
    return buffer(asyncStream, completionOrder, bufferSize).map(f -> {
      try {
        return f.get();
      } catch (InterruptedException | ExecutionException e) {
        Thread.currentThread().interrupt();
        throw new StreamException(e);
      }
    });
  }

  /**
   * Buffer the asynchronous mapping of the provided stream according to a buffer size.
   *
   * @param stream
   * @param asyncMapper
   * @param bufferSize
   * @param <T>
   * @return a buffered stream
   */
  public static <T, U> Stream<U> bufferInCompletionOrder(Stream<T> stream,
      Function<T, U> asyncMapper, int bufferSize) {
    return buffer(stream, asyncMapper, InCompletionOrder.INSTANCE, bufferSize);
  }

  /**
   * Buffer the asynchronous mapping of the provided stream according to a buffer size.
   *
   * @param stream
   * @param asyncMapper
   * @param bufferSize
   * @param <T>
   * @return a buffered stream
   */
  public static <T, U> Stream<U> bufferInSourceOrder(Stream<T> stream, Function<T, U> asyncMapper,
      int bufferSize) {
    return buffer(stream, asyncMapper, InSourceOrder.INSTANCE, bufferSize);
  }

  /** Partition the provided stream according to a partition size. */
  public static <T> Stream<Stream<T>> partition(Stream<T> stream, int partitionSize) {
    return StreamSupport.stream(new PartitionedSpliterator<T>(stream.spliterator(), partitionSize),
        stream.isParallel());
  }
}
