#!/bin/bash
oldpath=`pwd`
cd $1
java -jar ../../libs/jnaerator-0.13-20150328.111636-4-shaded.jar -library ml.rabidbeaver.cupsjni ../cups-2.0.2/cups/cups.h -o ../../libs/ -v -runtime JNAerator -mode StandaloneJar 2>&1
cd $oldpath
