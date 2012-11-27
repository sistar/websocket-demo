#!/bin/bash
export JAVA_HOME=/usr/java/jdk1.7.0_09
export PATH=$JAVA_HOME/bin:$PATH
mvn clean install
java -Xmx1024m -jar target/netty-javamagazin-echo-0.1-SNAPSHOT.jar