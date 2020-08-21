package org.singsurf.singsurf.definitions;

import java.util.ArrayList;
import java.util.List;

import org.lsmp.djep.vectorJep.Dimensions;



/** typesafe enum for var types */
public class DefType
{
	public static List<DefType> knownTypes = new ArrayList<DefType>(20);
	String type;
	/**
	 * Dimensions of variable representing output used in chained calculator.
	 */
	Dimensions outputDims;
	Dimensions inputDims;
	private DefType(String s) { 
	    type = s; 
	    inputDims = Dimensions.ONE;
	    outputDims = Dimensions.ONE;
	    knownTypes.add(this); 
	}
	/**
	 * 
	 * @param s name
	 * @param in input dim
	 * @param out output dim
	 */
	public DefType(String s, int in,int out) {
	    type = s; 
	    inputDims = Dimensions.valueOf(in);
	    outputDims = Dimensions.valueOf(out);
	    knownTypes.add(this);
    }
    public static final DefType none  = new DefType("none");
    public static final DefType ingrVar  = new DefType("ingrVar");
    public static final DefType localVar  = new DefType("localVar");

    public static final DefType psurf = new DefType("psurf",2,3);
	public static final DefType pcurve = new DefType("pcurve",1,3);
	
	public static final DefType asurf = new DefType("asurf",3,1);
	public static final DefType acurve = new DefType("acurve",2,1);
	//public static final DefType acurve3 = new DefType("acurve3",2);
	public static final DefType intersect = new DefType("intersect",3,1);
	public static final DefType genInt = new DefType("genInt",3,1);

	public static final DefType clip = new DefType("clip",3,1);
	public static final DefType genClip = new DefType("genClip",3,1);

	public static final DefType mapping = new DefType("mapping",3,3);
	public static final DefType genMap = new DefType("genMap",3,3);
	
	public static final DefType biMap = new DefType("biMap",3,3);
	public static final DefType biInt = new DefType("biInt",2,1);

	public static final DefType vfield = new DefType("vfield",3,3);
	public static final DefType genVfield = new DefType("genVfield",3,3);

	public static final DefType icurve = new DefType("icurve",3,3);
	public static final DefType genicurve = new DefType("genICurve",3,3);

	public static final DefType ridgeInt = new DefType("ridgeInt",3,1);

	public static final DefType colour = new DefType("colour",3,3);
	public static final DefType genColour = new DefType("genColour",3,3);

	public static final DefType extrude = new DefType("extrude",2,3);
	public static final DefType genExtrude = new DefType("genExtrude",2,3);
	public static final DefType p3 = new DefType("p3",2,3);
	public static final DefType globals = new DefType("globals");

	@Override
	public String toString() { return type; }
	public static DefType get(String s)
	{
		if(s == null) return none;
		for(DefType cur: knownTypes) {
			if(s.equals(cur.type)) return(cur);
		}
		return none;
	}
    public Dimensions getOutputDimensions() {
        return outputDims;
    }
	public Dimensions getInputDims() {
		return inputDims;
	}
}