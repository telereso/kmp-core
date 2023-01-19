# core-client

[![pipeline status](https://gitlab.com/airasia-superapp/travel/mobile/multiplatform/core-client/badges/main/pipeline.svg)](https://gitlab.com/airasia-superapp/travel/mobile/multiplatform/core-client/-/commits/main) [![coverage report](https://gitlab.com/airasia-superapp/travel/mobile/multiplatform/core-client/badges/main/coverage.svg)](https://gitlab.com/airasia-superapp/travel/mobile/multiplatform/core-client/-/commits/main)
[![Latest Release](https://gitlab.com/airasia-superapp/travel/mobile/multiplatform/core-client/-/badges/release.svg)](https://gitlab.com/airasia-superapp/travel/mobile/multiplatform/core-client/-/releases)

# Docs
To check latest stable version [visit here](https://airasia-superapp.gitlab.io/travel/mobile/multiplatform/core-client/latest)

To read docs for a specific release visit below link after updating `<version>`
```
https://airasia-superapp.gitlab.io/travel/mobile/multiplatform/core-client/<version>
```



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


