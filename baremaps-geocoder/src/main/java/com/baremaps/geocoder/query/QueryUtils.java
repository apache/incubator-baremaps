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

package com.baremaps.geocoder.query;

import java.util.HashMap;
import java.util.Map;

public class QueryUtils {

  private QueryUtils() {}

  private static final int INITIAL_CAPACITY = 249;
  private static final Map<String, String> countryCodes = setup();

  /**
   * Get the iso 2 letters country code
   *
   * @param country the country name in english
   * @return the iso 2 letters code
   */
  public static String getCountryCode(String country) {
    return countryCodes.get(country.toLowerCase());
  }

  private static Map<String, String> setup() {
    Map<String, String> output = new HashMap<>(INITIAL_CAPACITY);

    output.put("afghanistan", "AF");
    output.put("aland islands", "AX");
    output.put("albania", "AL");
    output.put("algeria", "DZ");
    output.put("american samoa", "AS");
    output.put("andorra", "AD");
    output.put("angola", "AO");
    output.put("anguilla", "AI");
    output.put("antarctica", "AQ");
    output.put("antigua and barbuda", "AG");
    output.put("argentina", "AR");
    output.put("armenia", "AM");
    output.put("aruba", "AW");
    output.put("australia", "AU");
    output.put("austria", "AT");
    output.put("azerbaijan", "AZ");
    output.put("bahamas", "BS");
    output.put("bahrain", "BH");
    output.put("bangladesh", "BD");
    output.put("barbados", "BB");
    output.put("belarus", "BY");
    output.put("belgium", "BE");
    output.put("belize", "BZ");
    output.put("benin", "BJ");
    output.put("bermuda", "BM");
    output.put("bhutan", "BT");
    output.put("bolivia", "BO");
    output.put("bosnia and herzegovina", "BA");
    output.put("botswana", "BW");
    output.put("bouvet island", "BV");
    output.put("brazil", "BR");
    output.put("british indian ocean territory", "IO");
    output.put("brunei darussalam", "BN");
    output.put("bulgaria", "BG");
    output.put("burkina faso", "BF");
    output.put("burundi", "BI");
    output.put("cambodia", "KH");
    output.put("cameroon", "CM");
    output.put("canada", "CA");
    output.put("cape verde", "CV");
    output.put("cayman islands", "KY");
    output.put("central african republic", "CF");
    output.put("chad", "TD");
    output.put("chile", "CL");
    output.put("china", "CN");
    output.put("christmas island", "CX");
    output.put("cocos (keeling) islands", "CC");
    output.put("colombia", "CO");
    output.put("comoros", "KM");
    output.put("congo", "CG");
    output.put("congo, democratic republic", "CD");
    output.put("cook islands", "CK");
    output.put("costa rica", "CR");
    output.put("cote d'ivoire", "CI");
    output.put("croatia", "HR");
    output.put("cuba", "CU");
    output.put("cyprus", "CY");
    output.put("czech republic", "CZ");
    output.put("denmark", "DK");
    output.put("djibouti", "DJ");
    output.put("dominica", "DM");
    output.put("dominican republic", "DO");
    output.put("ecuador", "EC");
    output.put("egypt", "EG");
    output.put("el salvador", "SV");
    output.put("equatorial guinea", "GQ");
    output.put("eritrea", "ER");
    output.put("estonia", "EE");
    output.put("ethiopia", "ET");
    output.put("falkland islands (malvinas)", "FK");
    output.put("faroe islands", "FO");
    output.put("fiji", "FJ");
    output.put("finland", "FI");
    output.put("france", "FR");
    output.put("french guiana", "GF");
    output.put("french polynesia", "PF");
    output.put("french southern territories", "TF");
    output.put("gabon", "GA");
    output.put("gambia", "GM");
    output.put("georgia", "GE");
    output.put("germany", "DE");
    output.put("ghana", "GH");
    output.put("gibraltar", "GI");
    output.put("greece", "GR");
    output.put("greenland", "GL");
    output.put("grenada", "GD");
    output.put("guadeloupe", "GP");
    output.put("guam", "GU");
    output.put("guatemala", "GT");
    output.put("guernsey", "GG");
    output.put("guinea", "GN");
    output.put("guinea-bissau", "GW");
    output.put("guyana", "GY");
    output.put("haiti", "HT");
    output.put("heard island & mcdonald islands", "HM");
    output.put("holy see (vatican city state)", "VA");
    output.put("honduras", "HN");
    output.put("hong kong", "HK");
    output.put("hungary", "HU");
    output.put("iceland", "IS");
    output.put("india", "IN");
    output.put("indonesia", "ID");
    output.put("iran, islamic republic of", "IR");
    output.put("iraq", "IQ");
    output.put("ireland", "IE");
    output.put("isle of man", "IM");
    output.put("israel", "IL");
    output.put("italy", "IT");
    output.put("jamaica", "JM");
    output.put("japan", "JP");
    output.put("jersey", "JE");
    output.put("jordan", "JO");
    output.put("kazakhstan", "KZ");
    output.put("kenya", "KE");
    output.put("kiribati", "KI");
    output.put("korea", "KR");
    output.put("kuwait", "KW");
    output.put("kyrgyzstan", "KG");
    output.put("lao people's democratic republic", "LA");
    output.put("latvia", "LV");
    output.put("lebanon", "LB");
    output.put("lesotho", "LS");
    output.put("liberia", "LR");
    output.put("libyan arab jamahiriya", "LY");
    output.put("liechtenstein", "LI");
    output.put("lithuania", "LT");
    output.put("luxembourg", "LU");
    output.put("macao", "MO");
    output.put("macedonia", "MK");
    output.put("madagascar", "MG");
    output.put("malawi", "MW");
    output.put("malaysia", "MY");
    output.put("maldives", "MV");
    output.put("mali", "ML");
    output.put("malta", "MT");
    output.put("marshall islands", "MH");
    output.put("martinique", "MQ");
    output.put("mauritania", "MR");
    output.put("mauritius", "MU");
    output.put("mayotte", "YT");
    output.put("mexico", "MX");
    output.put("micronesia, federated states of", "FM");
    output.put("moldova", "MD");
    output.put("monaco", "MC");
    output.put("mongolia", "MN");
    output.put("montenegro", "ME");
    output.put("montserrat", "MS");
    output.put("morocco", "MA");
    output.put("mozambique", "MZ");
    output.put("myanmar", "MM");
    output.put("namibia", "NA");
    output.put("nauru", "NR");
    output.put("nepal", "NP");
    output.put("netherlands", "NL");
    output.put("netherlands antilles", "AN");
    output.put("new caledonia", "NC");
    output.put("new zealand", "NZ");
    output.put("nicaragua", "NI");
    output.put("niger", "NE");
    output.put("nigeria", "NG");
    output.put("niue", "NU");
    output.put("norfolk island", "NF");
    output.put("northern mariana islands", "MP");
    output.put("norway", "NO");
    output.put("oman", "OM");
    output.put("pakistan", "PK");
    output.put("palau", "PW");
    output.put("palestinian territory, occupied", "PS");
    output.put("panama", "PA");
    output.put("papua new guinea", "PG");
    output.put("paraguay", "PY");
    output.put("peru", "PE");
    output.put("philippines", "PH");
    output.put("pitcairn", "PN");
    output.put("poland", "PL");
    output.put("portugal", "PT");
    output.put("puerto rico", "PR");
    output.put("qatar", "QA");
    output.put("reunion", "RE");
    output.put("romania", "RO");
    output.put("russian federation", "RU");
    output.put("rwanda", "RW");
    output.put("saint barthelemy", "BL");
    output.put("saint helena", "SH");
    output.put("saint kitts and nevis", "KN");
    output.put("saint lucia", "LC");
    output.put("saint martin", "MF");
    output.put("saint pierre and miquelon", "PM");
    output.put("saint vincent and grenadines", "VC");
    output.put("samoa", "WS");
    output.put("san marino", "SM");
    output.put("sao tome and principe", "ST");
    output.put("saudi arabia", "SA");
    output.put("senegal", "SN");
    output.put("serbia", "RS");
    output.put("seychelles", "SC");
    output.put("sierra leone", "SL");
    output.put("singapore", "SG");
    output.put("slovakia", "SK");
    output.put("slovenia", "SI");
    output.put("solomon islands", "SB");
    output.put("somalia", "SO");
    output.put("south africa", "ZA");
    output.put("south georgia and sandwich isl.", "GS");
    output.put("spain", "ES");
    output.put("sri lanka", "LK");
    output.put("sudan", "SD");
    output.put("suriname", "SR");
    output.put("svalbard and jan mayen", "SJ");
    output.put("swaziland", "SZ");
    output.put("sweden", "SE");
    output.put("switzerland", "CH");
    output.put("syrian arab republic", "SY");
    output.put("taiwan", "TW");
    output.put("tajikistan", "TJ");
    output.put("tanzania", "TZ");
    output.put("thailand", "TH");
    output.put("timor-leste", "TL");
    output.put("togo", "TG");
    output.put("tokelau", "TK");
    output.put("tonga", "TO");
    output.put("trinidad and tobago", "TT");
    output.put("tunisia", "TN");
    output.put("turkey", "TR");
    output.put("turkmenistan", "TM");
    output.put("turks and caicos islands", "TC");
    output.put("tuvalu", "TV");
    output.put("uganda", "UG");
    output.put("ukraine", "UA");
    output.put("united arab emirates", "AE");
    output.put("united kingdom", "GB");
    output.put("united states", "US");
    output.put("united states outlying islands", "UM");
    output.put("uruguay", "UY");
    output.put("uzbekistan", "UZ");
    output.put("vanuatu", "VU");
    output.put("venezuela", "VE");
    output.put("viet nam", "VN");
    output.put("virgin islands, british", "VG");
    output.put("virgin islands, u.s.", "VI");
    output.put("wallis and futuna", "WF");
    output.put("western sahara", "EH");
    output.put("yemen", "YE");
    output.put("zambia", "ZM");
    output.put("zimbabwe", "ZW");
    return output;
  }
}
