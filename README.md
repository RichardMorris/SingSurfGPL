# SingSurf
A mathematical curve and surface visualiser for singular surface and objects from Singularity Theory.

The program can calculate many of the objects found in Singularity theory and geometry:

* **Algebraic curves** defined by a single polynomial equation in two variables. e.g. electric motor 
`y^2(y^2-9)-x^2(x^2-10);`

![Electric motor](doc/electric_motor.png)
* **Algebraic surfaces** defined by a single polynomial equation in three variables. e.g. a Chubs surface
`x^4 + y^4 + z^4 - x^2 - y^2 - z^2 + 0.5;`
            
![Chubs surface](doc/chubs_surface.png)
* **Parameterised curves** defined by a 3D vector expression in a single variable. e.g. a helix
`[cos(pi t), sin(pi t), t];`    
![Helix](doc/helix.png)
* **Parameterised surfaces** defined by a 3D vector expression in two variables. e.g. a cross-cap
`[x,x y,y^2]`          
![Cross cap](doc/cross_cap.png)

* **Intersection** of surfaces with sets defined by another equation. 
For example the intersection of a conical surface with the set defined by a plane `a x b y + cz =d`.

![Intersection of a cone with a plane](doc/cone_intersection_sml.png)

It can be used to calculate non-polynomial curves. 
For example a super ellipse `pow(abs(x/a),p)+pow(abs(y/b),p)-1` 

![superellipse](doc/super_ellipse.png)

* **Clipping**, part of a surface inside a set define by an implicit equation, like the set inside a box 
`min(min(min(xh-x,x-xl),min(yh-y,y-yl)),min(zh-z,z-zl))`,
or clipped by a sphere `x^2+y^2+z^2-r^2`
![Clipped cross cap ](doc/cross_cap_clip.png)

* **Mapping** from R^3 to R^3 defined by 3D vector equation in three variables. e.g. a rotation
`[cos(pi th) x - sin(pi th) y,sin(pi th) x + cos(pi th) y,z];`   	
		   
![Torus and its rotation](doc/rotated_torus.png)
* **Vector Fields**, including unoriented vector field, and binary differential equations

![vector field](doc/vector_field.png)

* **Integral Curves**. Uses the points in a geometry to define the starting points

![Limit cycle](doc/hopf_bifucation.png)

* **Colourise**: sets the colour of a surface depending on an expression. For example to colour by the z coordinate
`[(z+1), 0,(1-z)];`	setting the red, green, and blue components for each point. 
	
![Colour by height](doc/colour_by_height.png)
* **Extrude**: produces surfaces of revolution and similar surfaces which depend on a curve and an equation. 
Can be used to produce families of curves.

![Surface of revolution](doc/surface_of_revolution.png)
	 
Several of these models have versions where the equation of another curve or surface can be used as part of the definition

* **Generalised Mappings** where the equation depends on another surface. For example projection of a curve onto a surface.
For example Gauss Map of a surface

```
	N / sqrt(N.N);   // Unit normal
	N = Sx ^^ Sy;    // calculate normal using cross product
	Sx = diff(S,x);  // derivatives of surface S
	Sy = diff(S,y);  // Definition of S read from the input surface
```

![cusp of gauss map](doc/cusp_of_gauss_map.png)
* **Generalised Intersections** where the equation depends on the definition of another curve or surface. 
e.g. The profile of a surface, or parabolic lines

			// The profile of a surface
			N . [A,B,C];
			N = diff(S,x) ^^ diff(S,y);	

![Profile of a torus](doc/torus_profile_sml.png)

* **Generalised Clipping**: e.g. the part of 	surface contained inside another already defined implicit surface

* **Generalised Colourise**: colour by Gaussian or mean curvature

![Gauss curvature of a torus](doc/gauss_curvature.png)

* **Generalised Extrude**: e.g. tangent developable of a curve, or envelope of normals

```
        S + t T;            // Point on surface plus a multiple of unit tangent
        T = TT/sqrt(TT.TT); // unit length
        TT = diff(S,x);     // tangent to curve
```
![tangent developable](doc/tangent_developable.png)

* **Generalised Vector Fields**: e.g. principle directions which are calculated using the definition of the input surface

![Principle directions](doc/principle_directions.png)

* **Generalised Integrals Curves**: e.g. principle curves of a surface calculated using the definition of the input surface

![principle curve](doc/principle_curves.png)

There are some more specialised modules

* Ridge Intersections: curves which depend on a surface and a vector field, for example the ridges of a surface 

![Ridges and sub-parabolic lines](doc/ridges.png)

* **BiIntersection** Intersections where the equations depends on a pair of curves. For example the pre-symmetry set of a curve.

* **BiMap** Mapping where the equation depends on a pair of curves. For example the Symmetry set.

![symmetry set](doc/symmetry_set.png)

* Projective varieties: algebraic surfaces defined in real projective space, 
with options for stereographic projections and rotations in 4D

![Sarti's surface](doc/sarti.png)

## Requirements

The program requires

* Java version 8 or upwards.
* JavaView mathematical visualisation software from [javaview.de](http://www.javaview.de/). Alas not open-source.
* JEP 2.4.1 Java Expression Parser, a mathematical parser evaluator from my fork 
[jep-java-gpl](https://github.com/RichardMorris/jep-java-gpl) which 
contains some small customisation of the package to fit the needs of the SingSurf program.
* The automated build process uses [Apache Ant](https://ant.apache.org/). It should be relatively easy to compile in java without ant.
* To compile [JUnit 4](https://junit.org/junit4/) must be available, but is not required to run.
It is a good idea to register your version of JavaView. Registration provides a license file `jv-lic.lic` which should be copied to the `rsrc` directory, this prevents a notification message appearing.    

A different version of this program is available for use with the newer 3.5/4.0 commercial release of Jep from [singularsys.com](http://www.singularsys.com/jep/). 
This has a more flexible parser but is otherwise identical in operation.

## Installation and running

###Source code release
The source code can be downloaded from [SingSurfGPL on github](https://github.com/RichardMorris/SingSurfGPL).

For the git source code, there are three different main classes
* `org.singsurf.singsurf.SingSurf3D` the 3D version with all sub-types
* `org.singsurf.singsurf.SingSurf2D` the 2D version, with setting for examining curves in the plane
* `org.singsurf.singsurf.ASurfSimp` a simplified version just with the algebraic surface component

To compile first set environment variables for Jep, Javaview home directories and the Junit jar.
Using 'bash' on linux use

     export JEP_HOME=C:/User/rich/git/jep-java-gpl
     export JAVAVIEW_HOME=C:/Users/rich/bin/javaview
     export JUNIT_JAR="C:\Users\rich\.p2\pool\plugins\org.junit_4.13.0.v20200204-1500.jar;C:\Users\rich\.p2\pool\plugins\org.hamcrest.core_1.3.0.v20180420-1519.jar"

the ant build file could then be run using

     ant -DJEP_HOME=$JEP_HOME -DJAVAVIEW_HOME=$JAVAVIEW_HOME -Djunit.jar=$JUNIT_JAR

this will compile the java classes. 

To run the main 3D version on unix

     ./singsurf.sh

This will use the JEP_HOME and JAVAVIEW_HOME environment variable if set, otherwise it
will read values specified in the script.
    
On windows use

     singsurf.bat

The script will need to be edited to set the location of the Jep and Javaview home directories.

Both versions have variables which can be set to change the amount of memory allocated or the font sizes used.

### Bundled release

A zip file with an executable jar file and all necessary files is available from [singsurf.org](http://singsurf.org/singsurf/SingSurfPro.html). Once unpacked this can be run using a single line Java command.
This include the necessary Jep and JavaView files.

Once unzipped the main SingSurf3D program can be used by running either

     ./singsurf.sh

on Linux or Macs via a command prompt or for Windows use

     singsurf.bat

### Javaview licence file

For best operation you should obtain a license file for the JavaView program. This prevents a red error message appearing on the main window.
To obtain the file complete [JavaView registration](http://www.javaview.de/download/registration.html) and a file will be automatically
sent to you. Once you have it place it in the `rsrc` sub-directory of the JavaView home directory or
the `rsrc` sub-directory of the SingSurf home directory for the bundled release.

For any installation problems contact rich@singsurf.org.





					               
