#!/bin/bash

PUBLISH_VERSION=${1:-"0.0.1"}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew :catalog:publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION" --no-configuration-cache --no-parallel
elif [[ "$@" == *"--local" ]]; then
    ./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" :catalog:publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION" --no-configuration-cache
else
    ./gradlew :catalog:publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION-local" --no-configuration-cache
fi

