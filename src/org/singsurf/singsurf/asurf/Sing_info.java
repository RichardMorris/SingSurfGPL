/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Sing_info {
    public Sol_info sing;
    LinkStatus status;
    public short numNLs=0;
    public Node_link_info[] adjacentNLs=null;    
    
	public Sing_info(Sol_info sing, LinkStatus status) {
		super();
		this.sing = sing;
		this.status = status;
	}
	
	@Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
		sb.append(String.format("SING: status %s adjacent node links %d%n",status,numNLs));
		sb.append(this.sing);
		sb.append('\n');
		return sb.toString();
	}
}
