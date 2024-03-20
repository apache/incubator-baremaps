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

# Release instructions

The following instructions assume that the release candidate version has been set in an environment variable:

```bash
export RELEASE_VERSION=<release> # e.g. 0.7.1
export RELEASE_CANDIDATE=<candidate> # e.g. 1
```

In order to release a new version of Apache Baremaps, follow these steps:

- [ ] Notify the mailing list and ask everyone to pause commits on the main branch
- [ ] Create a new issue on GitHub with the title "Release Baremaps <release>"
- [ ] Create a new branch for the release (e.g. `release-<release>`)

```bash
cd baremaps
git checkout -b release-<release>
git push --set-upstream origin release-<release>
```

- [ ] Set the release version and commit the changes:

```bash 
./mvnw versions:set -DnewVersion=<release>
git commit -a -m "Release Baremaps <release>"
```

- [ ] Tag the last commit with the release candidate version:

```bash
git tag v<release>-rc<candidate>
```

> TODO: The following step is not yet fully automated. We need to add secrets and steps to publish the artifacts to the [dev directory](https://dist.apache.org/repos/dist/dev/incubator/baremaps/) (SVN_USERNAME, SVN_PASSWORD) and to the maven repository (NEXUS_USERNAME, NEXUS_PASSWORD).

```bash

- [ ] Push the tag to the remote repository (this will trigger GitHub Action to build the release candidate, publish the artifacts to the [dev directory](https://dist.apache.org/repos/dist/dev/incubator/baremaps/) of dist.apache.org repository, and draft a release on GitHub):

```bash
git push origin v<release>-rc<candidate>
```

- [ ] Edit the release notes for this tag on GitHub.
- [ ] Ask the community to vote for the release candidate.
- [ ] If the release candidate is not approved by the community, commit the necessary changes, clean the git history, 
  and go back to step 5.

> TODO: The following step is not yet fully automated. We need to add secrets and steps to publish the artifacts to the [dev directory](https://dist.apache.org/repos/dist/dev/incubator/baremaps/) (SVN_USERNAME, SVN_PASSWORD) and to the maven repository (NEXUS_USERNAME, NEXUS_PASSWORD).

- [ ] If the release candidate is approved by the community, tag the release commit with the release version (this will trigger GitHub Action to build the release candidate, publish the artifacts to the [release directory](https://dist.apache.org/repos/dist/release/incubator/baremaps/) of dist.apache.org repository, and draft a release on GitHub):

```bash
git tag -a v<release>
git push origin v<release>
```

- [ ] Set the version of the next iteration and commit the changes:

```bash
./mvnw versions:set -DnewVersion=<next_version>-SNAPSHOT
git commit -a -m "Prepare for next development iteration"
git push origin
```

- [ ] Rebase the release branch into the main branch.
- [ ] Publish the release on GitHub and update the website.
- [ ] Notify the community of the release by sending a message to the mailing list.
- [ ] Clean up all the release candidate branches and tags.

## Reproducing the build

The release artifacts are bit-by-bit reproducible if the following conditions are met:
- The build is run with the same version of the JDK (e.g. OpenJDK 17 temurin)
- The build is run with the maven wrapper (e.g. `./mvnw`)

The procedure has been tested on different operating systems (e.g. Linux and MacOS).
For convenience, we suggest to build the release artifacts on a clean environment (e.g. a fresh Docker container).

```bash
git checkout v<release>-rc<candidate>
docker run \
  -v $(pwd):/baremaps \
  -w /baremaps \
  eclipse-temurin:17-jdk \
  ./mvnw clean install -DskipTests
```

## Verifying the release artifacts

Verify the GPG signature of the release artifacts:

```bash
gpg --verify apache-baremaps-<release>-incubating-bin.tar.gz.asc
gpg --verify apache-baremaps-<release>-incubating-src.tar.gz.asc
```

Verify the SHA512 checksum of the release artifacts:

```bash
shasum -a 512 -c apache-baremaps-<release>-incubating-bin.tar.gz.sha512
shasum -a 512 -c apache-baremaps-<release>-incubating-src.tar.gz.sha512
```

## Vote template

subject: [VOTE] Release Apache Baremaps <release>-rc<candidate> (incubating)

Hello Everyone,

I have created a build for Apache Baremaps (incubating) <release>, release candidate <candidate>.

Thanks to everyone who has contributed to this release.

You can read the release notes here:
https://github.com/apache/incubator-baremaps/releases/tag/v<release>-rc<candidate>

The commit to be voted upon:
https://github.com/apache/incubator-baremaps/tree/v<release>-rc<candidate>

Its hash is <hash>.

Its tag is v<release>-rc<candidate>.

The artifacts to be voted on are located here:
https://dist.apache.org/repos/dist/dev/incubator/baremaps/<release>-rc<candidate>/

The hashes of the artifacts are as follows:
<src>
<bin>

Release artifacts are signed with the following key:
http://people.apache.org/keys/committer/<username>.asc
https://downloads.apache.org/incubator/baremaps/KEYS

The README file for the src distribution contains instructions for building and testing the release.

Please vote on releasing this package as Apache Baremaps <release>.

The vote is open for the next 72 hours and passes if a majority of at least three +1 PMC votes are cast.

[ ] +1 Release this package as Apache Baremaps <release>
[ ] 0 I don't feel strongly about it, but I'm okay with the release
[ ] -1 Do not release this package because...

Here is my vote:

+1 (binding)

<release_manager_name>

## Announce template

subject: [ANNOUNCE] Apache Baremaps <release> (incubating) released

Hello Everyone,

The Apache Baremaps community is pleased to announce the release of Apache Baremaps 0.7.1 (incubating).
Apache Baremaps is a toolkit and a set of infrastructure components for creating, publishing, and operating online maps.

This is our first release since joining the Apache Software Foundation and an important milestone in the project's
history.
Thank you to everyone who contributed to this release and thank you to the mentors for their guidance and support.

We are looking to grow the community and welcome new contributors.
If you are interested in contributing to the project, please contact us on the mailing list or on GitHub.
We will be happy to help you get started.

The release notes are available here:
https://github.com/apache/incubator-baremaps/releases/tag/v<release>

The artifacts are available here:
https://dist.apache.org/repos/dist/release/incubator/baremaps/<release>

The hashes of the artifacts are as follows:
<src>
<bin>

The repository is available here:
https://github.com/apache/incubator-baremaps

The documentation is available here:
https://baremaps.apache.org

The mailing list is available here:
https://lists.apache.org/list.html?dev@baremaps.apache.org

The issue tracker is available here:
https://github.com/apache/incubator-baremaps/issues

Best regards,

<release_manager_name>

