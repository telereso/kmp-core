---
title: Home
layout: home
has_toc: true
nav_order: 1
---

# About

KMP stands for Kotlin Multiplatform.

KMP is a feature of Kotlin that allows developers to write code that can be shared across multiple platforms, such as Android, iOS, and the web. With Kotlin Multiplatform, developers can use a single codebase to create apps for different platforms, which can save time and effort.

This project provides a structure that will allow you to create SDKs in a scalable way to use on all your platforms (backend, frontend). 

This will be useful for you if you already have your standalone projects for each platform and you want to share common code between them without starting from scratch, or if you want to build universal SDKs for the supported platforms.

---

# Platforms

* Android
* iOS
* ReactNative
* Flutter (coming soon)
* Jvm
* NodeJs

---

## Contributing

Please help us grow this project 😃 ,

If you have an idea or wanna fix a bug , feel free to open an issue.


### Thank you for the contributors!

<ul class="list-style-none">
{% for contributor in site.github.contributors %}
  <li class="d-inline-block mr-1">
     <a href="{{ contributor.html_url }}"><img src="{{ contributor.avatar_url }}" width="32" height="32" alt="{{ contributor.login }}"></a>
  </li>
{% endfor %}
</ul>

For more details about contributing
check the [CONTRIBUTING](https://github.com/telereso/kmp-core/blob/main/CONTRIBUTING.md) page.