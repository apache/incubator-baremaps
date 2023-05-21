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

package org.apache.baremaps.raster;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsterDem {

  public static void main(String[] args) throws IOException {

    var target = Paths.get("/Volumes/Data/data/ASTGTMV003");
    if (Files.notExists(target)) {
      Files.createDirectories(target);
    }
    var baseUrl = "https://e4ftl01.cr.usgs.gov/ASTT/ASTGTM.003/2000.03.01/";
    var token = "...";

    var directoryUrl = new URL(baseUrl);
    var directoryConnection = (HttpURLConnection) directoryUrl.openConnection();

    var files = new ArrayList<String>();
    try (var stream = new BufferedInputStream(directoryConnection.getInputStream())) {
      var bytes = stream.readAllBytes();
      var html = new String(bytes);
      Pattern p = Pattern.compile("href=\"(ASTGTMV003_.*?)\"");
      Matcher m = p.matcher(html);
      while (m.find()) {
        files.add(m.group(1));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    files.parallelStream().forEach(file -> {
      try {
        if (Files.exists(target.resolve(file))) {
          return;
        }
        System.out.println("Downloading " + file);
        var fileUrl = new URL(baseUrl + file);
        var fileConnection = (HttpURLConnection) fileUrl.openConnection();
        fileConnection.setRequestProperty("Authorization", "Bearer " + token);
        try (var stream = new BufferedInputStream(fileConnection.getInputStream())) {
          Files.copy(stream, target.resolve(file), StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }
}
