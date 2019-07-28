package io.gazetteer.osm.osmxml;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

public class XMLConstants {

  public static final String CREATE = "create";
  public static final String MODIFY = "modify";
  public static final String DELETE = "delete";
  public static final String USER = "user";
  public static final String NODE = "node";
  public static final String WAY = "way";
  public static final String RELATION = "relation";
  public static final String ID = "id";
  public static final String LON = "lon";
  public static final String LAT = "lat";
  public static final String VERSION = "version";
  public static final String TIMESTAMP = "timestamp";
  public static final String CHANGESET = "changeset";
  public static final String UID = "uid";
  public static final String TAG = "tag";
  public static final String MEMBER = "member";
  public static final String TYPE = "type";
  public static final String ROLE = "role";
  public static final String REF = "ref";
  public static final String ND = "nd";
  public static final String KEY = "k";
  public static final String VAL = "v";

  public static final DateTimeFormatter format;
  static {
    format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
  }
}
