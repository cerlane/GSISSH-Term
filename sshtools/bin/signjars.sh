#!/bin/bash
for i in *.jar; do echo $i && $JAVA_HOME/bin/jarsigner -keystore $1 -storepass $3 $i $2; done
