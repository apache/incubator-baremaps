package com.baremaps.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.sse.ServerSentEvent;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Produces;
import com.linecorp.armeria.server.annotation.ProducesEventStream;
import com.linecorp.armeria.server.annotation.Put;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Sinks;

public class StyleService {

  private static Logger logger = LoggerFactory.getLogger(StyleService.class);

  private final Sinks.Many sink = Sinks.many().multicast().onBackpressureBuffer();

  private final Path style;

  public StyleService(Path style) {
    this.style = style;
  }

  @Get("/style.json")
  @Produces("application/json; charset=utf-8")
  public HttpResponse getStyle() throws IOException {
    return HttpResponse.of(Files.readString(style));
  }

  @Put("/style.json")
  public void putStyle(JsonNode json) throws IOException {
    Files.writeString(style, json.toPrettyString());
    sink.tryEmitNext(ServerSentEvent.ofData(json.toString()));
  }

  @Get("/style/changes")
  @ProducesEventStream
  public Publisher<ServerSentEvent> sseChanges(ServiceRequestContext ctx) {
    ctx.clearRequestTimeout();
    return sink.asFlux();
  }

}
