#!/usr/bin/env bash
mvn clean install -Dmaven.test.skip=true
docker build -f Dockerfile -t pbm .
docker tag pbm ec2-13-58-154-84.us-east-2.compute.amazonaws.com:5000/pbm
docker push ec2-13-58-154-84.us-east-2.compute.amazonaws.com:5000/pbm

