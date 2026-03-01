#!/bin/bash
cd backend
mvn clean package
java -jar target/websocket-0.0.1-SNAPSHOT.jar