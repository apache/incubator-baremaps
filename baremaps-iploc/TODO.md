# TODO and future improvements

We list here in no particular order the improvements to be done on the Iploc module.

## Caching the files from NicFetcher 

NicFetcher is a tool that downloads all the NIC files from the 5 RIRs. Some files are currently not required as they don't
contain any IP resources.

Each time a user runs the CLI, he will fetch again the same files without any caching functionality.

## Using Lucene Replicator to store the indexes of the geocoder

Lucene Replicator allows replicating files between a server and clients. it could be interesting for the retrieval of
the Geocoder indexes to be able to use a tool like that.

## Using a custom storage system

SQLite is a powerful tool, but nothing will be as efficient as a custom data structure created specifically for our
Iploc objects. If we decided to store the Iploc objects in a minimal binary format and generate a binary tree to search for
IPs, we could improve storage efficiency and search performances.

## Storing additional data in the database

For the moment we only store the field that was used for the query as well as the country code and network name
in the Iploc database. There is additional metadata that could be stored in the Nic objects which could have a use during the search.

## Using the continent as a filtering tool and as last resort

We use Nic files from different continents. Having knowledge of the source file that was used for a specific entry, 
we can filter our Geocoder search by continent. This way we will never find an address that is in the wrong continent.
E.g. We have a city that has the same name in England and in the USA.

Sometimes we will find no match for an entry and we could use the contientn as a last resort.

## Using the network name in the search

In africa and in other places, some entries don't contain any country or address fields but the country is written within the
network name. E.g. "Telecom Congo".

## Using the organisation field 

Many entries in the NIC files reference an organisation. Sometimes the only geographic information available concerning
an entry is the address within the organisation. Using the organisation would be a performance issue as we would have
to pre-process each file in order to extract organisations information. It would be interesting to see how many additional
addresses we can locate if we use the organisations.

## Adding information about accuracy such as radius

When we fetch an element in the Geocoder we can find which type of element we matched. Whether it is a road, a country,
a city, etc. From this data we can compute a radius of precision or a bbox that could be stored in the database.

## Defining what Lucene score is the minimum viable score

We have a threshold value at which we dismiss Lucene matches. This value was chosen arbitrarily and some analysis should
be done to define what is the perfect threshold.

## Including the Iploc service in an example GRPC service

We will create a GRPC service that serves the Iploc database inside the baremaps-grpc module.

## Insert into the database every item even if no location was found

It can be useful for the client to at least know the metadata of an IP even if no geoloc is provided.

## Using S3 to store files

The library https://github.com/awslabs/aws-java-nio-spi-for-s3 could be used to interact with a S3 storage as if it was
on the local filesystem. It could be useful for Geocoder index files and maybe SQLite?