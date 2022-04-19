package com.baremaps.pipeline.steps;

import com.baremaps.pipeline.Context;
import com.baremaps.pipeline.PipelineException;
import com.baremaps.pipeline.Step;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public record UnzipFile(String id, List<String> needs, String file, String directory) implements Step {

  @Override
  public void execute(Context context) {
    var filePath = context.directory().resolve(file);
    var directoryPath = context.directory().resolve(directory);
    try (var zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(filePath)))) {
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        var file = directoryPath.resolve(ze.getName());
        Files.createDirectories(file.getParent());
        Files.copy(zis, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }
}
