#!/bin/sh


sudo docker build -t registry.applifier.info:5000/unity-ads-android-test-server:latest .

sudo docker stop unity-ads-android-test-server
sudo docker rm unity-ads-android-test-server

sudo docker run -d --restart=always --name unity-ads-android-test-server -p 18080:8080 registry.applifier.info:5000/unity-ads-android-test-server:latest
