#!/bin/bash

PUBLISH_VERSION=${1:-0.0.1}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew koverReport dokkaHtml publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION"
#    ./gradlew closeAndReleaseSonatypeStagingRepository
elif [[ "$@" == *"--local" ]]; then
    ./gradlew koverReport dokkaHtml -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"
else
    ./gradlew publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION-local"
    echo "🏗 New Local version ready! : $PUBLISH_VERSION-local"
fi

