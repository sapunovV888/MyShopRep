#!/bin/bash

echo "\uD83D\uDD0D Запуск повної перевірки..."

mvn clean verify checkstyle:check spotbugs:check

if [ $? -eq 0 ]; then
  echo "\u2705 Усі перевірки пройдено успішно!"
else
  echo "\u274C Виявлено помилки!"
  exit 1
fi
