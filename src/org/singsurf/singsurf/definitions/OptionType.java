package org.singsurf.singsurf.definitions;

import java.util.ArrayList;
import java.util.List;

/** typesafe enum for var types */
public class OptionType
{
	static List<OptionType> knownTypes = new ArrayList<OptionType>(20);
	
	private final String type;
	private OptionType(String s) { type = s; knownTypes.add(this); }
	public static final OptionType intType  = new OptionType("integer");
	public static final OptionType doubleType = new OptionType("double");
	public static final OptionType StringType = new OptionType("string");

	@Override
	public String toString() { return getType(); }
	public static OptionType getOptionType(String s)
	{
		if(s == null) return null;
		for(OptionType cur:knownTypes)
		{
			if(s.equals(cur.getType())) return(cur);
		}
		return null;
	}
	public String getType() {
		return type;
	}
}