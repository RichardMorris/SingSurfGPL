/*
Created 23 May 2011 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.util.List;

import org.singsurf.singsurf.definitions.Definition;
import org.singsurf.singsurf.geometries.GeomStore;

public interface ClientFactory {
    /** Create a new instance with a given def
     * 
     * @param name Name of client
     * @param def definition of surface
     * @param store GeomStore object
     * @return a new AbstractProject instance
     */
    public AbstractProject newInstance(String name,Definition def,GeomStore store);
    
    /**
     * Create a new instance with no predefined def
     * @param name Name of client
     * @param defs list of definitions
     * @param store GeomStore object
     * @return  a new AbstractProject instance
     */
    public AbstractProject newInstance(String name,List<Definition> defs,GeomStore store);
}
