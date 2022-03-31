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

package com.baremaps.grpc.service;

import com.baremaps.geocoder.Geocoder;
import com.baremaps.geocoder.Request;
import com.baremaps.geocoder.Response;
import com.baremaps.geocoder.Result;
import com.baremaps.grpc.GeocoderServiceGrpc.GeocoderServiceImplBase;
import com.baremaps.grpc.SearchReply;
import com.baremaps.grpc.SearchReply.GeonamesResults;
import com.baremaps.grpc.SearchReply.GeonamesResults.GeoPoint;
import com.baremaps.grpc.SearchRequest;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import org.apache.lucene.queryparser.classic.ParseException;

/** Implementation of the geocoder grpc service */
public class GeocoderServiceImpl extends GeocoderServiceImplBase {
  private Geocoder geocoder;

  /**
   * Constructs a GeocoderServiceImpl
   *
   * @param geocoder a baremapse-geocoder
   */
  public GeocoderServiceImpl(Geocoder geocoder) {
    this.geocoder = geocoder;
  }

  /**
   * Implementation of the search of the geolocation of a place define by a query
   *
   * @param request - search request
   * @param responseObserver - response of the search
   */
  @Override
  public void search(SearchRequest request, StreamObserver<SearchReply> responseObserver) {
    Request req = new Request(request.getQuery(), request.getLimit());
    Response response = null;
    try {
      response = geocoder.search(req);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException e) {
      e.printStackTrace();
    }

    Result bestResult;
    if (response != null && !response.results().isEmpty()) {
      bestResult = response.results().get(0);
    } else {
      throw new RuntimeException("No best result found");
    }

    GeoPoint.Builder geoPointBuilder = GeoPoint.newBuilder();
    geoPointBuilder.setLatitude(Double.parseDouble(bestResult.document().get("latitude")));
    geoPointBuilder.setLatitude(Double.parseDouble(bestResult.document().get("longitude")));

    GeonamesResults.Builder geonamesResultsBuilder = GeonamesResults.newBuilder();
    geonamesResultsBuilder.setLocation(geoPointBuilder);
    geonamesResultsBuilder.setName(bestResult.document().get("name"));

    SearchReply.Builder searchReplyBuilder = SearchReply.newBuilder();
    searchReplyBuilder.addGeonamesResults(geonamesResultsBuilder);

    SearchReply searchReply = searchReplyBuilder.build();
    responseObserver.onNext(searchReply);
    responseObserver.onCompleted();
  }
}
