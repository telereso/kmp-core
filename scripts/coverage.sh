#!/bin/bash

classCoverage="$(cat lib/build/reports/kover/xml/report.xml | grep -o '<counter type="LINE.*/>' | tail -n 1)"

missedPrefix='<counter type="LINE" missed="'
missedSuffix='" covered='
missed=${classCoverage#"$missedPrefix"}
missed=${missed%"$missedSuffix"*}

coveredPrefix='covered="'
coveredSuffix='"/>'
covered=${classCoverage#*"$coveredPrefix"}
covered=${covered%"$coveredSuffix"}

total=$((missed + covered))

coverage=$((covered * 100 / total))

if [ -z "$coverage" ]; then
  badgePath="unknown-inactive"
else
  badgePath="$coverage%25"

  if (( coverage >= 90 )); then
    badgePath="$badgePath-brightgreen"
  elif (( coverage >= 80 )); then
    badgePath="$badgePath-green"
  elif (( coverage >= 70 )); then
    badgePath="$badgePath-yellowgreen"
  elif (( coverage >= 50 )); then
    badgePath="$badgePath-yellow"
  elif (( coverage >= 40 )); then
    badgePath="$badgePath-orange"
  else
    badgePath="$badgePath-red"
  fi
fi

echo "Total: $coverage%"

echo "Badge: https://img.shields.io/badge/coverage-$badgePath"
