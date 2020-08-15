/*
Created 12-Jun-2006 - Richard Morris
*/
package org.singsurf.singsurf.geometries;

import java.util.SortedSet;

import org.singsurf.singsurf.calculators.Calculator;
import org.singsurf.singsurf.clients.AbstractClient;

/**
 * @author Richard Morris
 *
 */
public interface SSGeomListener {
	public abstract void refreshList(SortedSet<String> list);
	public abstract void geometryHasChanged(String geomName);
	public abstract void geometryNameHasChanged(String oldName,String newName);
	public abstract void removeGeometry(String geomName,boolean rmDependants);
	public abstract void geometryDefHasChanged(AbstractClient client, Calculator inCalc);
}
