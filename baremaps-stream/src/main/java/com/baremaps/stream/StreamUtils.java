package com.baremaps.stream;

import com.baremaps.stream.BufferedSpliterator.CompletionStrategy;
import com.baremaps.stream.BufferedSpliterator.InCompletionOrder;
import com.baremaps.stream.BufferedSpliterator.InSourceOrder;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility methods for creating buffered and bached streams.
 */
public class StreamUtils {

  /**
   * Create an ordered sequential stream from an iterator.
   * @param iterator
   * @param <T>
   * @return a ordered sequential stream.
   */
  public static <T> Stream<T> stream(Iterator<T> iterator) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
  }

  /**
   * Parallelize the provide stream and split it according to the batch size.
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
   * Buffer the completion of the provided asynchronous stream according to a completion strategy and a buffer size.
   *
   * @param asyncStream
   * @param completionStrategy
   * @param bufferSize
   * @param <T>
   * @return a buffered stream
   */
  private static <T> Stream<CompletableFuture<T>> buffer(
      Stream<CompletableFuture<T>> asyncStream,
      CompletionStrategy completionStrategy,
      int bufferSize) {
    return StreamSupport.stream(
        new BufferedSpliterator<>(asyncStream.spliterator(), bufferSize, completionStrategy),
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
  public static <T> Stream<CompletableFuture<T>> bufferInCompletionOrder(Stream<CompletableFuture<T>> asyncStream,
      int bufferSize) {
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
      Stream<CompletableFuture<T>> asyncStream,
      int bufferSize) {
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
  private static <T, U> Stream<U> buffer(
      Stream<T> stream,
      Function<T, U> asyncMapper,
      CompletionStrategy completionStrategy,
      int bufferSize) {
    Stream<CompletableFuture<U>> asyncStream = stream
        .map(t -> CompletableFuture.supplyAsync(() -> asyncMapper.apply(t)));
    return buffer(asyncStream, completionStrategy, bufferSize)
        .map(f -> {
          try {
            return f.get();
          } catch (InterruptedException | ExecutionException e) {
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
  public static <T, U> Stream<U> bufferInCompletionOrder(Stream<T> stream, Function<T, U> asyncMapper, int bufferSize) {
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
  public static <T, U> Stream<U> bufferInSourceOrder(Stream<T> stream, Function<T, U> asyncMapper, int bufferSize) {
    return buffer(stream, asyncMapper, InSourceOrder.INSTANCE, bufferSize);
  }

}
