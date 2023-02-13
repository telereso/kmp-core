# Core

[![coverage report](https://telereso.github.io/kmp-core/tests/kover/badge.svg)](https://telereso.github.io/kmp-core/tests/kover) 


# Docs
Each version has it's docs , you can check the latest docs by providing the latest `<version>` to the follwoing url
`https://telereso.github.io/kmp-core/docs/core/<version>/index.html`

The latest version is 

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/telereso/kmp-core)](https://github.com/telereso/kmp-core/releases) 

For example to get docs for version 0.0.4 you can visiting

https://telereso.github.io/kmp-core/docs/core/0.0.4/index.html

# Setup
The following are the first commands to init all the project examples

## Android

Click run `androidApp` will open the android example

## iOS

Run the following command

```shell
cd iosApp && pod install && cd ..
```

Then Open `iosApp` in Xcode or AppScript, and click run button will open ios example

## Web

Run the following command

```shell
cd webApp && yarn && yarn serve
```

Then open `webApp` in intellij Idea or VSCode and check `README.md` file

## ReactNative

Run the following command

```shell
cd react-native-core-client && yarn && yarn bootstrap && yarn example android && yarn example ios
```

then open `react-native-core-client` in intellij Idea or VSCode and check `README.md` file

# Unit Test

## All Tetes

```shell
./gradlew allTests 
```

## iOS

```shell
./gradlew iosSimulatorArm64Test 
```

## Coverage

```shell
./gradlew koverHtmlReport 
```

For more details about contributing check [CONTRIBUTING page](https://github.com/telereso/kmp-core/blob/main/CONTRIBUTING.md)
