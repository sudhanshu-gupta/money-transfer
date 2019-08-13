#!/usr/bin/env bash

./mvnw clean package -U
docker-compose up --build