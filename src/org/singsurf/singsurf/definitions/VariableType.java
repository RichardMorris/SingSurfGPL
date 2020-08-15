package org.singsurf.singsurf.definitions;

import java.util.ArrayList;
import java.util.List;

/** typesafe enum for var types */
public class VariableType
{
	static List<VariableType> knownTypes = new ArrayList<VariableType>(20);
	private final String type;
	private VariableType(String s) { type = s; knownTypes.add(this); }
	

	@Override
	public String toString() { return getType(); }
	public static VariableType getType(String s)
	{
		if(s==null) return null;
		for(VariableType cur:knownTypes)
		{
			if(s.equals(cur.type)) return(cur);
		}
		return null;
	}
	public String getType() {
		return type;
	}
	
	public static final VariableType Normal  = new VariableType("Normal");
	public static final VariableType AuxOutX = new VariableType("AuxOutX");
	public static final VariableType AuxOutY = new VariableType("AuxOutY");
	public static final VariableType AuxOutZ = new VariableType("AuxOutZ");
	public static final VariableType AuxInX  = new VariableType("AuxInX");
	public static final VariableType AuxInY  = new VariableType("AuxInY");
	public static final VariableType AuxInZ  = new VariableType("AuxInZ");
	public static final VariableType VectorX = new VariableType("VectorX");
	public static final VariableType VectorY = new VariableType("VectorY");
	public static final VariableType VectorZ = new VariableType("VectorZ");
	public static final VariableType EigenPx = new VariableType("EigenPx");
	public static final VariableType EigenPy = new VariableType("EigenPy");
	public static final VariableType EigenQx = new VariableType("EigenQx");
	public static final VariableType EigenQy = new VariableType("EigenQy");

}