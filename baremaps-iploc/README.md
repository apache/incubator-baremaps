# Baremaps Iploc

## Introduction

Baremaps Iploc is a module for geo-localisation by IP address. Using data publicly available from the 5
[Regional Internet Registries (RIRs)](https://whatismyipaddress.com/rir) we are able to generate a stream
of objects detailing Internet resource allocations. We call these NIC Objects (Network Information Centre Objects).

Here is the list of the 5 RIRs.

 - [ARIN](https://www.arin.net/)
 - [LACNIC](https://www.lacnic.net/)
 - [AFRINIC](https://afrinic.net/)
 - [RIPE NCC](https://www.ripe.net/)
 - [APNIC](https://www.apnic.net/)

Using the list of NIC Objects, we extract those that concern IPv4 address ranges ([INETNUM](https://www.ripe.net/manage-ips-and-asns/db/support/documentation/ripe-database-documentation/rpsl-object-types/4-2-descriptions-of-primary-objects/4-2-4-description-of-the-inetnum-object))
, and using the Baremaps Geocoder service, we iterate through the extracted NIC objects to geo-locate each object using the address stored in the object's attributes. 
These geo-localised IPv4 address ranges are stored in a SQLite database which can be easily queried to geo-locate a specific IP.

Each NIC Object contains a list of attributes but these attributes are not consistent between each NIC object. We try to use these 4 attributes to query the Geocoder service : 

- *address* contains the address of the NIC Object
- *descr* sometimes contains the address of the NIC Object
- *country* contains the country code in ISO format (ISO 3166) - [RIPE list of country codes](https://www.ripe.net/participate/member-support/list-of-members/list-of-country-codes-and-rirs)
- *geoloc* contains the latitude and longitude which can be used directly

Some NIC Objects contain a reference to an organisation, and the organisation's NIC Object itself contains the 
geo-localisation information. However, we don't make use of that for now.

The [structure of the RIPE database](https://www.ripe.net/manage-ips-and-asns/db/support/documentation/ripe-database-documentation/ripe-database-structure)
should be applicable to all the RIRs.

## Running the CLI

The `iploc init` command requires the path to the Geocoder file, as well as
the path to the target SQLite database file and the geocoder index path.

*Note that the `allCountries.txt` file is a dump of the Geonames database available here https://download.geonames.org/export/dump/.*

```bash
iploc init --index geocoder_index/ --database iploc.db --geonames /home/drabble/Downloads/allCountries.txt
```

## Code usage

In order to generate the SQLite database that contains the geo-localised IP address ranges you must follow a few steps.

1) First you need to load/build a Geocoder which will be used to query the addresses contained in the NIC objects.

```java
Geocoder geocoder = new GeonamesGeocoder(indexPath, dataUri);
geocoder.build();
```

2) Then you need to generate a Stream of NIC Objects. You can use the Nic Fetcher to automatically download all of the NIC Objects from the 5 RIRs.

```java
Stream<Path> nicPathsStream = new NicFetcher().fetch();
Stream<NicObject> nicObjectStream =
    nicPathsStream.flatMap(nicPath -> {
        try {
            return NicParser.parse(new BufferedInputStream(Files.newInputStream(nicPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Stream.empty();
    });
```

3) You need to create the IpLoc service, giving the target SQLite database url and the geocoder in the second parameter

```java
SqliteUtils.executeResource(databaseUrl, "iploc_init.sql"); // Init the SQLite database
IpLoc ipLoc = new IpLoc(databaseUrl, geocoder);
```

4) Finally insert the stream of NIC objects in the database with the IpLoc service

```java
ipLoc.insertNicObjects(nicObjects.stream());
```

## Notes

There are many improvements that need to be worked on to improve the Iploc module. The list is detailed in [TODO.md](TODO.md).

## References
- [https://www.iana.org/numbers](https://www.iana.org/numbers)
- [https://www.irr.net/docs/list.html](https://www.irr.net/docs/list.html)
