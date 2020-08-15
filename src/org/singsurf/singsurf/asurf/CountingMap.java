package org.singsurf.singsurf.asurf;

import java.util.HashMap;

public class CountingMap<T> extends HashMap<T,Integer> {
	private static final long serialVersionUID = 1L;

	public Integer increment(T key) {
		Integer num = this.get(key);
		if(num==null) 
			return put(key, 1);
		else
			return put(key,num+1);
	}
	
	public Integer getCount(T key) {
		Integer num = this.get(key);
		if(num==null) 
			return 0;
		return num;
	}
	
}
