/* @author rich
 * Created on 03-Jul-2003
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.singsurf.singsurf;

/**
 * Defines the class which can return a String with the definition of the surface.
 * @author Rich Morris
 * Created on 03-Jul-2003
 */
public interface CgiStringMakerI {
	/** Returns a string with the cgi string for the definition.
	 * Either a CGI encoded string DEF=x%3Dy&....
	 * or and xml definition
	 * &lt;definition name="Sphere"type="asurf"&gt;
	 * ...
	 * &lt;/definition&gt;
	 */
	public String makeCGIstring();

	/** these return CGI format strings. */
	public static interface CGI extends CgiStringMakerI { }
	
	/** these return xml def format strings. */
	
	public static interface XmlI extends CgiStringMakerI { }
}
