#!/usr/bin/env bash

# fix 'gpg: signing failed: inappropriate ioctl for device'
GPG_TTY=$(tty)
export GPG_TTY

ARTIFACT_VERSION=$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version -q -DforceStdout)
GROUP_ID=$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.groupId -q -DforceStdout)
ARTIFACT_ID=$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.artifactId -q -DforceStdout)

mvn versions:set -DnewVersion="${ARTIFACT_VERSION}" -DgenerateBackupPoms=false
./mvnw -Possrh clean deploy -DskipTests -DaltDeploymentRepository=ossrh::file:./target/.m2

CURRENT_DIR=$(pwd)
GROUP_PATH=$(echo "${GROUP_ID}" | sed 's/\./\//g' )
cd "./target/.m2/${GROUP_PATH}/${ARTIFACT_ID}/${ARTIFACT_VERSION}/" || exit

jar -cvf "${CURRENT_DIR}/target/auto-semver-${ARTIFACT_VERSION}-bundle.jar" ./*.{asc,md5,sha1,pom,jar}
