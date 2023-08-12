#!/bin/bash

PUBLISH_VERSION=${1:-"0.0.1"}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew :catalog:publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION"
elif [[ "$@" == *"--local" ]]; then
    ./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" :catalog:publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"
else
    ./gradlew :catalog:publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION-local"
fi

