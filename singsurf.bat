@echo off
rem # USAGE:    Run
rem #             singsurf.bat 
rem #           from the windows explorer or command line.
rem #
rem # NOTES:    You will need to set the two variables below
rem # 
rem # REQUIRED: It is necessary that Java is installed on your computer.

rem # Set JavaView home directory :
set javaview_home=C:\Users\rich\bin\javaview

rem # set Jep home directory:
set jep_home=C:\Users\rich\git\jep-java-gpl

rem # Optional: To change the font sizes uncomment 

rem # set FONT_DEC=Font=Dialog-17 TextFont=Dialog-17

rem # Optional: memory allocation 
rem # Default is 1GB, to allocate more space use -Xmx8g for 8 GB
set MEM_SPEC=-Xmx1g

rem # No need to edit below this line

set jar=lib/SingSurf.jar
set cp=classes;%javaview_home%\jars\javaview.jar;%javaview_home%\jars\jvx.jar;%jep_home%\build

rem # Append trailing path separator to codebase param
set jv_cb=%javaview_home%\

rem # Launch SingSurf, should open a 3d display.

if exist %jar% (
    echo "Using Jar file"
	java -jar %jar% %MEM_SPEC% codebase=%jv_cb% %FONT_DEC%
) else (
    echo "Using class files"
    java -cp "%cp%" %MEM_SPEC% org.singsurf.singsurf.SingSurf3D codebase=%jv_cb%  %FONT_DEC%
)

