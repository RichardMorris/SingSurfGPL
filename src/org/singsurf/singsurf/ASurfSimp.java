/* @author rich
 * Created on 20-Jun-2003
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.singsurf.singsurf;
import java.awt.Panel;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.singsurf.singsurf.clients.ASurf;
import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.Definition;

import jv.object.PsViewerIf;

/**
 * A Simple version of the SingSurf applet. Just supports a single
 * Algebraic Surfaces Project.
 * @author Rich Morris
 * Created on 20-Jun-2003
 */
public class ASurfSimp extends PaSingSurf {

    private static final long serialVersionUID = 1L;
	private Panel pProject;

    @Override
    public void init() 
    {
        super.init();

        if(PRINT_DEBUG) System.out.println("SSS init");
//        String defUrl = m_viewer.getParameter("DefUrl");
        
        
        
        AbstractClient asurf=null; 
        Definition def=null;

        if(def == null) {
//        	String model = this.m_frame==null ? this.getParameter("Model") : null;
        	String model = null;
        	if(model!=null)
            	asurf = new ASurf(store,"defs/asurf.defs",model);
        	else
        		asurf = new ASurf(store,"Algebraic Surface","defs/asurf.defs");
        }
        else
        	asurf = new ASurf(store,"Algebraic Surface","defs/asurf.defs",def);
        	
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
        else
        	System.out.println("PanPos \""+SSPanelPos+"\"");

        validate();
    }

	public Definition calcDefFromQuery(String query) {
		Definition def = null;
		
		if(query!=null) {
			String[] parts = query.split("&");
			Map<String,String> map = new HashMap<String,String>();
			for(String part:parts) {
				String[] bits = part.split("=");
				if(bits.length!=2) {
					System.out.println("Could not decode "+part);
					continue;
				}
				try {
					String entry = URLDecoder.decode(bits[0],"US-ASCII");
					String value = URLDecoder.decode(bits[1],"US-ASCII");
					map.put(entry, value);
				} catch (UnsupportedEncodingException e) {
					System.out.println(e.getMessage());
				}
			}
			
			String eqn = map.get("EQN");
			if(eqn!=null) {
				def = new Definition("Algebraic Surface","asurf",eqn);
				String min = map.get("XMIN");
				String max = map.get("XMAX");
				if(min!=null && max!=null) {
					DefVariable var = new DefVariable("x",Double.parseDouble(min),Double.parseDouble(max));
					def.add(var);
				}
				else {
					DefVariable var = new DefVariable("x",-1.14,1.03);
					def.add(var);
				}
				min = map.get("YMIN");
				max = map.get("YMAX");
				if(min!=null && max!=null) {
					DefVariable var = new DefVariable("y",Double.parseDouble(min),Double.parseDouble(max));
					def.add(var);
				}
				else {
					DefVariable var = new DefVariable("y",-1.13,1.04);
					def.add(var);
				}
				min = map.get("ZMIN");
				max = map.get("ZMAX");
				if(min!=null && max!=null) {
					DefVariable var = new DefVariable("z",Double.parseDouble(min),Double.parseDouble(max));
					def.add(var);
				}
				else {
					DefVariable var = new DefVariable("z",-1.12,1.05);
					def.add(var);
				}
				
			}
		}
		return def;
	}

    /**
     * Standalone application support. The main() method acts as the applet's
     * entry point when it is run as a standalone application. It is ignored
     * if the applet is run from within an HTML page.
     */

    public static void main(String args[]) {
        ASurfSimp va = new ASurfSimp();
        commonMain(va,args);
    }
}
