/* @author rich
 * Created on 20-Jun-2003
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.singsurf.singsurf;
import java.awt.Panel;

import org.singsurf.singsurf.clients.ASurf;
import org.singsurf.singsurf.clients.AbstractClient;

import jv.object.PsViewerIf;

/**
 * A Simple version of the SingSurf applet. Just supports a single
 * Algebraic Surfaces Project.
 * @author Rich Morris
 * Created on 20-Jun-2003
 */
public class SingSurfSimp extends PaSingSurf {

	private static final long serialVersionUID = 1L;
    int serverType = 0;
	private Panel pProject;
	@Override
    public void init() 
	{
		super.init();
//		String s = m_viewer.getParameter("ServerType");
//		if(s==null) serverType = AbstractCGIClient.NO_SERVER;
//		else if(s.equals("CGI")) serverType = AbstractCGIClient.CGI_SERVER;
//		else if(s.equals("EXEC")) serverType = AbstractCGIClient.EXEC_SERVER;
//		else serverType = AbstractCGIClient.NO_SERVER;

		if(PRINT_DEBUG) System.out.println("SSS init");
		AbstractClient asurf = new ASurf(store,"Algebraic Surface","defs/asurf.defs");
//		asurf.setCGIServer("asurfCV",serverType,
//			m_viewer.getParameter("ServerDir"),
//			m_viewer.getParameter("ServerExtension"));
		m_viewer.addProject(asurf);
		asurf.init2();
		m_viewer.selectProject(asurf);

		
		/** The location the sing surf panels is displayed in. */
		String SSPanelPos = m_viewer.getParameter("SSPanel");
		if(!SSPanelPos.equals("Control"))
		{
			pProject = m_viewer.getPanel(PsViewerIf.PROJECT);
			add(SSPanelPos,pProject);
		}

		validate();
	}

	/**
	 * Standalone application support. The main() method acts as the applet's
	 * entry point when it is run as a standalone application. It is ignored
	 * if the applet is run from within an HTML page.
	 */

	public static void main(String args[]) {
		SingSurfSimp va = new SingSurfSimp();
		commonMain(va,args);
	}
}
