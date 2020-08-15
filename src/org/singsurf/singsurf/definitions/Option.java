/**
 * 
 */
package org.singsurf.singsurf.definitions;


public class Option
	{
		final String name;
		final OptionType type;
		double doubleVal;
		int	integerVal;
		String  stringVal;
		public Option(String varname,String value)
		{
			name = varname; 
			this.stringVal = value;
			doubleVal = parseDoubleValue(value);
			integerVal = (int) doubleVal;
			type = null;
		}

		private double parseDoubleValue(String value) {
			double d;
			try
			{
				d = Double.valueOf(value).doubleValue();
			}
			catch(NumberFormatException e)
			{	// Do really want to ignore this error
				d = 0.0;
			}
			return d;
		}
		
		public Option(String name,String value,OptionType type)
		{
			this.name = name; 
			this.stringVal = value;
			doubleVal = parseDoubleValue(value);
			integerVal = (int) doubleVal;
			this.type = type;
		}
		
		public Option(String line)
		{
			this(DefinitionReader.getAttribute(line,"name"),
				DefinitionReader.getAttribute(line,"value"));
		}
		

		public Option(String name2, double value) {
		    name = name2;
		    doubleVal = value;
		    stringVal = Double.toString(value);
		    integerVal = (int) value;
		    type = null;
		}
		public String getName() { return name; }
		public OptionType getType() { return type; }
		public double getDoubleVal() { return doubleVal; }
		public int getIntegerVal()   { return integerVal; }
		public String getStringVal() { return stringVal; }
		public String getValue() { return stringVal; }
		public boolean getBoolVal()  { return "true".equals(stringVal); }
		public void setBoolVal(boolean b)  { 
		     stringVal = (b ?  "true" : "false");
		}
		public void setValue(int val) {
			integerVal = val;
			stringVal = Integer.toString(val);
		}
		public void setValue(String val) {
		    stringVal = val;
		}

		
		@Override
		public String toString()
		{
			String s1 =  "<option name=\"" + name + "\""
				+ " value=\""+stringVal+"\"";
			if(type!=null) {
				s1 += " type\""+type.toString()+"\"";
			}
			s1 += "/>\n";
			return s1;
		}
		public Option duplicate() {
			Option res = new Option(this.name,this.stringVal);
			return res;
		}
		public void setValue(double value) {
		    doubleVal = value;
		    stringVal = Double.toString(value);
		}

		public String getJSON() {
			String s1 = "{ \"name\": \"" + name + "\", "
					+ "\"value\": \"" + stringVal + "\"";
			if(type!=null) {
				s1 += ", \"type\": \""+type.toString()+"\"";
			}
			s1 += " }";
			return s1;
		}
	}