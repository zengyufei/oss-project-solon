#!/bin/bash

docker rm -f oss
docker rmi solon:v1
docker build -t solon:v1 -f dockerfile .
#docker run -it --name solon -p 5555:8081  -d solon:v1 
docker-compose down
docker-compose up -d
