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

package org.apache.sis.storage.shapefile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.logging.Logger;
import org.apache.sis.feature.AbstractAttribute;
import org.apache.sis.feature.AbstractFeature;
import org.apache.sis.storage.DataStoreException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the {@link ShapeFile} class.
 *
 * @author Travis L. Pinney
 * @version 0.5
 * @since 0.5
 * @module
 */
public final class ShapeFileTest {
  /**
   * Returns URI path to a resource.
   *
   * @param name Resource name.
   * @return URI path.
   * @throws URISyntaxException if the resource name is incorrect.
   */
  private static String path(final String name) throws URISyntaxException {
    return new File(ShapeFileTest.class.getResource(name).toURI()).getPath();
  }

  /**
   * Test polylines count.
   *
   * @throws URISyntaxException if the resource name is incorrect.
   * @throws DataStoreException if a general file reading trouble occurs.
   */
  @Test
  public void testPolyineCount() throws URISyntaxException, DataStoreException {
    ShapeFile shp = new ShapeFile(path("SignedBikeRoute_4326_clipped.shp"));
    readAll(shp);
  }

  /**
   * Test polygon count.
   *
   * @throws URISyntaxException if the resource name is incorrect.
   * @throws DataStoreException if a general file reading trouble occurs.
   */
  @Test
  @Ignore("Shapefile removed from history (SIS-422)")
  public void testPolygonCount() throws URISyntaxException, DataStoreException {
    ShapeFile shp = new ShapeFile(path("ANC90Ply_4326.shp"));
    readAll(shp);
  }

  /**
   * Test point count.
   *
   * @throws URISyntaxException if the resource name is incorrect.
   * @throws DataStoreException if a general file reading trouble occurs.
   */
  @Test
  public void testPointCount() throws URISyntaxException, DataStoreException {
    ShapeFile shp = new ShapeFile(path("ABRALicenseePt_4326_clipped.shp"));
    readAll(shp);
  }

  /**
   * Test loading of shapefile descriptors.
   *
   * @throws URISyntaxException if the resource name is incorrect.
   * @throws DataStoreException if a general file reading trouble occurs.
   */
  @Test
  public void testDescriptors() throws URISyntaxException, DataStoreException {
    Logger log = org.apache.sis.util.logging.Logging.getLogger(ShapeFileTest.class);

    ShapeFile shp = new ShapeFile(path("ABRALicenseePt_4326_clipped.shp"));
    shp.loadDescriptors();

    assertNotNull(
        "The features type of the shapefile should have been set.", shp.getFeaturesType());
    log.info(
        MessageFormat.format(
            "ABRALicenseePt_4326_clipped.shp features type : {0}", shp.getFeaturesType()));

    assertNotNull(
        "The shapefile descriptor of the shapefile should have been set.",
        shp.getShapefileDescriptor());
    log.info(
        MessageFormat.format(
            "ABRALicenseePt_4326_clipped.shp shapefile descriptor : {0}",
            shp.getShapefileDescriptor()));

    assertNotNull(
        "The DBase III fields descriptors of the shapefile should have been set.",
        shp.getDatabaseFieldsDescriptors());
    log.info(
        MessageFormat.format(
            "ABRALicenseePt_4326_clipped.shp DBase fields descriptors : {0}",
            shp.getDatabaseFieldsDescriptors()));

    // Loading of the descriptor shall not prevent the shapefile from being red again.
    readAll(shp);
  }

  /**
   * Checks that the reader is able to detect EoF signs in the DBase file.
   *
   * @throws URISyntaxException if the resource name is incorrect.
   * @throws DataStoreException if a general file reading trouble occurs.
   */
  @Test
  @Ignore // TODO Adapt with another shapefile.
  public void testHandleEofNotification() throws URISyntaxException, DataStoreException {
    ShapeFile shp = new ShapeFile(path("DEPARTEMENT.SHP"));
    AbstractFeature first = null, last = null;

    Logger log = org.apache.sis.util.logging.Logging.getLogger(ShapeFileTest.class);

    try (InputFeatureStream is = shp.findAll()) {
      AbstractFeature feature = is.readFeature();

      // Read and retain the first and the last feature.
      while (feature != null) {
        if (first == null) {
          first = feature;
        }

        // Advice : To debug just before the last record, put a conditional breakpoint on department
        // name "MEURTHE-ET-MOSELLE".
        String deptName = (String) ((AbstractAttribute) feature.getProperty("NOM_DEPT")).getValue();
        log.info(deptName);

        last = feature;
        feature = is.readFeature();
      }
    }

    assertNotNull("No record has been found in the DBase file or Shapefile.", first);
    assertNotNull(
        "This test is not working well : last feature should always be set if any feature has been found.",
        last);
    assertEquals(
        "The first record red must be JURA department.",
        "JURA",
        ((AbstractAttribute) first.getProperty("NOM_DEPT")).getValue());
    assertEquals(
        "The last record red must be DEUX-SEVRES department.",
        "DEUX-SEVRES",
        ((AbstractAttribute) last.getProperty("NOM_DEPT")).getValue());
  }

  /**
   * Testing direct access in the shapefile.
   *
   * @throws URISyntaxException if the resource name is incorrect.
   * @throws DataStoreException if a general file reading trouble occurs.
   */
  @Test
  public void testDirectAcces() throws DataStoreException, URISyntaxException {
    ShapeFile shp = new ShapeFile(path("ABRALicenseePt_4326_clipped.shp"));

    // 1) Find the third record, sequentially.
    AbstractFeature thirdFeature;

    try (InputFeatureStream isSequential = shp.findAll()) {
      isSequential.readFeature();
      isSequential.readFeature();
      thirdFeature = isSequential.readFeature();
    }

    // Take one of its key fields and another field for reference, and its geometry.
    Double sequentialAddressId =
        Double.valueOf(
            (String) (((AbstractAttribute) thirdFeature.getProperty("ADDRID"))).getValue());
    String sequentialAddress =
        (String) (((AbstractAttribute) thirdFeature.getProperty("ADDRESS"))).getValue();
    Object sequentialGeometry = thirdFeature.getPropertyValue("geometry");

    // 2) Now attempt a direct access to this feature.
    AbstractFeature directFeature;
    String sql =
        MessageFormat.format(
            "SELECT * FROM ABRALicenseePt_4326_clipped WHERE ADDRID = {0,number,#0}",
            sequentialAddressId);

    try (InputFeatureStream isDirect = shp.find(sql)) {
      directFeature = isDirect.readFeature();
      assertNotNull("The direct access feature returned should not be null", directFeature);
    }

    assertNotNull(
        "The field ADDRID in the direct access feature has not been found again.",
        directFeature.getProperty("ADDRID"));

    Double directAddressId =
        Double.valueOf(
            (String) (((AbstractAttribute) directFeature.getProperty("ADDRID"))).getValue());
    String directAddress =
        (String) (((AbstractAttribute) directFeature.getProperty("ADDRESS"))).getValue();
    Object directGeometry = directFeature.getPropertyValue("geometry");

    assertEquals(
        "DBase part : direct access didn't returned the same address id than sequential access.",
        sequentialAddressId,
        directAddressId);
    assertEquals(
        "DBase part : direct access didn't returned the same address than sequential access.",
        sequentialAddress,
        directAddress);
    assertEquals(
        "Shapefile part : direct access didn't returned the same geometry than sequential access.",
        sequentialGeometry,
        directGeometry);
  }

  /**
   * Read all the shapefile content.
   *
   * @param shp Shapefile to read.
   * @throws DataStoreException if a general file reading trouble occurs.
   */
  private void readAll(ShapeFile shp) throws DataStoreException {
    try (InputFeatureStream is = shp.findAll()) {
      AbstractFeature feature = is.readFeature();

      while (feature != null) {
        feature = is.readFeature();
      }
    }
  }
}
