package io.gazetteer.osm.osmxml;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class XMLConstants {

  protected static final String CREATE = "create";
  protected static final String MODIFY = "modify";
  protected static final String DELETE = "delete";
  protected static final String USER = "user";
  protected static final String NODE = "node";
  protected static final String WAY = "way";
  protected static final String RELATION = "relation";
  protected static final String ID = "id";
  protected static final String LON = "lon";
  protected static final String LAT = "lat";
  protected static final String VERSION = "version";
  protected static final String TIMESTAMP = "timestamp";
  protected static final String CHANGESET = "changeset";
  protected static final String UID = "uid";
  protected static final String TAG = "tag";
  protected static final String MEMBER = "member";
  protected static final String TYPE = "type";
  protected static final String ROLE = "role";
  protected static final String REF = "ref";
  protected static final String ND = "nd";
  protected static final String KEY = "k";
  protected static final String VAL = "v";

  protected static final SimpleDateFormat format;
  static {
    format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
  }
}
