/*
 * Copyright (C) 2011 The Baremaps Authors
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

package com.baremaps.tiles.postgis;

import com.baremaps.core.tile.Tile;
import com.baremaps.tiles.TileReader;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;

public abstract class AbstractTileReader implements TileReader {

    private final CRSFactory crsFactory = new CRSFactory();
    private final CoordinateReferenceSystem epsg4326 = crsFactory.createFromName("EPSG:4326");
    private final CoordinateReferenceSystem epsg3857 = crsFactory.createFromName("EPSG:3857");
    private final CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    private final CoordinateTransform coordinateTransform = coordinateTransformFactory
            .createTransform(epsg4326, epsg3857);

    private static final String SQL_ENVELOPE = "ST_MakeEnvelope({0}, {1}, {2}, {3}, 3857)";

    protected static final Pattern SQL_SELECT = Pattern.compile("SELECT(.*),(.*),(.*)FROM(.*?)(?:WHERE(.*))?");

    protected String envelope(Tile tile) {
        Envelope envelope = tile.envelope();
        Coordinate min = coordinate(envelope.getMinX(), envelope.getMinY());
        Coordinate max = coordinate(envelope.getMaxX(), envelope.getMaxY());
        return MessageFormat.format(
                SQL_ENVELOPE,
                Double.toString(min.getX()),
                Double.toString(min.getY()),
                Double.toString(max.getX()),
                Double.toString(max.getY()));
    }

    protected Coordinate coordinate(double x, double y) {
        ProjCoordinate coordinate = coordinateTransform.transform(new ProjCoordinate(x, y), new ProjCoordinate());
        return new Coordinate(coordinate.x, coordinate.y);
    }
}
