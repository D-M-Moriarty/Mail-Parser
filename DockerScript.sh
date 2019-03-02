#!/usr/bin/env bash
mvn clean install -Dmaven.test.skip=true
docker build -f Dockerfile -t pbm .
docker tag pbm dmoriarty/pbm
docker push dmoriarty/pbm

