package org.singsurf.singsurf.definitions;
import java.util.ArrayList;
import java.util.List;

import org.singsurf.singsurf.SingSurfMessages;

/** A class to contain info about a surface definition. */

public class Definition
{
	/** The name of the definition. */
	String name;
	/** The type of surface. */
	DefType type;
	/** The type of input for operators. */
	DefType opType=null;
	/** The main definition. */
	String	equation;
	/** The variables. */
	List<DefVariable> variables = null;
	/** The parameters. */
	List<Parameter> parameters = null;
	/** The options. */
	List<Option> options = null;

	public Definition(String name,String type, String eqn)
	{
		this.name = name;
		this.type = DefType.get(type);
		equation = eqn;
		variables = new ArrayList<DefVariable>();
		parameters = new ArrayList<Parameter>();
		options = new ArrayList<Option>();
	}
	public Definition(String name,DefType type, String eqn)
	{
		this.name = name;
		this.type = type;
		equation = eqn;
		variables = new ArrayList<DefVariable>();
		parameters = new ArrayList<Parameter>();
		options = new ArrayList<Option>();
	}
	public Definition(String name,String type, String eqn,List<DefVariable> vars,List<Parameter> params,List<Option> opts)
	{
		this.name = name;
		this.type = DefType.get(type);
		equation = eqn;
		variables = vars;
		parameters = params;
		options = opts;
	}
	public Definition(String name,String type, String eqn,String opType,List<DefVariable> vars,List<Parameter> params,List<Option> opts)
	{
		this.name = name;
		this.type = DefType.get(type);
		this.opType = DefType.get(opType);
		equation = eqn;
		variables = vars;
		parameters = params;
		options = opts;
	}
	

	public String getName() { return name; }
	public DefType getType() { return type; }
	public DefType getOpType() { return opType; }
	public void setOpType(DefType opType) {this.opType = opType; }
	public String getEquation() { return equation; }
	public void setEquation(String def) {
		this.equation = def;
	}

	public int getNumVars() { if(variables==null) return -1; else return variables.size(); }
//	public Variable[] getVars() { return variables; }
	public DefVariable getVar(int i) { return variables.get(i); }
	public DefVariable getVariable(String varname)
	{
		for(DefVariable v:variables)
			if(varname.equals(v.getName())) return v;
		return null;
	}
	public int getVariableIndex(DefVariable v)
	{
		return variables.indexOf(v);
	}
	public void setVariable(int index,DefVariable var)
	{
		if(index>=variables.size())
			variables.add(index,var);
		variables.set(index,var);
	}
	
	public List<DefVariable> getVariablesByType(DefType vartype)
	{
		List<DefVariable> res = new ArrayList<DefVariable>();
		for(DefVariable var:variables) {
			if(var.getType() == vartype)
				res.add(var);
		}
		return res;
	}
	public int getNumParams() { if(parameters==null) return -1; else return parameters.size(); }
	public List<Parameter> getParams() { return parameters; }
	public Parameter getParam(int i) { return parameters.get(i); }
	//public Enumeration getParamEnumeration() { return parameters.elements(); }
	/** Tests whether a parameter with name exists. 
	 * 
	 * @param paramname - name of parameter
	 * @return the reference to the parameter or null if not found
	 */
	public Parameter getParameter(String paramname)
	{
		for(Parameter p:parameters)
			if(paramname.equals(p.getName())) return p;
		return null;
	}
	public boolean setParameterValue(String name,double val) {
		Parameter p=getParameter(name);
		if(p==null) return false;
		p.setVal(val);
		return true;
	}
	/**
	 * Deletes the parameter p 
	 * @param p
	 * @return true if parameter exists.
	 */
	public boolean deleteParameter(Parameter p)	{
		if(p==null) return false;
		return parameters.remove(p);
	}
	/**
	 * Deletes the parameter p 
	 * @param paramname - name of the parameter
	 * @return true if parameter exists.
	 */
	public boolean deleteParameter(String paramname)	{
		return deleteParameter(getParameter(paramname));
	}
	public Parameter addParameter(String paramname)
	{
		Parameter p = getParameter(paramname);
		if(p!=null) return p;
		p = new Parameter(paramname,0.0);
		parameters.add(p);
		return p;
	}
	public void setParamNames(Object newNames[])
	{
		int oldSize = parameters.size();
		boolean keep[]= new boolean[oldSize];
		for(int i=0;i<oldSize;++i) keep[i]=false;
		for(int i=0;i<newNames.length;++i)
		{
			Parameter p = getParameter((String) newNames[i]);
			if(p==null) addParameter((String) newNames[i]);
			else
				keep[parameters.indexOf(p)]=true;
		}
		for(int i=oldSize-1;i>=0;--i) 
			if(!keep[i]) parameters.remove(i);
	}
	
	public int getNumOpts() { if(options==null) return -1; else return options.size(); }
//	public LsmpDef.Option[] getOpts() { return options; }
	public Option getOpt(int i) { return options.get(i); }

	public Option getOpt(String optionName)
	{
		for(Option opt:options)
			if(optionName.equals(opt.getName()))
				return opt; 
		return null;
	}
	public Option getOption(String optionName)
	{
		return getOpt(optionName);
	}
	
	public Option setOption(String name,int val) {
		Option op = getOpt(name);
		if(op!=null)
			op.setValue(val);
		else {
			op = new Option(name,Integer.toString(val));
			this.options.add(op);
		}
		return op;
	}
	
	public Option setOption(String name,boolean val) {
		Option op = getOpt(name);
		if(op!=null)
			op.setBoolVal(val);
		else {
			op = new Option(name,(val ?  "true" : "false"));
			this.options.add(op);
		}
		return op;
	}
	public Option setOption(String name, String val) {
		if(val==null)
			return null;
		Option op = getOpt(name);
		if(op!=null)
			op.setValue(val);
		else {
			op = new Option(name,val );
			this.options.add(op);
		}
		return op;
	}
	
	public Option setOption(String name2, double value) {
		Option op = getOpt(name2);
		if(op!=null)
			op.setValue(value);
		else {
			op = new Option(name2,value );
			this.options.add(op);
		}
		return op;
	}


	
	/** add a variable. */
	public void add(DefVariable var) { variables.add(var); }
	/** add a parameter. */
	public void add(Parameter param) { parameters.add(param); }
	/** add an option. */
	public void add(Option opt) { options.add(opt); }

	/** Returns a string with the XML for the definition. **/
	
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer(1000);
		buf.append("<definition name=\""+name+"\"");
		if(type!=null)
			buf.append(" type=\""+type.toString()+"\"");
		if(opType!=null)
			buf.append(" opType=\""+opType.toString()+"\"");
		buf.append(">\n");
		buf.append(equation);
		buf.append('\n');

		for(DefVariable v:variables)
			buf.append(v.toString());
		for(Parameter p:parameters)
			buf.append(p.toString());
		for(Option op:options)
			buf.append(op.toString());

		buf.append("</definition>\n");
		return buf.toString();
	}

	public Definition duplicate()
	{
		Definition res = new Definition(this.name,this.type,this.equation);
		res.setOpType(this.getOpType());
		for(DefVariable v:variables)
			res.variables.add(v.duplicate());
		for(Parameter p:parameters)
			res.parameters.add(p.duplicate());
		for(Option o:options)
			res.options.add(o.duplicate());
		return res;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * <pre>
	 * {
	 * "name": "Kummer Surface",
	 * "type": "ASurf",
	 * "fulltype": "Algebraic Surface",
	 * "equation": "(3-v^2) ( x^2+y^2 + z^2 - v^2)^2 
	 *  - (3 v^2 - 1) p q r s;
		p = ( 1 - z - x sqrt(2));
		q = ( 1 - z + x sqrt(2));
		r = (1 + z + y sqrt(2));
		s=  ( 1 + z - y sqrt(2));",
		"variables" : [
       		{ "name": "x", "min": -1.54, "max": 1.63 },
       		{ "name": "y", "min": -1.53, "max"; 1.64 },
       		{ "name": "z", "min": -1.52, "max": 1.65 }],
       	"parameters" : [
      		{ "name": "v", "value": 1.2 } ],
      	"options": [
      		{ "name": "coarse", "value": 16 },
      		{ "name": "fine", "value": 64 },
      		{ "name": "face", "value": 2048} ]
      	}
      	</pre>
	 * 
	 * @return a JSON format string.
	 */
	public String getJSON() {

		StringBuilder buf = new StringBuilder();
		buf.append("{\n");
		buf.append("\"name\": \""+name+"\",\n");
		if(type!=null) {
			buf.append("\"type\": \""+type.toString()+"\",\n");
			buf.append("\"longtype\": \""+SingSurfMessages.getString(type.toString()+".longName")+"\",\n");
		}
		if(opType!=null)
			buf.append("\"opType\": \""+opType.toString()+"\",\n");
		
		buf.append("\"equation\": \"");
		buf.append(equation.replace("\n", "\\n"));
		buf.append("\",\n");
		buf.append("\"variables\": [\n");
		int count=0;
		for(DefVariable v:variables) {
			if(count++>0) buf.append(",\n");
			buf.append("    "+v.getJSON());
		}
		buf.append("],\n");
		buf.append("\"parameters\": [\n");
		count =0;
		for(Parameter p:parameters) {
			if(count++>0) buf.append(",\n");
			buf.append("    "+p.getJSON());
		}
		buf.append("],\n");
		buf.append("\"options\": [\n");
		count =0;
		for(Option op:options) {
			if(count++>0) buf.append(",\n");
			buf.append("    "+op.getJSON());
		}
		buf.append("]\n}");
		return buf.toString();

	}
	public Option removeOption(String string) {
		Option opt = this.getOpt(string);
		if(opt!=null)
			options.remove(opt);
		return opt;
	}
}
