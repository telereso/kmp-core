---
title: Home
layout: home
has_toc: true
nav_order: 1
---

# About

KMP stands fo kotlin multiplatfom ,

This project provide a structure that will allow you to create sdks in scalable way to use in all
your platforms (backend , frontend)

This will be useful for you in case you already have your own stand alone projects for each platform
and you want to share common code between them without starting from scratch
Or you wanna build universal sdks for the supported platforms

---

# Platforms

* Android
* iOS
* ReactNative
* Flutter (soon)
* Jvm
* NodeJs

---

## Contributing

Please help use grow this project 😃 ,

If you have an idea or wanna fix a a bug , feel free to open an issue

### Thank you for the contributors!

<ul class="list-style-none">
{% for contributor in site.github.contributors %}
  <li class="d-inline-block mr-1">
     <a href="{{ contributor.html_url }}"><img src="{{ contributor.avatar_url }}" width="32" height="32" alt="{{ contributor.login }}"></a>
  </li>
{% endfor %}
</ul>

For more details about contributing
check [CONTRIBUTING page](https://github.com/telereso/kmp-core/blob/main/CONTRIBUTING.md)