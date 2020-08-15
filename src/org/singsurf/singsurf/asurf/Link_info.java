/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Link_info {
    public Sol_info A;
	public Sol_info B;
    public boolean plotted;
        
    public Link_info(Sol_info a, Sol_info b) {
		super();
		A = a;
		B = b;
//		this.status = status;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("LINK %b%n",plotted));
        sb.append(A.toString());     
        sb.append("\n");
        sb.append(B.toString());     
        sb.append("\n");
        return sb.toString();
    }

}
