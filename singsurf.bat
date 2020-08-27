@echo off
rem # USAGE:    Run
rem #             singsurf.bat 
rem #           from the windows explorer or command line.
rem #
rem # NOTES:    - You may need to edit the variables below
rem #           - Optionally, put a link to this BAT file on your desktop,
rem #             and in the property panel of the link adjust the directory "Start in .."
rem #             to the SingSurf base directory such as "c:\program files\SingSurf\".
rem # 
rem # REQUIRED: It is necessary that Java is installed on your computer. Otherwise
rem #           download and install the latest Java version from Oracle (www.java.com).
rem # SET CODEBASE (no need to edit):
rem # 
rem # The default codebase folder is the directory of this file.
rem #  javaview /rsrc and /image directories will be here.
set jv_cb="."

rem # SET JAR LIBRARY (optionally edit):
rem # 
set jar=%jv_cb%/lib/SingSurf.jar

rem # Append trailing path separator to codebase
set jv_cb=%jv_cb%\

rem # LAUNCH IN NORMAL MODE:
rem # 
rem # Launch SingSurf, should open a 3d display.
rem # It is assumed that Java is installed on your computer. Otherwise
rem # download and install the latest Java version from http://www.java.com.
rem # Here Java's default memory size is increased to 1GB using the -Xmx parameter.

start javaw -jar %jar% -Xmx1024m codebase=%jv_cb%

rem # LAUNCH IN ALTERNATE MODE with more memory:
rem # 
rem # The following alternative command increases the memory available to the Java virtual machine
rem # and should be used if Java runs out of memory during a SingSurf session.
rem # Here Java's default memory size is increased to 6GB. Adjust to your hardware capacitiy.
rem # start javaw -jar %jar% -Xmx6144m codebase=%jv_cb%