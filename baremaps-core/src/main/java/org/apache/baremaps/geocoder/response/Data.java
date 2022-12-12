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

package org.apache.baremaps.geocoder.response;

/*
 * Licensed under the Apache License, Version 2.0 (the "License"), you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * Structured of a Geonames record.
 */
public record Data(

        // name of geographical point (utf8) varchar(200)
        String name,

        // name of geographical point in plain ascii characters, varchar(200)
        String asciiname,

        // alternatenames, comma separated, ascii names automatically transliterated,
        // convenience attribute from alternatename table, varchar(10000)
        String alternatenames,

        // latitude in decimal degrees (wgs84)
        Double latitude,

        // longitude in decimal degrees (wgs84)
        Double longitude,

        // see http://www.geonames.org/export/codes.html, char(1)
        String featureClass,

        // see http://www.geonames.org/export/codes.html, varchar(10)
        String featureCode,

        // ISO-3166 2-letter country code, 2 characters
        String countryCode,

        // alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
        String cc2,

        // fipscode (subject to change to iso code), see exceptions below, see file
        // admin1Codes.txt for display names of this code, varchar(20)
        // in switzerland usually canton code (ex: VD)
        String admin1Code,

        // code for the second administrative division, a county in the US, see file
        // admin2Codes.txt, varchar(80)
        String admin2Code,

        // code for third level administrative division, varchar(20)
        String admin3Code,

        // code for fourth level administrative division, varchar(20)
        String admin4Code,

        // population
        Long population,

        // elevation in meters, integer
        Integer elevation,

        // digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or
        // 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
        Integer dem,

        // the iana timezone id (see file timeZone.txt) varchar(40)
        String timezone,

        // date of last modification in yyyy-MM-dd format
        String modificationDate) {
}
