/*
Created 16-Jun-2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.TextArea;

/**
 * @author Richard Morris
 *
 */
public class SSHelp extends jv.project.PjProject {
    private static final long serialVersionUID = 1L;
    /** The main text area for the equation. **/
    protected TextArea helpText;

    /**
     * 
     */
    public SSHelp() {
	super("SingSurf Help");
	init();
    }

    @Override
    public void init() {
	helpText = new TextArea("", 40, 40, TextArea.SCROLLBARS_VERTICAL_ONLY);
	helpText.setText("To create a new curve or surface use the\n" + "New menu");
	helpText.setEditable(false);
	super.init();
    }
}
