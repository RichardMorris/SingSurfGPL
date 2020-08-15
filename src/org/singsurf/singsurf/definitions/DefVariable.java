package org.singsurf.singsurf.definitions;

import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.jepwrapper.Dimensions;

/** A single variable. */
public class DefVariable
{
	String name; 
	DefType type=DefType.none;
	double min,max,value;
	int steps=-1;
	public DefVariable(String varname,double min_val,double max_val)
	{
		name = varname; min = min_val; max = max_val; 
	}
	public DefVariable(String varname,double min_val,double max_val,int steps)
	{
		name = varname; min = min_val; max = max_val; this.steps = steps;
	}
	public DefVariable(String varname,String type,double min_val,double max_val)
	{
		name = varname; min = min_val; max = max_val;
		this.type = DefType.get(type);
	}
	public DefVariable(String varname,String type,double min_val,double max_val,int steps)
	{
		name = varname; min = min_val; max = max_val;
		this.type = DefType.get(type);
		this.steps = steps;
	}
	public DefVariable(String varname,DefType type,double min_val,double max_val,int steps)
	{
		name = varname; min = min_val; max = max_val;
		this.type = type;
		this.steps = steps;
	}
	public DefVariable(String varname,String type)
	{
		name = varname; min = 0.0; max = 0.0;
		this.type = DefType.get(type);
	}

	public static DefVariable parseTag(String line) throws ParseException
	{
		String myname = DefinitionReader.getAttribute(line,"name");
		DefType mytype = DefType.get(DefinitionReader.getAttribute(line,"type"));
		double mymin=0.0,mymax=0.0;
		int mysteps = -1;
		try
		{
			String smin = DefinitionReader.getAttribute(line,"min");
			if(smin!=null)
				mymin = Double.valueOf(smin).doubleValue();
			String smax = DefinitionReader.getAttribute(line,"max");
			if(smax!=null)
				mymax = Double.valueOf(smax).doubleValue();
			String s = DefinitionReader.getAttribute(line,"steps");
			if(s != null)
				mysteps = Integer.valueOf(s).intValue();
//			String d = LsmpDefReader.getAttribute(line,"dim");
//			if(d != null)
//				mydim = Integer.valueOf(d).intValue();
		}
		catch(NumberFormatException e)
		{
			throw new ParseException("Format markup for variable: "+line);
		}
		DefVariable v = new DefVariable(myname,mytype,mymin,mymax,mysteps);
		//v.setDim(mydim);
		return v;
	}
	public String getName() { return name; }
	public double getMin() { return min; }
	public double getMax() {return max; }
	public int getSteps() {return steps; }
	public DefType getType() {return type; }
	@Override
	public String toString()
	{
		String s1 = "<variable name=\"" + name + "\""
			+ " min=\"" + min + "\""
			+ " max=\"" + max + "\"";
		if(steps != -1)
			s1 = s1 + " steps=\""+steps+"\"";
		if(type!=null)
			return s1+" type=\"" + type.toString() +"\">\n";
		else
			return s1+">\n";

	}
	
	public DefVariable duplicate() {
		DefVariable res = new DefVariable(this.name,this.type,this.min,this.max,this.steps);
		return res;
	}

	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public void setBounds(double min,double max,int steps) {
		this.min = min;
		this.max = max;
		this.steps = steps;
	}
	public Dimensions getDim() {return this.type.outputDims;}

	public String getJSON() {
		String s1 = "{ \"name\": \"" + name + "\", "
				+ "\"min\": \"" + min + "\", "
				+ "\"max\": \"" + max + "\"";
			if(steps != -1)
				s1 = s1 + ", \"steps\": \""+steps+"\"";
			if(type!=null)
				s1 = s1 + ", \"type\": \"" + type.toString() +"\"";

		return s1+"}";
	}
}