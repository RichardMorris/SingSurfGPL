/*
Created 26-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

class Pair {
	int a,b;
	Pair(int i,int j) {
		if(i>j) {a=i;b=j;}
		else	{a=j;b=i;}
	}
	@Override
	public boolean equals(Object obj) {
		Pair p = (Pair) obj;
		return (a==p.a&&b==p.b);
	}
	@Override
	/**
	 * A perfect hashCode for a pair.
	 * Based upon unique numbering for rationals.
	 */
	public int hashCode() {
		int h = (a*(a+1));
		return h+b;
	}
}