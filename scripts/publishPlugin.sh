#!/bin/bash

PUBLISH_VERSION=${1:-"0.0.1"}

if [[ "$@" == *"--release" ]]; then
    ./gradlew :gradle-plugin:publishPlugins "-PpublishVersion=$PUBLISH_VERSION" "-PpublishGradlePlugin=true"
elif [[ "$@" == *"--local" ]]; then
    ./gradlew -Dmaven.repo.local="$(pwd)/build/.m2/repository" :gradle-plugin:publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION" "-PpublishGradlePlugin=true"
else
    ./gradlew :gradle-plugin:publishToMavenLocal "-PpublishVersion=$PUBLISH_VERSION-local" "-PpublishGradlePlugin=true"
fi

