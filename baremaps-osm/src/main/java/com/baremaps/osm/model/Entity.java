package com.baremaps.osm.model;

/**
 * An entity in the OSM dataset.
 */
public interface Entity {

  /**
   * Returns the metadata of the entity.
   *
   * @return the metadata
   */
  Info getInfo();
}
