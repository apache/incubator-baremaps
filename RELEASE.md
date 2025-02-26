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
export RELEASE_VERSION=<release_version> # e.g. 0.7.1
export NEXT_VERSION=<next_version> # e.g. 0.7.2
export CANDIDATE_NUMBER=<candidate_number> # e.g. 1
export RELEASE_MANAGER_NAME=<release_manager_name> # e.g. John Doe
export COMMIT_HASH=<commit_hash> # e.g. 1234567890
```

In order to release a new version of Apache Baremaps, follow these steps:

- [ ] Notify the mailing list and ask everyone to pause commits on the main branch
- [ ] Create a new issue on GitHub with the title "Release Baremaps $RELEASE_VERSION"
- [ ] Create a new branch for the release (e.g. `release-$RELEASE_VERSION`)

```bash
cd baremaps
git checkout -b release-$RELEASE_VERSION
```

- [ ] Set the release version and commit the changes:

```bash 
./mvnw versions:set -DnewVersion=$RELEASE_VERSION -DgenerateBackupPoms=false
git commit -a -m "Release Baremaps $RELEASE_VERSION"
git push --set-upstream origin release-$RELEASE_VERSION
```

- [ ] Tag the last commit with the release candidate version:

```bash
git tag v$RELEASE_VERSION-rc$CANDIDATE_NUMBER
```

- [ ] Push the tag to the remote repository (this will trigger GitHub Action to build the release candidate, publish the artifacts to the [dev directory](https://dist.apache.org/repos/dist/dev/incubator/baremaps/) of dist.apache.org repository, and draft a release on GitHub):

```bash
git push origin v$RELEASE_VERSION-rc$CANDIDATE_NUMBER
```

- [ ] Edit the release notes for this tag on GitHub.
- [ ] Ask the community to vote for the release candidate.
- [ ] If the release candidate is not approved by the community, commit the necessary changes, clean the git history, create a new release candidate, and repeat the process.

> TODO: The following step is not yet fully automated. We need to add secrets and steps to publish the artifacts to the [dev directory](https://dist.apache.org/repos/dist/dev/incubator/baremaps/) (APACHE_USERNAME, APACHE_PASSWORD) and to the maven repository (NEXUS_USERNAME, NEXUS_PASSWORD).

- [ ] If the release candidate is approved by the community, tag the release commit with the release version (this will trigger the same GitHub Action as before):

```bash
git tag -a v$RELEASE_VERSION
git push origin v$RELEASE_VERSION
```

- [ ] Register the release on [reporter.apache.org](https://reporter.apache.org/addrelease.html?incubator-baremaps).
- [ ] Rebase the release branch into the main branch.
- [ ] Clean up all the release candidate branches and tags.
- [ ] Publish the release on GitHub.
- [ ] Copy the release artifacts from the [dev directory](https://dist.apache.org/repos/dist/dev/incubator/baremaps/) to the [release directory](https://dist.apache.org/repos/dist/release/incubator/baremaps/).

```bash
svn cp https://dist.apache.org/repos/dist/dev/incubator/baremaps/$RELEASE_VERSION-rc$CANDIDATE_NUMBER https://dist.apache.org/repos/dist/release/incubator/baremaps/$RELEASE_VERSION -m "Release Apache Baremaps (incubating) $RELEASE_VERSION"
```

- [ ] Publish the release artifacts to the maven repository.

```bash
./mvnw clean deploy -Papache-release
```

- [ ] Set the version of the next iteration and commit the changes:

```bash
./mvnw versions:set -DnewVersion=$NEXT_VERSION-SNAPSHOT -DgenerateBackupPoms=false
git commit -a -m "Prepare for next development iteration"
git push origin
```

```bash
./mvnw clean deploy
```

- [ ] Notify the community of the release by sending a message to the mailing list.

## Reproducing the build

The release artifacts are bit-by-bit reproducible if the following conditions are met:
- The build is run with the same version of the JDK (e.g. OpenJDK 17 temurin)
- The build is run with the maven wrapper (e.g. `./mvnw`)

The procedure has been tested on different operating systems (e.g. Linux and MacOS).
For convenience, we suggest to build the release artifacts on a clean environment (e.g. a fresh Docker container).

```bash
git checkout v$RELEASE_VERSION-rc$CANDIDATE_NUMBER
docker run \
  -v $(pwd):/baremaps \
  -w /baremaps \
  eclipse-temurin:17-jdk \
  ./mvnw clean verify -DskipTests artifact:compare -Dreference.repo=https://repository.apache.org/content/repositories/staging/
```

## Verifying the release artifacts

Verify the GPG signature of the release artifacts:

```bash
gpg --verify apache-baremaps-$RELEASE_VERSION-incubating-bin.tar.gz.asc
gpg --verify apache-baremaps-$RELEASE_VERSION-incubating-src.tar.gz.asc
```

Verify the SHA512 checksum of the release artifacts:

```bash
shasum -a 512 -c apache-baremaps-$RELEASE_VERSION-incubating-bin.tar.gz.sha512
shasum -a 512 -c apache-baremaps-$RELEASE_VERSION-incubating-src.tar.gz.sha512
```

## Vote template

```bash
cat << EOF
subject: [VOTE] Release Apache Baremaps $RELEASE_VERSION-rc$CANDIDATE_NUMBER (incubating)

Hello Everyone,

I have created a build for Apache Baremaps (incubating) $RELEASE_VERSION, release candidate $CANDIDATE_NUMBER.

Thanks to everyone who has contributed to this release.

You can read the release notes here:
https://github.com/apache/incubator-baremaps/releases/tag/v$RELEASE_VERSION-rc$CANDIDATE_NUMBER

The commit to be voted upon:
https://github.com/apache/incubator-baremaps/tree/v$RELEASE_VERSION-rc$CANDIDATE_NUMBER

Its hash is $COMMIT_HASH.

Its tag is v$RELEASE_VERSION-rc$CANDIDATE_NUMBER.

The artifacts to be voted on are located here:
https://dist.apache.org/repos/dist/dev/incubator/baremaps/$RELEASE_VERSION-rc$CANDIDATE_NUMBER/

The hashes of the artifacts are as follows:
<src>
<bin>

Release artifacts are signed with the following key:
http://people.apache.org/keys/committer/<username>.asc
https://downloads.apache.org/incubator/baremaps/KEYS

The README file for the src distribution contains instructions for building and testing the release.

Please vote on releasing this package as Apache Baremaps $RELEASE_VERSION.

The vote is open for the next 72 hours and passes if a majority of at least three +1 PMC votes are cast.

[ ] +1 Release this package as Apache Baremaps $RELEASE_VERSION
[ ] 0 I don't feel strongly about it, but I'm okay with the release
[ ] -1 Do not release this package because...

Here is my vote:

+1 (binding)

$RELEASE_MANAGER_NAME
EOF
```

## Announce template

```bash
cat << EOF
subject: [ANNOUNCE] Apache Baremaps $RELEASE_VERSION (incubating) released

Hello Everyone,

The Apache Baremaps community is pleased to announce the release of Apache Baremaps $RELEASE_VERSION (incubating).
Apache Baremaps is a toolkit and a set of infrastructure components for creating, publishing, and operating online maps.
<short description of the release which should include release highlights>

The release notes are available here:
https://github.com/apache/incubator-baremaps/releases/tag/v$RELEASE_VERSION

The artifacts are available here:
https://dist.apache.org/repos/dist/release/incubator/baremaps/$RELEASE_VERSION

We are looking to grow our community and welcome new contributors.
If you are interested in contributing to the project, please contact us on the mailing list or on GitHub.
We will be happy to help you get started.

The repository is available here:
https://github.com/apache/incubator-baremaps

The documentation is available here:
https://baremaps.apache.org

The mailing list is available here:
https://lists.apache.org/list.html?dev@baremaps.apache.org

The issue tracker is available here:
https://github.com/apache/incubator-baremaps/issues

Best regards,

$RELEASE_MANAGER_NAME
EOF
```

