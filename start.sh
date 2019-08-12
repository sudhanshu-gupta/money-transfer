#!/usr/bin/env bash

mvn clean package -U -DskipTests
docker-compose up --build