package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import com.baremaps.pipeline.Step;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public record FetchUri(String id, List<String> needs, String uri, String path) implements Step {

  @Override
  public void execute(Context context) {
    try (var inputStream = context.blobStore().get(URI.create(uri)).getInputStream()) {
      var downloadFile = context.directory().resolve(path);
      Files.createDirectories(downloadFile.getParent());
      Files.copy(inputStream, downloadFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
