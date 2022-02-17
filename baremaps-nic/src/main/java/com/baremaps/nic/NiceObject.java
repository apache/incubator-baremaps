/*
 * Copyright (C) 2021 Heig-vd
 *
 * Licensed under the “Commons Clause” License Condition v1.0. You may obtain a copy of the License at
 *
 * https://github.com/heigvd-software-engineering/netscan/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baremaps.nic;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Representation of Nic Object. */
public class NiceObject {

  private List<NicAttribute> attributes;

  /**
   * Constructor.
   *
   * @param attributes - list of Nic Attributes
   */
  public NiceObject(List<NicAttribute> attributes) {
    checkNotNull(attributes);
    checkArgument(!attributes.isEmpty());
    Optional<NicAttribute> geoloc =
        attributes.stream().filter(attr -> attr.name().equals("geoloc")).findFirst();
    geoloc.ifPresent(
        s -> {
          String[] geolocLatLong = s.getValue().split(" ");
          // Check for geoloc format as it crash the db when converting to point otherwise
          if (geolocLatLong.length < 2
              || geolocLatLong[0].isEmpty()
              || geolocLatLong[1].isEmpty()) {
            attributes.remove(geoloc.get());
          } else {

            // manage case like (6,42 5.5) (+6 7) and (6,7)
            geolocLatLong[0] = geolocLatLong[0].replaceAll("(\\d),(\\d)", "$1.$2");
            geolocLatLong[1] = geolocLatLong[1].replaceAll("(\\d),(\\d)", "$1.$2");
            geolocLatLong[0] = geolocLatLong[0].replaceAll("[+,]", "");
            geolocLatLong[1] = geolocLatLong[1].replaceAll("[+,]", "");

            s.setName("geoloc_ripe");
            // inverse geoloc to be Longitude/Latitude
            String geolocLongLat = geolocLatLong[1] + " " + geolocLatLong[0];
            s.setValue(geolocLongLat);
          }
        });
    this.attributes = attributes;
  }

  /** Empty Constructor. */
  public NiceObject() {
    this.attributes = new ArrayList<>();
  }

  /**
   * Get RIPE Object type.
   *
   * @return Type of the RIPE object
   */
  public String type() {
    return attributes.get(0).name();
  }

  /**
   * Get RIPE Object id.
   *
   * @return RIPE Object id
   */
  public String id() {
    return attributes.get(0).value();
  }

  /**
   * Get RIPE Objects attributes.
   *
   * @return RIPE Objects attributes
   */
  public List<NicAttribute> attributes() {
    return attributes;
  }

  /**
   * Get RIPE Object first attribute value by name (single "line").
   *
   * @param name - attribute name
   * @return value of the attribute search
   */
  public Optional<String> single(String name) {
    return attributes.stream()
        .filter(attr -> attr.name().equals(name))
        .map(NicAttribute::value)
        .findFirst();
  }

  /**
   * Get RIPE Object attributes values by name (all the attribute with same name).
   *
   * @param name - attribute name
   * @return values of the attributes with same name
   */
  public List<String> multiple(String name) {
    return attributes.stream()
        .filter(attr -> attr.name().equals(name))
        .map(NicAttribute::value)
        .collect(Collectors.toList());
  }

  /**
   * Export RIPE Object into a txt file
   *
   * @param directoryPath - Folder whesre to save the RIPE Object
   */
  public void exportToFile(Path directoryPath) throws IOException {
    if (Files.notExists(directoryPath)) {
      Files.createDirectories(directoryPath);
    }
    String fileName = type() + "-" + id() + ".txt";
    FileOutputStream fos = new FileOutputStream(directoryPath + "/" + fileName);

    try (DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos))) {
      outStream.writeUTF(toString());
    }
  }

  public List<NicAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<NicAttribute> attributes) {
    this.attributes = attributes;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    for (NicAttribute attr : attributes()) {
      str.append(attr.name()).append(": ").append(attr.value()).append("\n");
    }
    return str.toString();
  }

  public void addAttribute(NicAttribute ripeAttribute) {
    attributes.add(ripeAttribute);
  }
}
