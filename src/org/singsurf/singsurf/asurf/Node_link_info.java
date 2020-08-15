/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Node_link_info {
	public Node_info A;
	public Node_info B;
	public Sing_info singA, singB;
	public LinkStatus status;

	public Node_link_info(Node_info a, Node_info b, LinkStatus status) {
		super();
		A = a;
		B = b;
		this.status = status;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("NODE LINK: status %s\n", status));
		sb.append(A.sol);
		sb.append('\n');
		sb.append(B.sol);
		sb.append("\n");

		return sb.toString();

	}

}
