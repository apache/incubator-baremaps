package com.baremaps.server;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.sse.ServerSentEvent;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Produces;
import com.linecorp.armeria.server.annotation.ProducesEventStream;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.linecorp.armeria.server.annotation.Put;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StyleService {

  private static Logger logger = LoggerFactory.getLogger(StyleService.class);

  private final Path style;

  private final ChangePublisher publisher;

  public StyleService(Path style) {
    this.style = style;
    this.publisher = new ChangePublisher(style);
  }

  @Get("/styles")
  @ProducesJson
  public List<String> listStyles() throws IOException {
    String json = Files.readString(style);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readValue(json, JsonNode.class);
    String id = node.get("id").asText();
    return Arrays.asList(id);
  }

  @Get("/styles/{style}")
  @Produces("application/json; charset=utf-8")
  public HttpResponse getStyle() throws IOException {
    return HttpResponse.of(Files.readString(style));
  }

  @Put("/styles/{style}")
  public void putStyle(JsonNode json) throws IOException {
    Files.writeString(style, json.toPrettyString());
  }

  @Get("/styles/{style}/changes")
  @ProducesEventStream
  public Publisher<ServerSentEvent> sseChanges(ServiceRequestContext ctx) {
    ctx.clearRequestTimeout();
    return publisher;
  }

  private class ChangePublisher implements Publisher<ServerSentEvent> {

    private final List<Subscriber<? super ServerSentEvent>> subscribers = new CopyOnWriteArrayList<>();

    public ChangePublisher(Path style) {
      new Thread(() -> {
        try {
          WatchService watchService = FileSystems.getDefault().newWatchService();
          style.getParent().register(watchService, ENTRY_MODIFY);
          WatchKey key;
          while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
              for (Subscriber<? super ServerSentEvent> subscriber : subscribers) {
                String json = Files.readString(style);
                ObjectMapper mapper = new ObjectMapper();
                Object object = mapper.readValue(json, Object.class);
                String data = mapper.writeValueAsString(object);
                subscriber.onNext(ServerSentEvent.ofData(data));
              }
            }
            key.reset();
          }
        } catch (InterruptedException | IOException e) {
          logger.error("Unable to monitor style changes", e);
        }
      }).start();
    }

    @Override
    public void subscribe(Subscriber<? super ServerSentEvent> subscriber) {
      subscribers.add(subscriber);
    }

  }
}
