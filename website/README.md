---
title: Home
layout: home
has_toc: true
nav_order: 1
---

# About

KMP stands kotlin multiplatfom , 

This project provide a structure that will allow you to create sdks in scalable way to use in all your platforms (backend , frontend)

# Platforms

* Android
* iOS
* ReactNative
* Flutter (soon)
* Jvm
* NodeJs

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


[//]: # ()
[//]: # (This is a *bare-minimum* template to create a Jekyll site that uses the [Just the Docs] theme. You can easily set the created site to be published on [GitHub Pages] – the [README] file explains how to do that, along with other details.)

[//]: # ()
[//]: # (If [Jekyll] is installed on your computer, you can also build and preview the created site *locally*. This lets you test changes before committing them, and avoids waiting for GitHub Pages.[^1] And you will be able to deploy your local build to a different platform than GitHub Pages.)

[//]: # ()
[//]: # (More specifically, the created site:)

[//]: # ()
[//]: # (- uses a gem-based approach, i.e. uses a `Gemfile` and loads the `just-the-docs` gem)

[//]: # (- uses the [GitHub Pages / Actions workflow] to build and publish the site on GitHub Pages)

[//]: # ()
[//]: # (Other than that, you're free to customize sites that you create with this template, however you like. You can easily change the versions of `just-the-docs` and Jekyll it uses, as well as adding further plugins.)

[//]: # ()
[//]: # ([Browse our documentation][Just the Docs] to learn more about how to use this theme.)

[//]: # ()
[//]: # (To get started with creating a site, just click "[use this template]"!)

[//]: # ()
[//]: # (----)

[//]: # ()
[//]: # ([^1]: [It can take up to 10 minutes for changes to your site to publish after you push the changes to GitHub]&#40;https://docs.github.com/en/pages/setting-up-a-github-pages-site-with-jekyll/creating-a-github-pages-site-with-jekyll#creating-your-site&#41;.)

[//]: # ()
[//]: # ([Just the Docs]: https://just-the-docs.github.io/just-the-docs/)

[//]: # ([GitHub Pages]: https://docs.github.com/en/pages)

[//]: # ([README]: https://github.com/just-the-docs/just-the-docs-template/blob/main/README.md)

[//]: # ([Jekyll]: https://jekyllrb.com)

[//]: # ([GitHub Pages / Actions workflow]: https://github.blog/changelog/2022-07-27-github-pages-custom-github-actions-workflows-beta/)

[//]: # ([use this template]: https://github.com/just-the-docs/just-the-docs-template/generate)