package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CountingListMap<T,F> extends HashMap<T,List<F>> {
	private static final long serialVersionUID = 1L;

	public List<F> add(T key,F val) {
		List<F> num = this.get(key);
		if(num==null) {
			num = new ArrayList<>();
			num.add(val);
			return put(key, num);
		}
		else {
			num.add(val);
			return num;
		}
	}
	
	public Integer getCount(T key) {
		List<F> num = this.get(key);
		if(num==null) 
			return 0;
		return num.size();
	}
	
}
