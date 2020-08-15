package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jv.geom.PgElementSet;
import jv.vecmath.PiVector;

public class MeshTopology {
	List<Integer>[] elementByVertex;
	
	@SuppressWarnings("unchecked")
	public MeshTopology(PgElementSet surf) {
		elementByVertex = (List<Integer>[]) new List<?>[surf.getNumVertices()];
		for(int eleNum=0;eleNum<surf.getNumElements();++eleNum) {
//		for(PiVector vec:surf.getElements()) {
			PiVector vec = surf.getElement(eleNum);
			for(int i:vec.getEntries()) {
				if(elementByVertex[i]==null) {
					elementByVertex[i] = new ArrayList<Integer>();
				}
				elementByVertex[i].add(eleNum);
			}
		}
	}
	
	public List<Integer> elementsBy(int vertIndex) {
		List<Integer> res = elementByVertex[vertIndex];
		if(res==null)
			return Collections.emptyList();
		return res;
	}
}
