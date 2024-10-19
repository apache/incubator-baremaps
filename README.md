<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to you under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<div align="center">

# Apache Baremaps (Incubating)

[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat&logo=github&color=2370ff&labelColor=454545)](http://makeapullrequest.com)
![Build Passing](https://github.com/baremaps/baremaps/actions/workflows/build.yml/badge.svg)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=apache_baremaps&metric=vulnerabilities)](https://sonarcloud.io/project/overview?id=apache_baremaps)
[![Mailing List](https://img.shields.io/badge/Apache-dev_mailing_list-success.svg?logo=apache)](https://lists.apache.org/list.html?dev@baremaps.apache.org)

</div>

**Apache Baremaps** is a toolkit and a set of infrastructure components for creating, publishing, and operating online maps. It provides a data pipeline enabling developers to build maps with different data sources with live reload capabilities. It provides other services commonly used in online maps, such as location search and IP to location.

## 🔥 Live Demo

[Live Demo](https://baremaps.apache.org/map/)

## 📖 How do I use Apache Baremaps?

You can find the documentation on the project's [website](https://baremaps.apache.org/). The following pages showcase the main uses of Apache Baremaps.

- The [OpenStreetMap](https://baremaps.apache.org/documentation/examples/import-osm-into-postgis) example is a good introduction to Baremaps, it shows how to produce high resolution vector tiles.
- The [NaturalEarth](https://baremaps.apache.org/documentation/examples/import-naturalearth-into-postgis) example shows how to produce low resolution vector tiles.
- The [Contour](https://baremaps.apache.org/documentation/examples/import-contour-into-postgis) example shows how to produce contour lines from a digital elevation model.
- The [IP to location](https://baremaps.apache.org/documentation/examples/ip-to-location) example shows how to create and serve an IP to location service in a simple web application.
- The [Geocoding](https://baremaps.apache.org/documentation/examples/geocoding) example shows how to create and serve a geocoding service in a simple web application.

## 👩‍💻 How do I contribute?

There are many places where you can contribute to Apache Baremaps such as the code, the documentation, the website or the examples.

The official documentation is located in a separate [repository](https://github.com/apache/incubator-baremaps-site).

If you want to contribute to the code you can refer to the following developer guides available in the documentation.

- [Project structure](https://baremaps.apache.org/documentation/developer-manual/project-structure)
- [How to build with Maven](https://baremaps.apache.org/documentation/developer-manual/how-to-build-with-maven)
- [Set up in IntelliJ IDEA](https://baremaps.apache.org/documentation/developer-manual/setup-in-intellij)
- [Geocoder](https://baremaps.apache.org/documentation/developer-manual/geocoder)
- [IP to location](https://baremaps.apache.org/documentation/developer-manual/ip-to-location)
- [Basemap](https://baremaps.apache.org/documentation/developer-manual/basemap)

You can also contribute in the following ways.

- [Create an issue](https://github.com/apache/incubator-baremaps/issues): Report a bug, submit a feature request, or submit a pull request in the incubator-baremaps repository.
- [Improve the documentation](https://github.com/apache/incubator-baremaps-site/issues): Report documentation issue or submit a pull request in the incubator-baremaps-site repository.
- [Join the mailing list](https://lists.apache.org/list.html?dev@baremaps.apache.org): Initiate or participate in project discussions on the mailing list
- Write a post: Write a post to share your use cases and experiences with Apache Baremaps

Finally, check out [CONTRIBUTING](CONTRIBUTING.md) and [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md).

## 📄 License

This project is licensed under Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
