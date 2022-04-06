package com.baremaps.core;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;

public class Test {

  public static void main(String... args) {

    String file = "/Users/bchapuis/Downloads/natural_earth_vector.gpkg/packages/natural_earth_vector.gpkg";
    GeoPackage geoPackage = GeoPackageManager.open(new File(file));

    List<String> features = geoPackage.getFeatureTables();

    features.get(0);
    // Query Features
    String featureTable = features.get(0);
    FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
    FeatureResultSet featureResultSet = featureDao.queryForAll();
    try {
      for (FeatureRow featureRow : featureResultSet) {

      }
    } finally {
      featureResultSet.close();
    }

    System.out.println(features);

  }

}
