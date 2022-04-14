# Baremaps Iploc

## Introduction

Baremaps Iploc is a module for geo-localisation by IP address. Using data publicly available from the 5
[Regional Internet Registries (RIRs)](https://whatismyipaddress.com/rir) we are able to generate a stream
of objects detailing Internet resource allocations. We call these NIC Objects (Network Information Centre Objects).

 - [ARIN](https://www.arin.net/)
 - [LACNIC](https://www.lacnic.net/)
 - [AFRINIC](https://afrinic.net/)
 - [RIPE NCC](https://www.ripe.net/)
 - [APNIC](https://www.apnic.net/)

Using the list of NIC Objects, we extract those that concern IPv4 ranges ([INETNUM](https://www.ripe.net/manage-ips-and-asns/db/support/documentation/ripe-database-documentation/rpsl-object-types/4-2-descriptions-of-primary-objects/4-2-4-description-of-the-inetnum-object))
, and using the Baremaps Geocoder service, we iterate through the extracted NIC objects to geolocate each object using the address stored in the object's attributes. 
These geolocalised IPv4 ranges are stored in a SQLite database which can be easily queried to geolocate a specific IP.

Each NIC Object contains a list of attributes. The attributes are not consistent between each NIC object. We use 4 attributes
of the NIC Objects: 

- *address* contains the address of the NIC Object
- *descr* sometimes contains the address of the NIC Object
- *country* contains the country code in ISO format (ISO 3166) - [RIPE list of country codes](https://www.ripe.net/participate/member-support/list-of-members/list-of-country-codes-and-rirs)
- *geoloc* contains the latitude and longitude which can be used directly

Some addresses NIC Objects contain a reference to an organisation, and the organisation NIC Object itself contains the 
geo-localisation information. However we don't make use of that for now.

The [structure of the RIPE database](https://www.ripe.net/manage-ips-and-asns/db/support/documentation/ripe-database-documentation/ripe-database-structure)
should be applicable all the RIRs.

## References
- [https://www.iana.org/numbers](https://www.iana.org/numbers)
- [https://www.irr.net/docs/list.html](https://www.irr.net/docs/list.html)
