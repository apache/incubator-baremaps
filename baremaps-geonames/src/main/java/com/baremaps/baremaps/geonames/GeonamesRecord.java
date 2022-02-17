package com.baremaps.baremaps.geonames;

import java.util.StringJoiner;

/** Structured of a Geonames record. */
public class GeonamesRecord {
  // geonameid: integer id of record in geonames database
  public final Integer geonameid;

  // name: name of geographical point (utf8) varchar(200)
  public final String name;

  // asciiname: name of geographical point in plain ascii characters, varchar(200)
  public final String asciiname;

  // alternatenames: alternatenames, comma separated, ascii names automatically transliterated,
  // convenience attribute from alternatename table, varchar(10000)
  public final String alternatenames;

  // latitude: latitude in decimal degrees (wgs84)
  public final Double latitude;

  // longitude: longitude in decimal degrees (wgs84)
  public final Double longitude;

  // feature class: see http://www.geonames.org/export/codes.html, char(1)
  public final String featureClass;

  // feature code: see http://www.geonames.org/export/codes.html, varchar(10)
  public final String featureCode;

  // country code: ISO-3166 2-letter country code, 2 characters
  public final String countryCode;

  // cc2: alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
  public final String cc2;

  // admin1 code: fipscode (subject to change to iso code), see exceptions below, see file
  // admin1Codes.txt for display names of this code; varchar(20)
  // in switzerland usually canton code (ex: VD)
  public final String admin1Code;

  // admin2 code: code for the second administrative division, a county in the US, see file
  // admin2Codes.txt; varchar(80)
  public final String admin2Code;

  // admin3 code: code for third level administrative division, varchar(20)
  public final String admin3Code;

  // admin4 code: code for fourth level administrative division, varchar(20)
  public final String admin4Code;

  // population: bigint (8 byte int)
  public final Long population;

  // elevation: in meters, integer
  public final Integer elevation;

  // dem: digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or
  // 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
  public final Integer dem;

  // timezone: the iana timezone id (see file timeZone.txt) varchar(40)
  public final String timezone;

  // modification date: date of last modification in yyyy-MM-dd format
  public final String modificationDate;

  /**
   * Main constructor.
   *
   * @param geonameid - integer id of record in geonames database
   * @param name - name of geographical point (utf8) varchar(200)
   * @param asciiname - name of geographical point in plain ascii characters, varchar(200)
   * @param alternatenames - alternatenames, comma separated, ascii names automatically
   *     transliterated, convenience attribute from alternatename table, varchar(10000)
   * @param latitude - latitude in decimal degrees (wgs84)
   * @param longitude - longitude in decimal degrees (wgs84)
   * @param featureClass - see http://www.geonames.org/export/codes.html, char(1)
   * @param featureCode - see http://www.geonames.org/export/codes.html, varchar(10)
   * @param countryCode - ISO-3166 2-letter country code, 2 characters
   * @param cc2 - alternate country codes, comma separated, ISO-3166 2-letter country code, 200
   *     characters
   * @param admin1Code - fipscode (subject to change to iso code), see exceptions below, see file
   *     admin1Codes.txt for display names of this code; varchar(20)
   * @param admin2Code - code for the second administrative division, a county in the US, see file
   *     admin2Codes.txt; varchar(80)
   * @param admin3Code - code for third level administrative division, varchar(20)
   * @param admin4Code - code for fourth level administrative division, varchar(20)
   * @param population - bigint (8 byte int)
   * @param elevation - in meters, integer
   * @param dem - digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca
   *     90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by
   *     cgiar/ciat.
   * @param timezone - the iana timezone id (see file timeZone.txt) varchar(40)
   * @param modificationDate - date of last modification in yyyy-MM-dd format
   */
  GeonamesRecord(
      Integer geonameid,
      String name,
      String asciiname,
      String alternatenames,
      Double latitude,
      Double longitude,
      String featureClass,
      String featureCode,
      String countryCode,
      String cc2,
      String admin1Code,
      String admin2Code,
      String admin3Code,
      String admin4Code,
      Long population,
      Integer elevation,
      Integer dem,
      String timezone,
      String modificationDate) {
    this.geonameid = geonameid;
    this.name = name;
    this.asciiname = asciiname;
    this.alternatenames = alternatenames;
    this.latitude = latitude;
    this.longitude = longitude;
    this.featureClass = featureClass;
    this.featureCode = featureCode;
    this.countryCode = countryCode;
    this.cc2 = cc2;
    this.admin1Code = admin1Code;
    this.admin2Code = admin2Code;
    this.admin3Code = admin3Code;
    this.admin4Code = admin4Code;
    this.population = population;
    this.elevation = elevation;
    this.dem = dem;
    this.timezone = timezone;
    this.modificationDate = modificationDate;
  }

  /**
   * Show GeonamesRecord formatted data.
   *
   * @return String
   */
  @Override
  public String toString() {
    return new StringJoiner(", ", GeonamesRecord.class.getSimpleName() + "[", "]")
        .add("geonameid=" + geonameid)
        .add("name='" + name + "'")
        .add("asciiname='" + asciiname + "'")
        .add("alternatenames='" + alternatenames + "'")
        .add("latitude=" + latitude)
        .add("longitude=" + longitude)
        .add("featureClass='" + featureClass + "'")
        .add("featureCode='" + featureCode + "'")
        .add("countryCode='" + countryCode + "'")
        .add("cc2='" + cc2 + "'")
        .add("admin1Code='" + admin1Code + "'")
        .add("admin2Code='" + admin2Code + "'")
        .add("admin3Code='" + admin3Code + "'")
        .add("admin4Code='" + admin4Code + "'")
        .add("population=" + population)
        .add("elevation=" + elevation)
        .add("dem=" + dem)
        .add("timezone='" + timezone + "'")
        .add("modificationDate='" + modificationDate + "'")
        .toString();
  }
}
