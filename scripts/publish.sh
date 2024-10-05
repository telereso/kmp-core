#!/bin/bash

PUBLISH_VERSION=${1:-0.0.1}

if [[ "$@" == *"--sonatype" ]]; then
    ./gradlew koverXmlReport koverHtmlReport koverPrintCoverage dokkaHtml publishAllPublicationsToSonatypeRepository "-PpublishVersion=$PUBLISH_VERSION" \
    -x catalog:publishToMavenLocal --no-configuration-cache --no-parallel
#    ./gradlew closeAndReleaseSonatypeStagingRepository
elif [[ "$@" == *"--local" ]]; then
    ./gradlew koverXmlReport koverHtmlReport koverPrintCoverage  dokkaHtml -Dmaven.repo.local="$(pwd)/build/.m2/repository" publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION" \
    -x catalog:publishToMavenLocal --no-configuration-cache
else
    ./gradlew publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION-local" \
    -x catalog:publishToMavenLocal --no-configuration-cache
    echo "published $PUBLISH_VERSION-local !"
fi

