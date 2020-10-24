#! /bin/sh
# Runs the singsurf mathematical visualisation program
# Either set the JEP_HOME and JAVAVIEW_HOME environment variable or set them below.

# Set the two variables for the jep and javaview home directories

if [[ -z "${JEP_HOME}" ]]; then
  jep_home=C:/Users/rich/git/jep-java-gpl
else
  jep_home="${JEP_HOME}"
fi

if [[ -z "${JAVAVIEW_HOME}" ]]; then
  javaview_home=C:/Users/rich/bin/javaview
else
  javaview_home="${JAVAVIEW_HOME}"
fi

# Specify fonts 
FONT_SPEC="Font=Dialog-14 TextFont=Dialog-17"

# Optional: memory allocation 
# Default is 1GB, to allocate more space use -Xmx8g for 8 GB
MEM_SPEC=-Xmx1g

# Whether to use the jar file if it exists
use_jar=true

if $use_jar  && [ -f lib/SingSurf.jar ]  ; then
   echo "SingSurf.jar found. Running jar version"
   java $MEM_SPEC -jar lib/SingSurf.jar codebase=$javaview_home/ $FONT_SPEC
else
   echo "SingSurf.jar not found. Running class version "
   java $MEM_SPEC -cp "classes;$javaview_home/jars/*;$jep_home/build" org.singsurf.singsurf.SingSurf3D codebase=$javaview_home/ $FONT_SPEC
fi
