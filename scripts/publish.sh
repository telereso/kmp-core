#!/bin/bash

PUBLISH_VERSION=${1:-0.0.1}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew preBuild publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION"
#    ./gradlew closeAndReleaseSonatypeStagingRepository
else
    ./gradlew preBuild koverHtmlReport -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"
fi

