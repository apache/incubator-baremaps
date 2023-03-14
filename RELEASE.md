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

- [ ] Publish the artifacts:

```bash
svn co https://dist.apache.org/repos/dist/release/incubator/baremaps/ baremaps-release
mkdir baremaps-release/<version>
cp ./baremaps-cli/target/baremaps-<version>-incubating-* baremaps-release/<version>/.
svn commit -m "Baremaps <version>"

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

## Email template

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

[Release manager name]
