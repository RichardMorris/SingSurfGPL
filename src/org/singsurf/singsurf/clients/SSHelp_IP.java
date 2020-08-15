/*
Created 16-Jun-2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import jv.object.PsUpdateIf;
import jv.project.PjProject_IP;

/**
 * @author Richard Morris
 *
 */
public class SSHelp_IP extends PjProject_IP {
    private static final long serialVersionUID = 1L;
    SSHelp parent;

    /**
     * 
     */
    public SSHelp_IP() {
	super();

    }

    @Override
    public void init() {
	addTitle("SingSurf Help");

    }

    @Override
    public void setParent(PsUpdateIf par) {
	super.setParent(par);
	parent = (SSHelp) par;
	add(parent.helpText);
    }
}
