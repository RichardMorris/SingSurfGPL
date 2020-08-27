#! /bin/sh
java -Xmx16g -cp "lib/javaview.jar;lib/jep.jar;lib/jepext.jar;lib/jvx.jar;lib/SingSurf.jar" org.singsurf.singsurf.ASurfCL  "$1" "$2" "$3"
