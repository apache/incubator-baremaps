# Release instructions

In order to release a new version of baremaps, follow these steps:

- [ ] Notify the community of the upcoming release by creating a new issue on GitHub.
- [ ] Notify the community by sending a message on the mailing list and ask to pause commits on the main branch.
- [ ] Create a new branch for the release candidate:

```bash
git checkout -b v[version]-rc[n]
```

- [ ] Set the release version and commit the changes:

```bash 
mvn versions:set -DnewVersion=[version]
git commit -a -m "Release Baremaps [version]"
```

- [ ] Tag the commit with the release candidate version:

```bash
git tag -a v[version]-rc[n]
```

- [ ] Push the branch and the tag to the remote repository:

```bash
git push origin v[version]-rc[n]
```

- [ ] Generate the GPG and SHA512 signatures of the release candidate (instructions below).
- [ ] Generate the release notes and attach them to the release candidate on GitHub.
- [ ] Ask the community to vote for the release candidate.
- [ ] If the release candidate is not approved by the community, go back to step 3 and perform the necessary changes.
- [ ] Otherwise, tag the commit with the release version:

```bash
git tag -a v[version]
git push origin v[version]
```

- [ ] Set the next development version and commit the changes:

```bash
mvn versions:set -DnewVersion=[next_version]-SNAPSHOT
git commit -a -m "Prepare for next development iteration"
git push origin
```

- [ ] Merge the accepted release candidate branch into the main branch.
- [ ] Notify the community of the release by sending a message on the mailing list.
- [ ] Clean up all the release candidate branches and tags.

## Signing and verifying releases with GPG

The following command signs the release with GPG:

```bash
gpg --no-tty --pinentry-mode loopback --batch --yes --passphrase "$GPG_PASSPHRASE" --output "baremaps-[version]-incubating-[src|bin].zip.asc" --detach-sign --armor "baremaps-[version]-incubating-[src|bin].zip"
```

The following command verifies the release with GPG:

```bash
gpg --verify baremaps-[version]-incubating-[src|bin].zip.asc
```

## Signing and verifying releases with SHA512

The following command signs the release with SHA512:

```bash
shasum -a 512 -c baremaps-[version]-incubating-[src|bin].zip.sha512
```

The following command verifies the release with SHA512:

```bash
gpg --verify baremaps-[version]-incubating-[src|bin].zip.asc
```

## Email template

Hello Baremaps Community,

This is a call for a vote to the 1st release candidate for Apache Baremaps,
version [version]-incubating.

We request project mentors (binded) as well as all contributors (unbinded)
and users to review and vote on this incubator release.

The commit to be voted upon:
[url]

The full list of changes and release notes are available at:
[url]

Best regards,

[Release manager name]


