#!/bin/bash

# Prompt the user to enter his GPG key ID
echo "Please select your GPG key ID:"
read JRELEASER_GPG_KEY_ID
export JRELEASER_GPG_KEY_ID

# Prompt the user to enter his GPG key passphrase
echo "Please enter your GPG passphrase:"
read -s JRELEASER_GPG_PASSPHRASE
export JRELEASER_GPG_PASSPHRASE
echo "************"

# Prompt the user to enter his Github token
echo "Please enter your Github token:"
read -s JRELEASER_GITHUB_TOKEN
export JRELEASER_GITHUB_TOKEN
echo "************"

# Add the GPG public and private keys to environment variables
export JRELEASER_GPG_PUBLIC_KEY=$(gpg --export --armor $JRELEASER_GPG_KEY_ID)
export JRELEASER_GPG_PRIVATE_KEY=$(gpg --batch --pinentry-mode=loopback --yes --passphrase $JRELEASER_GPG_PASSPHRASE --export-secret-key --armor $JRELEASER_GPG_KEY_ID)

# Extract the release name from the pom.xml file
RELEASE="baremaps-$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)-incubating"

# Package the application and skip tests
mvn clean package -DskipTests

# Unzip the release artifacts
unzip -o baremaps-cli/target/$RELEASE.zip

# Move the release to a directory ignored by git and without the version number.
# This directory can be added permanently to the PATH environment variable to test the release.
mv $RELEASE baremaps

# Add the release to the PATH
export PATH=$PATH:$PWD/baremaps/bin

# Display the version of the release
baremaps --version

# Sign the release artifacts
mvn jreleaser:full-release
