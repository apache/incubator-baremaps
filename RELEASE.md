<!--
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
  -->
# Release instructions

In order to release a new version of Apache Baremaps, follow these steps:

- [ ] Notify the mailing list and ask everyone to pause commits on the main branch
- [ ] Create a new issue on GitHub with the title "Release Baremaps <version>"
- [ ] Create a new branch for the release (e.g. `release-<version>`)

```bash
git checkout -b release-<version>
git push --set-upstream origin release-<version>
```

- [ ] Set the release version and commit the changes:

```bash 
mvn versions:set -DnewVersion=<version>
git commit -a -m "Release Baremaps <version>"
```

- [ ] Tag the last commit with the release candidate version:

```bash
git tag v<version>-rc<number>
```

- [ ] Push the tag to the remote repository:

```bash
git push origin v<version>-rc<number>
```

- [ ] Generate the release notes for this tag on GitHub.
- [ ] Generate the artifacts:

```bash
./scripts/generate-artifacts.sh
```

- [ ] Publish the artifacts:

```bash
svn co https://dist.apache.org/repos/dist/dev/incubator/baremaps/ baremaps-dev
mkdir baremaps-dev/<version>-rc<number>
cp ./baremaps-cli/target/baremaps-<version>-incubating-* baremaps-dev/<version>-rc<number>/.
svn commit -m "Baremaps <version>-rc<number>"
```

- [ ] Ask the community to vote for the release candidate.
- [ ] If the release candidate is not approved by the community, commit the necessary changes, clean the git history, and go back to step 5.
- [ ] If the release candidate is approved by the community, tag the release commit with the release version:

```bash
git tag -a v[version]
git push origin v[version]
```

- [ ] Move the artifacts into the release directory with svn:

```bash
svn mv -m "Baremaps <version>" \
  https://dist.apache.org/repos/dist/dev/incubator/baremaps/<version>-rc<number>/ \
  https://dist.apache.org/repos/dist/release/incubator/baremaps/<version>/
```

- [ ] Set the version of the next iteration and commit the changes:

```bash
mvn versions:set -DnewVersion=[next_version]-SNAPSHOT
git commit -a -m "Prepare for next development iteration"
git push origin
```

- [ ] Rebase the release branch into the main branch.
- [ ] Notify the community of the release by sending a message to the mailing list.
- [ ] Clean up all the release candidate branches and tags.

## Verifying the release artifacts

Verify the GPG signature of the release artifacts:

```bash
gpg --verify baremaps-<version>-incubating-bin.tar.gz.asc
gpg --verify baremaps-<version>-incubating-src.tar.gz.asc
```

Verify the SHA512 checksum of the release artifacts:

```bash
shasum -a 512 -c baremaps-<version>-incubating-bin.tar.gz.sha512
shasum -a 512 -c baremaps-<version>-incubating-src.tar.gz.sha512
```

## Vote template

subject: [VOTE] Release Apache Baremaps <version>-rc<number> (incubating)

Hello Everyone,

I have created a build for Apache Baremaps (incubating) <version>, release candidate <number>.

Thanks to everyone who has contributed to this release.

You can read the release notes here:
https://github.com/apache/incubator-baremaps/releases/tag/v<version>-rc<number>

The commit to be voted upon:
https://github.com/apache/incubator-baremaps/tree/v<version>

Its hash is <hash>.

Its tag is v<version>-rc<number>.

The artifacts to be voted on are located here:
https://dist.apache.org/repos/dist/release/incubator/baremaps/<version>-rc<number>/

The hashes of the artifacts are as follows:
<src>
<bin>

Release artifacts are signed with the following key:
http://people.apache.org/keys/committer/<username>.asc
https://downloads.apache.org/incubator/baremaps/KEYS

The README file for the src distribution contains instructions for building and testing the release.

Please vote on releasing this package as Apache Baremaps <version>.

The vote is open for the next 72 hours and passes if a majority of at least three +1 PMC votes are cast.

[ ] +1 Release this package as Apache Baremaps <version>
[ ] 0 I don't feel strongly about it, but I'm okay with the release
[ ] -1 Do not release this package because...

Here is my vote:

+1 (binding)

<release_manager_name>


## Announce template

subject: [ANNOUNCE] Apache Baremaps <version> (incubating) released

Hello Everyone,

The Apache Baremaps community is pleased to announce the release of Apache Baremaps 0.7.1 (incubating).
Apache Baremaps is a toolkit and a set of infrastructure components for creating, publishing, and operating online maps.

This is our first release since joining the Apache Software Foundation and an important milestone in the project's history.
Thank you to everyone who contributed to this release and thank you to the mentors for their guidance and support.

We are looking to grow the community and welcome new contributors.
If you are interested in contributing to the project, please contact us on the mailing list or on GitHub.
We will be happy to help you get started.

The release notes are available here:
https://github.com/apache/incubator-baremaps/releases/tag/v<version>

The artifacts are available here:
https://dist.apache.org/repos/dist/release/incubator/baremaps/<version>

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

