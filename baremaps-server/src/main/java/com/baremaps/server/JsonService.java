package com.baremaps.server;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.linecorp.armeria.common.HttpRequest;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.AbstractHttpService;
import com.linecorp.armeria.server.ServiceRequestContext;
import java.util.function.Supplier;

public class JsonService extends AbstractHttpService {

  private final Supplier<?> supplier;

  private final ObjectWriter writer;

  public JsonService(Supplier<?> supplier) {
    this.supplier = supplier;
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    writer = mapper.writerWithDefaultPrettyPrinter();
  }

  @Override
  protected HttpResponse doGet(ServiceRequestContext ctx, HttpRequest req) throws JsonProcessingException {
    return HttpResponse.of(writer.writeValueAsString(supplier.get()));
  }

}
