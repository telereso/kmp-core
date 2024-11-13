# Core Preview

## Deploy Website

```shell
../gradlew :core-preview:wasmJsBrowserDistribution && firebase deploy
```

## Generate Symbols

```shell
../gradlew :core-preview:processMaterialSymbols --no-configuration-cache -PrepoPath=/Users/ahmedalnaami/StudioProjects/multiplatform/material-design-icons
```