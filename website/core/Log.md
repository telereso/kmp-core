---
layout: default
title: Log
parent: Core
nav_order: 6
---

# [Log](https://kmp.telereso.io/docs/core/latest/-core/io.telereso.kmp.core/-log/index.html){:target="_blank"}

Handy logging util class while building the sdk 

## Log Anywhere 
if you need to log any event at any level just use the following extensions

* logDebug() <br>
  Takes a message
* logInfo() <br>
  Takes a message
* logError() <br>
  Takes a message and a throwable

These functions will use the closest class name as a tage and print the message the proper logging level

## Log with tag
if you need to log everything under one tag you can use the following set of functions 

* Log.d() <br>
  Takes a `Tag` and a message
* Log.i() <br>
  Takes a `Tag` and a message
* Log.e() <br>
  Takes a `Tag` , a message and a throwable
