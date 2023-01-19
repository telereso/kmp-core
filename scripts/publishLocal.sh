#!/bin/bash

scriptDir=$(dirname "$0")

# run the following before running this script
export JF_HOST='https://airasia.jfrog.io/artifactory'
export JF_USER="<>"
export JF_PASS="<>"
export DEPLOY_VERSION="v0.0.1"
export ARTIFACT="core-client"


PUBLISH_VERSION="${DEPLOY_VERSION:1}${CI_BUILD_NUMBER}"

./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"
./gradlew dokkaHtml "-PpublishVersion=$PUBLISH_VERSION"
#./gradlew publish "-PpublishVersion=$PUBLISH_VERSION" "-PartifactoryUrl=$JF_HOST" "-PartifactoryUser=$JF_USER" "-PartifactoryPassword=$JF_PASS"
