#!/bin/bash

PUBLISH_VERSION=${1:-0.0.1}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew preBuild koverHtmlReport dokkaHtml publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION"
#    ./gradlew closeAndReleaseSonatypeStagingRepository
else
#    ./gradlew preBuild koverHtmlReport dokkaHtml -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION"
    ./gradlew preBuild koverHtmlReport dokkaHtml
fi

