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

public class GeocoderServiceImpl extends GeocoderServiceImplBase {
  private Geocoder geocoder;

  public GeocoderServiceImpl(Geocoder geocoder) {
    this.geocoder = geocoder;
  }

  @Override
  public void search(SearchRequest request,
      StreamObserver<SearchReply> responseObserver) {
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
    if(response != null && !response.results().isEmpty()) {
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
