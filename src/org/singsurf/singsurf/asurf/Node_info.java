/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Node_info {
    public Sol_info sol;
    
    public Node_info(Sol_info sol) {
		super();
		this.sol = sol;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(sol.toStringCore("Node:   "));
        sb.append('\n');
        return sb.toString();
    }

}
