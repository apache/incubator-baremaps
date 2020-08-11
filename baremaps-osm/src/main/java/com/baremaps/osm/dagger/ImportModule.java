/*
 * Copyright (C) 2020 The Baremaps Authors
 *
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
package com.baremaps.osm.dagger;

import com.baremaps.osm.cache.Cache;
import com.baremaps.osm.model.Node;
import com.baremaps.osm.model.Relation;
import com.baremaps.osm.model.Way;
import com.baremaps.osm.parser.PBFFileBlockHandler;
import com.baremaps.osm.store.PostgisHeaderStore;
import com.baremaps.osm.store.PostgisNodeStore;
import com.baremaps.osm.store.PostgisRelationStore;
import com.baremaps.osm.store.PostgisWayStore;
import com.baremaps.osm.store.Store;
import com.baremaps.osm.store.StoreImportHandler;
import dagger.Module;
import dagger.Provides;
import java.util.List;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;

@Module
public class ImportModule {

  private final Cache<Long, Coordinate> coordinateCache;

  private final Cache<Long, List<Long>> referenceCache;

  private final DataSource dataSource;

  public ImportModule(
      Cache<Long, Coordinate> coordinateCache,
      Cache<Long, List<Long>> referenceCache,
      DataSource dataSource) {
    this.coordinateCache = coordinateCache;
    this.referenceCache = referenceCache;
    this.dataSource = dataSource;
  }

  @Provides
  @Singleton
  public GeometryFactory providesGeometryFactory() {
    return new GeometryFactory();
  }

  @Provides
  @Singleton
  public CoordinateTransform providesCoordinateTranform() {
    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem sourceCRS = crsFactory.createFromName("EPSG:4326");
    CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:3857");
    CoordinateTransformFactory factory = new CoordinateTransformFactory();
    CoordinateTransform coordinateTransform = factory.createTransform(sourceCRS, targetCRS);
    return coordinateTransform;
  }

  @Provides
  @Singleton
  public Cache<Long, Coordinate> providesCoordinateCache() {
    return coordinateCache;
  }

  @Provides
  @Singleton
  public Cache<Long, List<Long>> providesReferenceCache() {
    return referenceCache;
  }

  @Provides
  @Singleton
  public DataSource providesDataSource() {
    return dataSource;
  }

  @Provides
  @Singleton
  public PostgisHeaderStore providesHeaderStore(DataSource dataSource) {
    return new PostgisHeaderStore(dataSource);
  }

  @Provides
  @Singleton
  public Store<Node> providesNodeStore(PostgisNodeStore nodeStore) {
    return nodeStore;
  }

  @Provides
  @Singleton
  public Store<Way> providesWayStore(DataSource dataSource) {
    return new PostgisWayStore(dataSource);
  }

  @Provides
  @Singleton
  public Store<Relation> providesReferenceStore(DataSource dataSource) {
    return new PostgisRelationStore(dataSource);
  }

  @Provides
  @Singleton
  public PBFFileBlockHandler providesFileBlockHandler(
      PostgisHeaderStore headerStore,
      Store<Node> nodeStore,
      Store<Way> wayStore,
      Store<Relation> referenceStore) {
    return new StoreImportHandler(headerStore, nodeStore, wayStore, referenceStore);
  }

}
