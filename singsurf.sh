#! /bin/sh

jep_home=C:/Users/rich/git/jep-java-gpl
javaview_home=C:/Users/rich/bin/javaview

#java -Xmx16g -jar lib/SingSurf.jar

javaw -Xmx16g -cp "bin;$javaview_home/jars/javaview.jar;$javaview_home/jars/jvx.jar;$jep_home/build" org.singsurf.singsurf.SingSurf3D codebase=$javaview_home/ Font=Dialog-14 TextFont=Dialog-17
#javaw -Xmx16g -cp "bin;$javaview_home/jars/javaview.jar;$javaview_home/jars/jvx.jar;$jep_home/build" org.singsurf.singsurf.SingSurf3D codebase=$javaview_home/ 


#C:\Program Files\Java\jdk1.8.0_201\bin\javaw.exe -Dfile.encoding=UTF-8 -classpath "C:\Users\rich\eclipse-workspace\SingSurfGPL\bin;C:\Users\rich\git\jep-java-gpl\lbin;C:\Users\rich\.p2\pool\plugins\org.junit_4.13.0.v20200204-1500.jar;C:\Users\rich\.p2\pool\plugins\org.hamcrest.core_1.3.0.v20180420-1519.jar;C:\Users\rich\bin\javaview\jars\javaview.jar;C:\Users\rich\bin\javaview\jars\jvx.jar" org.singsurf.singsurf.SingSurf3D Font=Dialog-14 TextFont=Dialog-17