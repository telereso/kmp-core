#!/bin/bash

scriptDir=$(dirname "$0")

# run the following before running this script


PUBLISH_VERSION="${DEPLOY_VERSION:1}${CI_BUILD_NUMBER}"

./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"
./gradlew dokkaHtml "-PpublishVersion=$PUBLISH_VERSION"
#./gradlew publish "-PpublishVersion=$PUBLISH_VERSION" "-PartifactoryUrl=$JF_HOST" "-PartifactoryUser=$JF_USER" "-PartifactoryPassword=$JF_PASS"
